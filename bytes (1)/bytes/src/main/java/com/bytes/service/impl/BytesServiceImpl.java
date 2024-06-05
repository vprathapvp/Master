package com.bytes.service.impl;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.bytes.message.ResponseBytesMessage;
import com.bytes.message.ResponseFile;
import com.bytes.message.ResponseGetall;
import com.bytes.message.ResponseMetadata;
import com.bytes.model.Bytes;
import com.bytes.model.Metadata;
import com.bytes.model.User;
import com.bytes.model.WatchedVideo;
import com.bytes.repository.BytesRepository;
import com.bytes.repository.MetadataRepository;
import com.bytes.repository.UserRepository;
import com.bytes.repository.WatchedVideoRepository;
import com.bytes.service.BytesService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import bytes.com.exception.ResourceNotFoundException;

/**
 * Service implementation for handling byte-related operations.
 */
@Service
public class BytesServiceImpl implements BytesService {

	private final BytesRepository bytesRepository;
	private final UserRepository userRepository;
	private final MetadataRepository metadataRepository;
	private final WatchedVideoRepository watchedVideoRepository;
	private Metadata metadata;

	@Autowired
	public BytesServiceImpl(BytesRepository bytesRepository, UserRepository userRepository,
			MetadataRepository metadataRepository, WatchedVideoRepository watchedVideoRepository) {
		this.bytesRepository = bytesRepository;
		this.userRepository = userRepository;
		this.metadataRepository = metadataRepository;
		this.watchedVideoRepository = watchedVideoRepository;
		this.metadata = new Metadata();
	}

	/**
	 * Uploads a file along with its metadata.
	 *
	 * @param file        The file to be uploaded.
	 * @param email       The email of the user uploading the file.
	 * @param description The description of the file.
	 * @param latitude    The latitude of the file.
	 * @param longitude   The longitude of the file.
	 * @return ResponseBytesMessage indicating the status of the upload.
	 */
	@Override
	@Transactional
	public ResponseBytesMessage uploadFile(File file, String email, String description, String latitude,
			String longitude) {
		try {
			if (file == null || StringUtils.isEmpty(email) || StringUtils.isEmpty(description)
					|| StringUtils.isEmpty(latitude) || StringUtils.isEmpty(longitude)) {
				return new ResponseBytesMessage("Please provide file, email, description, latitude, and longitude.");
			}

			User user = userRepository.findByEmail(email);
			if (user == null) {
				return new ResponseBytesMessage("User not found for email: " + email);
			}

			long videoDurationInSeconds = getVideoDuration(file); // Getting video duration from the provided file
			if (videoDurationInSeconds > 30) {
				return new ResponseBytesMessage("Video duration is more than 30 seconds");
			}

			String formattedVideoDuration = convertSecondsToHHmmss(videoDurationInSeconds);
			String fileName = file.getName();

			Bytes bytes = new Bytes(fileName, user, Files.probeContentType(file.toPath()),
					Files.readAllBytes(file.toPath()));
			bytes.setUpdateDateTime(LocalDateTime.now());
			bytes.setVideoDuration(formattedVideoDuration);
			bytes.setDescription(description);
			bytes.setVideo("Y");
			bytes.setLatitude(latitude);
			bytes.setLongitude(longitude);
			// Save Bytes entity
			bytesRepository.save(bytes);

			return new ResponseBytesMessage("Uploaded the file successfully: " + fileName);
		} catch (IOException e) {
			return new ResponseBytesMessage("Could not upload the file: " + file.getName() + " - " + e.getMessage());
		}
	}

	/**
	 * Converts seconds to HH:mm:ss format.
	 *
	 * @param seconds The duration in seconds.
	 * @return The duration in HH:mm:ss format.
	 */
	private String convertSecondsToHHmmss(long seconds) {
		long hour = seconds / 3600;
		long minute = (seconds % 3600) / 60;
		long second = seconds % 60;
		return String.format("%02d:%02d:%02d", hour, minute, second);
	}

	/**
	 * Retrieves the duration of a video file in seconds.
	 *
	 * @param file The video file.
	 * @return The duration of the video in seconds.
	 * @throws IOException If an I/O error occurs.
	 */
	public long getVideoDuration(File file) throws IOException {
		try {
			ProcessBuilder builder = new ProcessBuilder();
			builder.command("ffmpeg", "-i", file.getAbsolutePath());

			Process process = builder.start();
			process.waitFor();

			String output = new String(process.getErrorStream().readAllBytes());
			System.out.println("FFmpeg Command Output: " + output);

			if (output.contains("Duration:")) {
				String durationLine = output.substring(output.indexOf("Duration:"));
				String[] durationParts = durationLine.split(",")[0].split(" ")[1].split(":");
				double durationInSeconds = TimeUnit.HOURS.toSeconds(Long.parseLong(durationParts[0]))
						+ TimeUnit.MINUTES.toSeconds(Long.parseLong(durationParts[1]))
						+ Double.parseDouble(durationParts[2]);
				return (long) durationInSeconds;
			} else {
				throw new IOException("Failed to get video duration");
			}
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
			throw new IOException("Error executing FFmpeg command: " + e.getMessage());
		}
	}

	/**
	 * Posts a comment on a video file.
	 *
	 * @param id        The ID of the video file.
	 * @param firstName The first name of the commenter.
	 * @param lastName  The last name of the commenter.
	 * @param comment   The comment text.
	 */
	@Override
	public void postComment(String id, String firstName, String lastName, String comment) {
		Bytes bytes = bytesRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Video file not found with id: " + id));

		Metadata metadata = bytes.getMetadata();
		if (metadata == null) {
			metadata = new Metadata();
			bytes.setMetadata(metadata);
		}

		ObjectMapper mapper = new ObjectMapper();

		ArrayNode existingComments;
		String existingCommentsStr = metadata.getComments();
		if (existingCommentsStr == null || existingCommentsStr.isEmpty()) {
			existingComments = mapper.createArrayNode();
		} else {
			try {
				existingComments = (ArrayNode) mapper.readTree(existingCommentsStr);
			} catch (IOException e) {
				throw new RuntimeException("Failed to parse existing comments JSON", e);
			}
		}

		ObjectNode newComment = mapper.createObjectNode();
		newComment.put("firstName", firstName);
		newComment.put("lastName", lastName);
		newComment.put("comment", comment);

		existingComments.add(newComment);

		metadata.setComments(existingComments.toString());

		bytesRepository.save(bytes);
	}

	/**
	 * Retrieves JSON representation of comments for a video file.
	 *
	 * @param id The ID of the video file.
	 * @return JSON representation of comments.
	 */
	public String getCommentsJson(String id) {
		Bytes bytes = bytesRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Video file not found with id: " + id));

		Metadata metadata = bytes.getMetadata();

		ObjectMapper mapper = new ObjectMapper();
		ObjectNode response = mapper.createObjectNode();

		if (metadata != null && metadata.getComments() != null) {
			try {
				ArrayNode commentsArray = (ArrayNode) mapper.readTree(metadata.getComments());

				response.set("comments", commentsArray);
			} catch (Exception e) {
				throw new RuntimeException("Failed to parse comments JSON", e);
			}
		}

		return response.toString();
	}

	/**
	 * Retrieves a file by its ID.
	 *
	 * @param id The ID of the file.
	 * @return The file.
	 */
	@Override
	public Bytes getFile(String id) {
		return bytesRepository.findById(id).orElse(null);
	}

	/**
	 * Retrieves a file by its ID.
	 *
	 * @param id The ID of the file.
	 * @return The file.
	 */
	@Override
	public Optional<Bytes> getFileById(String id) {
		return bytesRepository.findById(id);
	}

	/**
	 * Retrieves all files uploaded by a user with the specified email.
	 *
	 * @param email The email of the user.
	 * @return List of files uploaded by the user.
	 */
	@Override
	public List<Bytes> getFilesByEmail(String email) {
		return bytesRepository.findByUserEmail(email);
	}

	/**
	 * Retrieves all files.
	 *
	 * @return List of all files.
	 */
	@Override
	public List<Bytes> getAllFiles() {
		List<Bytes> files = bytesRepository.findAll();
		return new ArrayList<>(files);
	}

	/**
	 * Retrieves metadata details for a file by its ID.
	 *
	 * @param id The ID of the file.
	 * @return Metadata details for the file.
	 */
	@Override
	public ResponseMetadata getFileDetailsById(String id) {
		Optional<Bytes> dbFileOptional = bytesRepository.findById(id);

		if (dbFileOptional.isPresent()) {
			Bytes bytes = dbFileOptional.get();
			Metadata metadata = bytes.getMetadata();
			int likeCount = metadata.getLikeCount();
			int dislikeCount = metadata.getDislikeCount();
			int viewsCount = metadata.getViewsCount();
			String comment = metadata.getComments();
			String share = metadata.getShare();
			String download = metadata.getDownload();
			String email = metadata.getemail();
			return new ResponseMetadata(likeCount, dislikeCount, viewsCount, comment, email, share, download);
		} else {
			throw new ResourceNotFoundException("File not found for ID: " + id);
		}
	}

	/**
	 * Updates details of a file.
	 *
	 * @param id             The ID of the file.
	 * @param newFileName    The new file name.
	 * @param newDescription The new description.
	 * @return The updated file.
	 */
	@Override
	public Bytes updateFileDetails(String id, String newFileName, String newDescription) {
		Bytes bytes = bytesRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Video file not found with id: " + id));

		if (newFileName != null) {
			bytes.setName(newFileName);
		}
		if (newDescription != null) {
			bytes.setDescription(newDescription);
		}

		bytes.setUpdateDateTime(LocalDateTime.now());

		return bytesRepository.save(bytes);
	}

	/**
	 * Updates metadata details of a file.
	 *
	 * @param id      The ID of the file.
	 * @param request The metadata details.
	 * @return A message indicating the success of the update.
	 */
	@Transactional
	public String updateFileDetails(String id, ResponseMetadata request) {
		Optional<Bytes> dbFileOptional = bytesRepository.findById(id);

		if (dbFileOptional.isPresent()) {
			Bytes bytes = dbFileOptional.get();

			Metadata metadata = bytes.getMetadata();

			if (metadata == null) {
				throw new ResourceNotFoundException("Metadata not found for Bytes ID: " + id);
			}

			if (request.getLikeCount() >= 0) {
				metadata.setLikeCount(request.getLikeCount());
			}
			if (request.getDislikeCount() >= 0) {
				metadata.setDislikeCount(request.getDislikeCount());
			}
			if (request.getViewsCount() >= 0) {
				metadata.setViewsCount(request.getViewsCount());
			}
			if (request.getComment() != null) {
				metadata.setComments(request.getComment());
			}
			if (request.getemail() != null) {
				metadata.setemail(request.getemail());
			}
			if (request.getShare() != null) {
				metadata.setShare(request.getShare());
			}
			if (request.getDownload() != null) {
				metadata.setDownload(request.getDownload());
			}
			metadataRepository.save(metadata);

			return "File details updated successfully for Bytes ID: " + id;
		} else {
			throw new ResourceNotFoundException("Bytes not found for ID: " + id);
		}
	}

	/**
	 * Retrieves the email of the uploader for a video.
	 *
	 * @param videoId The ID of the video.
	 * @return The email of the uploader.
	 */
	@Override
	public String getUploaderIdForVideo(String videoId) {
		Bytes bytes = bytesRepository.findById(videoId).orElse(null);
		if (bytes != null) {
			return bytes.getUser().getEmail();
		} else {
			// Handle the case when the video with the given ID is not found
			throw new ResourceNotFoundException("Video not found with ID: " + videoId);
		}
	}

	/**
	 * Validates whether the logged-in user's email matches the video uploader's
	 * email.
	 *
	 * @param loggedInUserEmail  The email of the logged-in user.
	 * @param videoUploaderEmail The email of the video uploader.
	 * @return True if the emails match, false otherwise.
	 */
	@Override
	public boolean validateEmail(String loggedInUserEmail, String videoUploaderEmail) {
		// Compare the logged-in user's email with the video uploader's email
		return loggedInUserEmail.equals(videoUploaderEmail);
	}

	/**
	 * Searches files by their description using a keyword.
	 *
	 * @param keyword The keyword to search for in file descriptions.
	 * @return List of files matching the keyword.
	 */
	public List<ResponseGetall> searchFilesByDescription(String keyword) {
		return getAllFiles().stream().filter(dbFile -> {
			String description = dbFile.getDescription();
			return description != null && description.toLowerCase().contains(keyword.toLowerCase());
		}).map(dbFile -> {
			String fileDownloadUri;
			try {
				fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath().path("/api/files/download/")
						.path(String.valueOf(dbFile.getId())) // Assuming getId() returns the file ID
						.toUriString();
			} catch (IllegalStateException e) {
				fileDownloadUri = "/api/files/download/" + dbFile.getId(); // Fallback without servlet context path
			}
			return new ResponseGetall(dbFile.getName(), fileDownloadUri, dbFile.getDescription());
		}).collect(Collectors.toList());
	}

	/**
	 * Downloads a file by its ID.
	 *
	 * @param fileId The ID of the file to download.
	 * @return ResponseEntity with the file as a resource.
	 */
	@Override
	public ResponseEntity<Resource> downloadFile(String fileId) {
		Optional<Bytes> dbFileOptional = bytesRepository.findById(fileId);

		if (dbFileOptional.isPresent()) {
			Bytes dbFile = dbFileOptional.get();
			ByteArrayResource resource = new ByteArrayResource(dbFile.getData());
			MediaType mediaType = determineMediaType(dbFile.getName());

			return ResponseEntity.ok()
					.header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + dbFile.getName() + "\"")
					.contentLength(dbFile.getData().length).contentType(mediaType).body(resource);
		} else {
			return ResponseEntity.notFound().build();
		}
	}

	/**
	 * Determines the media type of a file based on its extension.
	 *
	 * @param fileName The name of the file.
	 * @return The media type.
	 */
	private MediaType determineMediaType(String fileName) {
		String fileExtension = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();

		switch (fileExtension) {
		case "mp4":
			return MediaType.parseMediaType("video/mp4");
		case "webm":
			return MediaType.parseMediaType("video/webm");
		default:
			return MediaType.APPLICATION_OCTET_STREAM;
		}
	}

	/**
	 * Saves a file.
	 *
	 * @param bytes The file to save.
	 */
	@Override
	public void save(Bytes bytes) {
		bytesRepository.save(bytes);
	}

	/**
	 * Validates whether the provided email matches the logged-in user's email.
	 *
	 * @param email The email to validate.
	 * @return True if the email matches the logged-in user's email, false
	 *         otherwise.
	 */
	@Override
	public boolean validateEmail(String email) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		String loggedInUserEmail = authentication.getName();
		return email.equals(loggedInUserEmail);
	}

	/**
	 * Deletes a file by its ID.
	 *
	 * @param id The ID of the file to delete.
	 */
	@Override
	@Transactional
	public void deleteFile(String id) {
		try {
			bytesRepository.deleteById(id);
		} catch (EmptyResultDataAccessException ex) {
			throw new ResourceNotFoundException("File not found with ID: " + id);
		} catch (DataAccessException ex) {
			throw new RuntimeException("Error deleting file with ID: " + id, ex);
		}
	}

	/**
	 * Deletes all files.
	 */
	@Override
	@Transactional
	public void deleteAllFiles() {
		bytesRepository.deleteAll();
	}

	/**
	 * Likes a file.
	 *
	 * @param id    The ID of the file.
	 * @param email The email of the user liking the file.
	 */
	@Override
	public void likeFile(String id, String email) {
		Optional<Bytes> optionalBytes = bytesRepository.findById(id);

		if (optionalBytes.isPresent()) {
			Bytes bytes = optionalBytes.get();
			Metadata metadata = bytes.getMetadata();
			if (metadata == null) {
				metadata = new Metadata();
				metadata.setBytes(bytes);
				metadata.setLikedUsers("");
				metadata.setLikeCount(0);
				metadata.setDislikedUsers("");
				metadata.setDislikeCount(0);
				bytes.setMetadata(metadata);
			}

			if (metadata.getLikedUsers().contains(email)) {
				metadata.setLikeCount(metadata.getLikeCount() - 1);
				metadata.setLikedUsers(
						metadata.getLikedUsers().replace(email + ",", "").replace("," + email, "").replace(email, ""));
			} else {
				if (metadata.getDislikedUsers().contains(email)) {
					metadata.setDislikeCount(metadata.getDislikeCount() - 1);
					metadata.setDislikedUsers(metadata.getDislikedUsers().replace(email + ",", "")
							.replace("," + email, "").replace(email, ""));
				}
				metadata.setLikeCount(metadata.getLikeCount() + 1);
				if (metadata.getLikedUsers().isEmpty()) {
					metadata.setLikedUsers(email);
				} else {
					metadata.setLikedUsers(metadata.getLikedUsers() + "," + email);
				}
			}
			metadataRepository.save(metadata);
		} else {
			throw new ResourceNotFoundException("File not found with ID: " + id);
		}
	}

	/**
	 * Dislikes a file.
	 *
	 * @param id    The ID of the file.
	 * @param email The email of the user disliking the file.
	 */
	@Override
	public void dislikeFile(String id, String email) {
		Optional<Bytes> optionalBytes = bytesRepository.findById(id);

		if (optionalBytes.isPresent()) {
			Bytes bytes = optionalBytes.get();
			Metadata metadata = bytes.getMetadata();
			if (metadata == null) {
				metadata = new Metadata();
				metadata.setBytes(bytes);
				metadata.setLikedUsers("");
				metadata.setLikeCount(0);
				metadata.setDislikedUsers("");
				metadata.setDislikeCount(0);
				bytes.setMetadata(metadata);
			}

			if (metadata.getDislikedUsers().contains(email)) {
				metadata.setDislikeCount(metadata.getDislikeCount() - 1);
				metadata.setDislikedUsers(metadata.getDislikedUsers().replace(email + ",", "").replace("," + email, "")
						.replace(email, ""));
			} else {
				if (metadata.getLikedUsers().contains(email)) {
					metadata.setLikeCount(metadata.getLikeCount() - 1);
					metadata.setLikedUsers(metadata.getLikedUsers().replace(email + ",", "").replace("," + email, "")
							.replace(email, ""));
				}
				metadata.setDislikeCount(metadata.getDislikeCount() + 1);
				if (metadata.getDislikedUsers().isEmpty()) {
					metadata.setDislikedUsers(email);
				} else {
					metadata.setDislikedUsers(metadata.getDislikedUsers() + "," + email);
				}
			}
			metadataRepository.save(metadata);
		} else {
			throw new ResourceNotFoundException("File not found with ID: " + id);
		}
	}

	/**
	 * Retrieves the ID of the uploader based on their email.
	 * 
	 * @param email The email of the uploader.
	 * @return The ID of the uploader.
	 * @throws ResourceNotFoundException If no user is found with the provided
	 *                                   email.
	 */
	@Override
	public String getUploaderIdByEmail(String email) {
		User user = userRepository.findByEmail(email);
		if (user != null) {
			return user.getId().toString();
		} else {
			throw new ResourceNotFoundException("User not found for email: " + email);
		}
	}

	/**
	 * Adds a watched video record for a user.
	 *
	 * @param userId  The ID of the user.
	 * @param videoId The ID of the video.
	 */
	@Override
	public void addWatchedVideo(Long userId, String videoId) { // change Long to String for videoId
		User user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("User not found"));
		Bytes video = bytesRepository.findById(videoId)
				.orElseThrow(() -> new IllegalArgumentException("Video not found"));
		WatchedVideo watchedVideo = new WatchedVideo(user, video, LocalDateTime.now());
		watchedVideoRepository.save(watchedVideo);
	}

	public List<Bytes> getWatchedVideos(Long userId) {
		List<WatchedVideo> watchedVideos = watchedVideoRepository.findByUserId(userId);
		return watchedVideos.stream().map(WatchedVideo::getVideo).collect(Collectors.toList());
	}

	/**
	 * Suggests videos to a user based on their watched history.
	 *
	 * @param userId The ID of the user.
	 * @return List of suggested videos.
	 */
	@Override
	public List<ResponseFile> suggestVideos(Long userId) {
		List<WatchedVideo> watchedVideos = watchedVideoRepository.findByUserId(userId);
		// Step 1: Collect keywords
		List<String> keywords = watchedVideos.stream()
				.map(watchedVideo -> watchedVideo.getVideo().getDescription().split("\\s+")).flatMap(Arrays::stream)
				.distinct().collect(Collectors.toList());
		// Step 2: Collect videos from keywords
		List<Bytes> suggestions = keywords.stream().map(keyword -> bytesRepository.findByDescriptionContaining(keyword))
				.flatMap(List::stream).distinct().collect(Collectors.toList());
		// Step 3: Order suggestions by creation date
		suggestions.sort(Comparator.comparing(Bytes::getCreatedDateTime));
		// Step 4: Return video id, name, url, description, and creation date
		return suggestions.stream().map(video -> {
			String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath().path("/api/files/download/")
					.path(video.getId()).toUriString();
			return new ResponseFile(video.getId(), video.getName(), fileDownloadUri, video.getDescription(),
					video.getCreatedDateTime());
		}).sorted(Comparator.comparing(ResponseFile::getCreatedDateTime).reversed()).collect(Collectors.toList());
	}

}

package com.bytes.service.impl;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.ArrayList;
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
import com.bytes.message.ResponseGetall;
import com.bytes.message.ResponseMetadata;
import com.bytes.model.Bytes;
import com.bytes.model.Metadata;
import com.bytes.model.User;
import com.bytes.repository.BytesRepository;
import com.bytes.repository.MetadataRepository;
import com.bytes.repository.UserRepository;
import com.bytes.service.BytesService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import bytes.exception.ResourceNotFoundException;

@Service
public class BytesServiceImpl implements BytesService {

	private final BytesRepository bytesRepository;
	private final UserRepository userRepository;
	private final MetadataRepository metadataRepository;
	private Metadata metadata; // Assuming metadata is an instance variable of your class

	@Autowired
	public BytesServiceImpl(BytesRepository bytesRepository, UserRepository userRepository,
			MetadataRepository metadataRepository) {
		this.bytesRepository = bytesRepository;
		this.userRepository = userRepository;
		this.metadataRepository = metadataRepository;
		this.metadata = new Metadata(); // Initialize metadata
	}

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
			bytes.setdescription(description);
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

	private String convertSecondsToHHmmss(long seconds) {
		long hour = seconds / 3600;
		long minute = (seconds % 3600) / 60;
		long second = seconds % 60;
		return String.format("%02d:%02d:%02d", hour, minute, second);
	}

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

	@Override
	public Bytes getFile(String id) {
		return bytesRepository.findById(id).orElse(null);
	}

	@Override
	public Optional<Bytes> getFileById(String id) {
		return bytesRepository.findById(id);
	}

	@Override
	public List<Bytes> getFilesByEmail(String email) {
		return bytesRepository.findByUserEmail(email);
	}

	@Override
	public List<Bytes> getAllFiles() {
		List<Bytes> files = bytesRepository.findAll();
		return new ArrayList<>(files);
	}

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

	@Override
	public Bytes updateFileDetails(String id, String newFileName, String newDescription) {
		Bytes bytes = bytesRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Video file not found with id: " + id));

		if (newFileName != null) {
			bytes.setName(newFileName);
		}
		if (newDescription != null) {
			bytes.setdescription(newDescription);
		}

		bytes.setUpdateDateTime(LocalDateTime.now());

		return bytesRepository.save(bytes);
	}

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

	@Override
	public boolean validateEmail(String loggedInUserEmail, String videoUploaderEmail) {
		// Compare the logged-in user's email with the video uploader's email
		return loggedInUserEmail.equals(videoUploaderEmail);
	}

	public List<ResponseGetall> searchFilesByDescription(String keyword) {
		return getAllFiles().stream().filter(dbFile -> {
			String description = dbFile.getdescription();
			return description != null && description.toLowerCase().contains(keyword.toLowerCase());
		}).map(dbFile -> {
			String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath().path("/api/files/download/")
					.path(String.valueOf(dbFile.getId())) // Assuming getId() returns the file ID
					.toUriString();
			return new ResponseGetall(dbFile.getName(), fileDownloadUri, dbFile.getdescription());
		}).collect(Collectors.toList());
	}

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

	@Override
	public void save(Bytes bytes) {
		bytesRepository.save(bytes);
	}

	@Override
	public boolean validateEmail(String email) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		String loggedInUserEmail = authentication.getName();
		return email.equals(loggedInUserEmail);
	}

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

	@Override
	@Transactional
	public void deleteAllFiles() {
		bytesRepository.deleteAll();
	}

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

}

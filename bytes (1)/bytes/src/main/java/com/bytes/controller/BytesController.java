package com.bytes.controller;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.bytes.message.ResponseBytesFile;
import com.bytes.message.ResponseBytesMessage;
import com.bytes.message.ResponseFile;
import com.bytes.message.ResponseGetall;
import com.bytes.model.Bytes;
import com.bytes.model.User;
import com.bytes.moderator.RekognitionService;
import com.bytes.repository.BytesRepository;
import com.bytes.repository.MetadataRepository;
import com.bytes.repository.UserRepository;
import com.bytes.service.BytesService;

import bytes.com.exception.ResourceNotFoundException;

@RestController
@CrossOrigin("http://localhost:8081")
public class BytesController {

	private final BytesService bytesService;
	private final HttpServletRequest httpServletRequest;
	private final UserRepository userRepository;
	private final MetadataRepository metadataRepository;
	private final BytesRepository bytesRepository;
	private static final Logger logger = LoggerFactory.getLogger(BytesController.class);
	@Autowired
	private RekognitionService rekognitionService;

	@Autowired
	public BytesController(BytesService bytesService, HttpServletRequest httpServletRequest,
			UserRepository userRepository, MetadataRepository metadataRepository, BytesRepository bytesRepository) {
		this.bytesService = bytesService;
		this.httpServletRequest = httpServletRequest;
		this.userRepository = userRepository;
		this.metadataRepository = metadataRepository;
		this.bytesRepository = bytesRepository;
	}

	// Retrieve video by ID
	@GetMapping("/api/bytes/{id}")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<Resource> getFile(@PathVariable String id) {
		logger.info("Fetching file with ID: {}", id);
		try {
			Bytes bytes = bytesService.getFile(id);
			if (bytes == null) {
				logger.warn("File not found with ID: {}", id);
				return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
			}

			ByteArrayResource resource = new ByteArrayResource(bytes.getData());
			HttpHeaders headers = new HttpHeaders();
			headers.setContentDispositionFormData("attachment", bytes.getName());
			logger.info("File fetched successfully with ID: {}", id);
			return ResponseEntity.ok().headers(headers).contentType(MediaType.parseMediaType("video/mp4"))
					.contentLength(bytes.getData().length).body(resource);
		} catch (Exception e) {
//			e.printStackTrace();
			logger.error("Error fetching file with ID: {}", id, e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
		}
	}

	// Retrieve all video
	@GetMapping("/api/bytes")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<List<ResponseBytesFile>> getAllFiles(Principal principal) {
		logger.info("Fetching all files for user: {}", principal.getName());
		try {
			String email = principal.getName();
			User user = userRepository.findByEmail(email);
			if (user == null) {
				throw new ResourceNotFoundException("User not found with email: " + email);
			}
			String firstName = user.getFirstName();
			String lastName = user.getLastName();

			List<ResponseBytesFile> files = bytesService.getAllFiles().stream().map(dbFile -> {
				String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
						.path("/api/files/download/").path(dbFile.getId()).toUriString();

				return new ResponseBytesFile(dbFile.getId(), dbFile.getName(), fileDownloadUri, dbFile.getDescription(),
						firstName, lastName);
			}).collect(Collectors.toList());
			logger.info("All files fetched successfully for user: {}", email);
			return ResponseEntity.ok().body(files);
		} catch (Exception e) {
//			e.printStackTrace();
			logger.error("Error fetching all files for user: {}", principal.getName(), e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
		}
	}

	// Search video by description keyword
	@GetMapping("api/search/{keyword}")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<List<ResponseGetall>> searchFilesByDescription(@PathVariable String keyword) {
		logger.info("Searching files by description keyword: {}", keyword);
		try {
			List<ResponseGetall> matchingFiles = bytesService.searchFilesByDescription(keyword);

			if (matchingFiles.isEmpty()) {
				logger.warn("No files found with description keyword: {}", keyword);
				return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
			}

			logger.info("Files found with description keyword: {}", keyword);
			return ResponseEntity.ok(matchingFiles);
		} catch (Exception e) {
//			e.printStackTrace();
			logger.error("Error searching files by description keyword: {}", keyword, e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
		}
	}

	// Get video by email
	@GetMapping("/api/bytes/byEmail")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<List<ResponseBytesFile>> getFilesByEmail(@RequestParam("email") String email) {
		logger.info("Fetching files by email: {}", email);
		try {
			List<ResponseBytesFile> files = bytesService.getFilesByEmail(email).stream().map(dbFile -> {
				String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
						.path("/api/files/download/").path(dbFile.getId()).toUriString();
				User user = userRepository.findByEmail(email);
				String firstName = "";
				String lastName = "";
				if (user != null) {
					firstName = user.getFirstName();
					lastName = user.getLastName();
				}
				return new ResponseBytesFile(dbFile.getId(), dbFile.getName(), fileDownloadUri, dbFile.getDescription(),
						firstName, lastName);

			}).collect(Collectors.toList());

			logger.info("Files fetched successfully by email: {}", email);
			return ResponseEntity.ok().body(files);
		} catch (Exception e) {
//			e.printStackTrace();
			logger.error("Error fetching files by email: {}", email, e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
		}
	}

	// Download video by ID
	@GetMapping("/api/files/download/{fileId}")
	@PreAuthorize("permitAll()")
	public ResponseEntity<Resource> downloadFile(@PathVariable String fileId) {
		logger.info("Downloading file with ID: {}", fileId);
		return bytesService.downloadFile(fileId);
	}

	// Get comments for a video
	@GetMapping("/api/getcomments/{id}")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<String> getComments(@PathVariable String id) {
		logger.info("Fetching comments for file with ID: {}", id);
		try {
			String commentsJson = bytesService.getCommentsJson(id);
			logger.info("Comments fetched successfully for file with ID: {}", id);
			return ResponseEntity.ok().body(commentsJson);
		} catch (ResourceNotFoundException e) {
			logger.warn("Video file not found with ID: {}", id);
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Video file not found with id: " + id);
		}
	}

	@GetMapping("/frames/{filename}")
	public ResponseEntity<Resource> getFrame(@PathVariable String filename) {
		try {
			String outputDir = System.getProperty("java.io.tmpdir"); // Directory where frames are saved
			Path filePath = Paths.get(outputDir, filename);
			Resource resource = new UrlResource(filePath.toUri());

			if (resource.exists() || resource.isReadable()) {
				return ResponseEntity.ok()
						.header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
						.body(resource);
			} else {
				return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
		}
	}

	// Retrieve watched video
	@GetMapping("/watched")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<List<Bytes>> getWatchedVideos(@RequestParam("userId") Long userId) {
		try {
			List<Bytes> watchedVideos = bytesService.getWatchedVideos(userId);
			return ResponseEntity.ok(watchedVideos);
		} catch (Exception e) {
			return ResponseEntity.status(500).body(null);
		}
	}

	// get suggestions based on watch history
	@GetMapping("/suggestions")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<List<ResponseFile>> suggestVideos(@RequestParam("userId") Long userId) {
		try {
			List<ResponseFile> suggestedVideos = bytesService.suggestVideos(userId);
			return ResponseEntity.ok(suggestedVideos);
		} catch (Exception e) {
			return ResponseEntity.status(500).body(null);
		}
	}

	// add to watch history
	@PostMapping("/watch")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<String> addWatchedVideo(@RequestParam("userId") Long userId,
			@RequestParam("videoId") String videoId) {
		try {
			bytesService.addWatchedVideo(userId, videoId);
			return ResponseEntity.ok("Video added to watched list successfully.");
		} catch (Exception e) {
			return ResponseEntity.status(500).body("Error adding video to watched list.");
		}
	}

	// upload video
	@PostMapping("/upload")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<ResponseBytesMessage> uploadFile(@RequestParam("file") MultipartFile file,
			@RequestParam("email") String email, @RequestParam("description") String description,
			@RequestParam("latitude") String latitude, @RequestParam("longitude") String longitude) {
		logger.info("Uploading file with email: {}", email);

		// Check if the provided email matches the one in the authentication token
		if (!bytesService.validateEmail(email)) {
			logger.warn("Access Denied: Email in token does not match provided email");
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
					new ResponseBytesMessage("Access Denied: Email in token does not match provided email", null));
		}

		File tempFile = null;
		try {
			tempFile = File.createTempFile("temp", file.getOriginalFilename());
			file.transferTo(tempFile);

			// Check video duration
			long videoDuration = bytesService.getVideoDuration(tempFile);
			if (videoDuration > 30) {
				logger.warn("Video duration is more than 30 seconds for file: {}", file.getOriginalFilename());
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseBytesMessage(
						"Video duration is more than 30 seconds. Please upload a video with duration less than or equal to 30 seconds.",
						null));
			}

			// Moderate video content
//			boolean isSafe = rekognitionService.moderateVideo(tempFile);
//
//			if (!isSafe) {
//				logger.warn("Video contains inappropriate content: {}", file.getOriginalFilename());
//				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseBytesMessage(
//						"Video contains inappropriate content. Please upload a safe video.", null));
//			}

			// Process the uploaded file
			ResponseBytesMessage response = bytesService.uploadFile(tempFile, email, description, latitude, longitude);
			HttpStatus status = response.getMessage().startsWith("Uploaded") ? HttpStatus.OK : HttpStatus.BAD_REQUEST;
			logger.info("File uploaded successfully: {}", file.getOriginalFilename());

			return ResponseEntity.status(status).body(response);
		} catch (IOException e) {
			logger.error("Failed to process video file: {}", file.getOriginalFilename(), e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new ResponseBytesMessage("Failed to process video file", null));
		} finally {
			if (tempFile != null && tempFile.exists()) {
				tempFile.delete();
			}
		}
	}

	// Post a comment
	@PostMapping("/api/comment/{id}")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<String> postComment(@PathVariable String id, @RequestParam("comment") String comment) {
		logger.info("Posting comment for file with ID: {}", id);
		try {
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			String email = authentication.getName();
			User user = userRepository.findByEmail(email);
			if (user == null) {
				throw new ResourceNotFoundException("User not found with email: " + email);
			}
			String firstName = user.getFirstName();
			String lastName = user.getLastName();
			bytesService.postComment(id, firstName, lastName, comment);
			logger.info("Comment posted successfully for file with ID: {}", id);
			return ResponseEntity.ok("Comment posted successfully.");
		} catch (ResourceNotFoundException e) {
			logger.warn("User not found with email while posting comment: {}", e.getMessage());
			return ResponseEntity.notFound().build();
		} catch (Exception e) {
//			e.printStackTrace();
			logger.error("Failed to post comment for file with ID: {}", id, e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to post comment.");
		}
	}

	// Like a video
	@PutMapping("/api/like/{id}")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<String> likeFile(@PathVariable String id) {
		logger.info("Liking file with ID: {}", id);
		try {
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			String email = authentication.getName();
			bytesService.likeFile(id, email);
			logger.info("File liked successfully with ID: {}", id);
			return ResponseEntity.ok("Liked successfully.");
		} catch (ResourceNotFoundException e) {
			logger.warn("File not found with ID while liking: {}", id);
			return ResponseEntity.notFound().build();
		} catch (Exception e) {
//				e.printStackTrace();
			logger.error("Failed to like file with ID: {}", id, e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to like.");
		}
	}

	// Dislike a video
	@PutMapping("/api/dislike/{id}")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<String> dislikeFile(@PathVariable String id) {
		try {
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			String email = authentication.getName();
			bytesService.dislikeFile(id, email);
			logger.info("File disliked successfully with ID: {}", id);
			return ResponseEntity.ok("Disliked successfully.");
		} catch (ResourceNotFoundException e) {
			logger.warn("File not found with ID while disliking: {}", id);
			return ResponseEntity.notFound().build();
		} catch (Exception e) {
//			e.printStackTrace();
			logger.error("Failed to dislike file with ID: {}", id, e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to dislike.");
		}
	}

	// Update video details
	@PutMapping("/api/update-details/{id}")
	public ResponseEntity<String> updateFileDetails(@PathVariable String id, @RequestBody Map<String, String> request) {
		logger.info("Updating details for file with ID: {}", id);
		try {
			String name = request.get("name");
			String description = request.get("description");

			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			String loggedInUserEmail = authentication.getName();

			String videoUploaderEmail = bytesService.getUploaderIdForVideo(id);

			if (!bytesService.validateEmail(loggedInUserEmail, videoUploaderEmail)) {
				logger.warn("Access denied for updating file details. User: {}, Uploader: {}", loggedInUserEmail,
						videoUploaderEmail);
				return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied");
			}
			Bytes updatedFile = bytesService.updateFileDetails(id, name, description);
			logger.info("File details updated successfully for ID: {}", updatedFile.getId());
			return ResponseEntity.ok("File details updated successfully for ID: " + updatedFile.getId());
		} catch (ResourceNotFoundException e) {
			logger.warn("File not found with ID: {} for updating details", id);
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
		} catch (Exception e) {
			logger.error("Failed to update file details for ID: {}", id, e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to update file details");
		}
	}

	// Delete video by ID
	@DeleteMapping("/api/delete-bytes/{id}")
	public ResponseEntity<String> deleteFile(@PathVariable String id) {
		logger.info("Deleting file with ID: {}", id);
		try {
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			String loggedInUserEmail = authentication.getName();
			String videoUploaderEmail = bytesService.getUploaderIdForVideo(id);
			if (!bytesService.validateEmail(loggedInUserEmail, videoUploaderEmail)) {
				logger.warn("Access denied for deleting file. User: {}, Uploader: {}", loggedInUserEmail,
						videoUploaderEmail);
				return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied");
			}
			bytesService.deleteFile(id);
			logger.info("File deleted successfully with ID: {}", id);
			return ResponseEntity.noContent().build();
		} catch (ResourceNotFoundException e) {
			logger.warn("File not found with ID: {} for deletion", id);
			return ResponseEntity.notFound().build();
		} catch (Exception e) {
//			e.printStackTrace();
			logger.error("Failed to delete file with ID: {}", id, e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("An internal server error occurred while processing the request.");
		}
	}

	// Delete all video
	@DeleteMapping("/bytes")
	public ResponseEntity<String> deleteAllFiles() {
		logger.info("Deleting all files");
		try {
			bytesService.deleteAllFiles();
			logger.info("All files deleted successfully");
			return ResponseEntity.ok("All files deleted successfully");
		} catch (Exception e) {
			logger.error("Failed to delete all files", e);
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to delete files", e);
		}
	}
}
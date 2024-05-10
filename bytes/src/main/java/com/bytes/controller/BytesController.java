package com.bytes.controller;

import java.io.File;
import java.io.IOException;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
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
import com.bytes.message.ResponseGetall;
import com.bytes.model.Bytes;
import com.bytes.model.User;
import com.bytes.repository.BytesRepository;
import com.bytes.repository.MetadataRepository;
import com.bytes.repository.UserRepository;
import com.bytes.service.BytesService;

import bytes.exception.ResourceNotFoundException;

@RestController
@CrossOrigin("http://localhost:8081")
public class BytesController {

	@Autowired
	private BytesService bytesService;

	private final HttpServletRequest httpServletRequest;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private MetadataRepository metadataRepository;

	@Autowired
	private BytesRepository bytesRepository;

	@Autowired
	public BytesController(BytesService bytesService, HttpServletRequest httpServletRequest) {
		this.bytesService = bytesService;
		this.httpServletRequest = httpServletRequest;
	}

	// Retrieve by id
	@GetMapping("/api/bytes/{id}")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<Resource> getFile(@PathVariable String id) {
		try {
			Bytes bytes = bytesService.getFile(id);
			if (bytes == null) {
				return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
			}

			ByteArrayResource resource = new ByteArrayResource(bytes.getData());
			HttpHeaders headers = new HttpHeaders();
			headers.setContentDispositionFormData("attachment", bytes.getName());

			return ResponseEntity.ok().headers(headers).contentType(MediaType.parseMediaType("video/mp4"))
					.contentLength(bytes.getData().length).body(resource);
		} catch (Exception e) {
			e.printStackTrace(); // Log the exception for debugging
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
		}
	}

	@GetMapping("/api/bytes")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<List<ResponseBytesFile>> getAllFiles(Principal principal) {
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

				return new ResponseBytesFile(dbFile.getId(), dbFile.getName(), fileDownloadUri, dbFile.getdescription(),
						firstName, lastName);
			}).collect(Collectors.toList());
			return ResponseEntity.ok().body(files);
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
		}
	}

	// Retrieve by description keyword
	@GetMapping("api/search/{keyword}")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<List<ResponseGetall>> searchFilesByDescription(@PathVariable String keyword) {
		List<ResponseGetall> matchingFiles = bytesService.searchFilesByDescription(keyword);

		if (matchingFiles.isEmpty()) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
		}

		return ResponseEntity.ok(matchingFiles);
	}

	@GetMapping("/api/bytes/byEmail")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<List<ResponseBytesFile>> getFilesByEmail(@RequestParam("email") String email) {
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
				return new ResponseBytesFile(dbFile.getId(), dbFile.getName(), fileDownloadUri, dbFile.getdescription(),
						firstName, lastName);

			}).collect(Collectors.toList());

			return ResponseEntity.ok().body(files);
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
		}
	}

	@GetMapping("/api/files/download/{fileId}")
	@PreAuthorize("permitAll()")
	public ResponseEntity<Resource> downloadFile(@PathVariable String fileId) {
		return bytesService.downloadFile(fileId);
	}

	@GetMapping("/api/getcomments/{id}")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<String> getComments(@PathVariable String id) {
		try {
			String commentsJson = bytesService.getCommentsJson(id);
			return ResponseEntity.ok().body(commentsJson);
		} catch (ResourceNotFoundException e) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Video file not found with id: " + id);
		}
	}

	@PostMapping("/api/upload")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<ResponseBytesMessage> uploadFile(@RequestParam("file") MultipartFile file,
			@RequestParam("email") String email, @RequestParam("description") String description,
			@RequestParam("latitude") String latitude, @RequestParam("longitude") String longitude) {
		if (!bytesService.validateEmail(email)) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN)
					.body(new ResponseBytesMessage("Access Denied: Email in token does not match provided email"));
		}

		try {
			File tempFile = File.createTempFile("temp", file.getOriginalFilename());
			file.transferTo(tempFile);
			long videoDuration = bytesService.getVideoDuration(tempFile);
			if (videoDuration > 30) {
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseBytesMessage(
						"Video duration is more than 30 seconds. Please upload a video with duration less than or equal to 30 seconds."));
			} else {
				ResponseBytesMessage response = bytesService.uploadFile(tempFile, email, description, latitude,
						longitude);
				HttpStatus status = response.getMessage().startsWith("Uploaded") ? HttpStatus.OK
						: HttpStatus.BAD_REQUEST;
				return ResponseEntity.status(status).body(response);
			}
		} catch (IOException e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new ResponseBytesMessage("Failed to process video file"));
		}
	}

	@PutMapping("/api/like/{id}")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<String> likeFile(@PathVariable String id) {
		try {
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			String email = authentication.getName();
			bytesService.likeFile(id, email);
			return ResponseEntity.ok("Liked successfully.");
		} catch (ResourceNotFoundException e) {
			return ResponseEntity.notFound().build();
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to like.");
		}
	}

	@PostMapping("/api/comment/{id}")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<String> postComment(@PathVariable String id, @RequestParam("comment") String comment) {
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
			return ResponseEntity.ok("Comment posted successfully.");
		} catch (ResourceNotFoundException e) {
			return ResponseEntity.notFound().build();
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to post comment.");
		}
	}

	@PutMapping("/api/dislike/{id}")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<String> dislikeFile(@PathVariable String id) {
		try {
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			String email = authentication.getName();
			bytesService.dislikeFile(id, email);
			return ResponseEntity.ok("Disliked successfully.");
		} catch (ResourceNotFoundException e) {
			return ResponseEntity.notFound().build();
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to dislike.");
		}
	}

	// update video details
	@PutMapping("/api/update-details/{id}")
	public ResponseEntity<String> updateFileDetails(@PathVariable String id, @RequestBody Map<String, String> request) {
		try {
			String name = request.get("name");
			String description = request.get("description");

			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			String loggedInUserEmail = authentication.getName();

			String videoUploaderEmail = bytesService.getUploaderIdForVideo(id);

			if (!bytesService.validateEmail(loggedInUserEmail, videoUploaderEmail)) {
				return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied");
			}
			Bytes updatedFile = bytesService.updateFileDetails(id, name, description);
			return ResponseEntity.ok("File details updated successfully for ID: " + updatedFile.getId());
		} catch (ResourceNotFoundException e) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to update file details");
		}
	}

// delete video based on id 
	@DeleteMapping("/api/delete-bytes/{id}")
	public ResponseEntity<String> deleteFile(@PathVariable String id) {
		try {
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			String loggedInUserEmail = authentication.getName();
			String videoUploaderEmail = bytesService.getUploaderIdForVideo(id);
			if (!bytesService.validateEmail(loggedInUserEmail, videoUploaderEmail)) {
				return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied");
			}
			bytesService.deleteFile(id);
			return ResponseEntity.noContent().build();
		} catch (ResourceNotFoundException e) {
			return ResponseEntity.notFound().build();
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("An internal server error occurred while processing the request.");
		}
	}

// delete all 
	@DeleteMapping("/bytes")
	public ResponseEntity<String> deleteAllFiles() {
		try {
			bytesService.deleteAllFiles();
			return ResponseEntity.ok("All files deleted successfully");
		} catch (Exception e) {
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to delete files", e);
		}
	}

}
package com.bytes.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.bytes.message.ChannelRequest;
import com.bytes.model.User;
import com.bytes.repository.UserRepository;
import com.bytes.service.UserService;
import com.bytes.service.impl.JwtTokenUtil;

@RestController
public class UserController {

	private final UserService userService;
	private final JwtTokenUtil jwtTokenUtil;
	private final AuthenticationManager authenticationManager;
	private final BCryptPasswordEncoder passwordEncoder;
	private static final Logger logger = LoggerFactory.getLogger(UserController.class);

	@Autowired
	private UserRepository userRepository;

	@Autowired
	public UserController(UserService userService, JwtTokenUtil jwtTokenUtil, BCryptPasswordEncoder passwordEncoder,
			AuthenticationManager authenticationManager) {
		this.userService = userService;
		this.jwtTokenUtil = jwtTokenUtil;
		this.authenticationManager = authenticationManager;
		this.passwordEncoder = passwordEncoder;
	}

	// Get user profile by email
	@GetMapping("/profile/{email}")
	public ResponseEntity<Map<String, Object>> getUserProfile(@PathVariable String email) {
		logger.info("Fetching profile for email: {}", email);
		User user = userService.findByEmail(email);
		if (user != null) {
			logger.info("User found for email: {}", email);
			Map<String, Object> response = new HashMap<>();
			response.put("id", user.getId());
			response.put("firstName", user.getFirstName());
			response.put("lastName", user.getLastName());
			response.put("email", user.getEmail());
			response.put("password", user.getPassword());
			response.put("latitude", user.getLatitude());
			response.put("longitude", user.getLongitude());
			response.put("profileImageUrl", "http://localhost:8090/images/" + user.getId());
			response.put("createdDateTime", user.getCreatedDateTime());
			response.put("updateDateTime", user.getUpdateDateTime());
			response.put("isActive", user.getIsActive());

			return ResponseEntity.ok(response);
		} else {
			logger.warn("User not found for email: {}", email);
			return ResponseEntity.notFound().build();
		}
	}

	// Get (admin) checks the password matches
	@GetMapping("/admin/profile/{email}/{plainTextPassword}")
	public ResponseEntity<String> getAdminUserProfile(@PathVariable String email,
			@PathVariable String plainTextPassword) {
		logger.info("Fetching admin profile for email: {}", email);
		User user = userService.findByEmail(email);
		if (user != null) {
			if (passwordEncoder.matches(plainTextPassword, user.getPassword())) {
				logger.info("Password matches for user with email: {}", email);
				return ResponseEntity.ok("Password matches for user with email: " + email);
			} else {
				logger.warn("Password does not match for user with email: {}", email);
				return ResponseEntity.ok("Password does not match for user with email: " + email);
			}
		} else {
			logger.warn("User not found for email: {}", email);
			return ResponseEntity.notFound().build();
		}
	}

	// get the profileimage by id
	@GetMapping("/images/{userId}")
	public ResponseEntity<byte[]> getProfileImage(@PathVariable Long userId) {

		byte[] image = userService.getProfileImage(userId);
		if (image != null) {
			return ResponseEntity.ok().contentType(MediaType.IMAGE_JPEG) // or MediaType.IMAGE_PNG depending on your
																			// image type
					.body(image);
		} else {
			logger.warn("Profile image not found for user with ID: {}", userId);
			return ResponseEntity.notFound().build();
		}
	}

	// Register a new user profile
	@PostMapping("/signup/create-profile")
	public ResponseEntity<String> registerUser(@Valid @RequestBody User user) {
		if (userService.existsByEmail(user.getEmail())) {
			logger.warn("Email already exists: {}", user.getEmail());
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body("Email already exists, please choose another one.");
		} else {
			userService.save(user);
			logger.info("User registered successfully: {}", user.getEmail());
			return ResponseEntity.status(HttpStatus.CREATED).body("User registered successfully");
		}
	}

	// Login user and generate JWT token
	@PostMapping("/profile/login")
	public ResponseEntity<?> loginUser(@RequestBody User user) {
		try {
			Authentication authentication = authenticationManager
					.authenticate(new UsernamePasswordAuthenticationToken(user.getEmail(), user.getPassword()));
			SecurityContextHolder.getContext().setAuthentication(authentication);
			UserDetails userDetails = (UserDetails) authentication.getPrincipal();
			User dbUser = userService.findByEmail(user.getEmail());
			if (dbUser.getIsActive() == 'N') {
				logger.warn("Inactive account login attempt: {}", user.getEmail());
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Account is inactive.");
			}
			String token = jwtTokenUtil.generateToken(userDetails);
			logger.info("User logged in successfully: {}", user.getEmail());
			return ResponseEntity.ok(token);
		} catch (AuthenticationException e) {
			logger.warn("Invalid credentials provided for login: {}", user.getEmail());
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
		} catch (Exception e) {
			logger.error("Internal server error during login: {}", e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal server error");
		}
	}

	// subscribe channel
	@PostMapping("/subscribe")
	public ResponseEntity<String> subscribe(@RequestBody ChannelRequest request) {
		String channelEmail = request.getChannelEmail();
		userService.subscribeToChannel(channelEmail);
		return ResponseEntity.ok("Subscribed successfully");
	}

	// unsubscribe channel
	@PostMapping("/unsubscribe")
	public ResponseEntity<String> unsubscribe(@RequestBody ChannelRequest request) {
		String channelEmail = request.getChannelEmail();
		userService.unsubscribeFromChannel(channelEmail);
		return ResponseEntity.ok("Unsubscribed successfully");
	}

	// Upload user profile image
	@PostMapping("/upload-profile/{email}")
	public ResponseEntity<String> uploadProfileImage(@PathVariable String email,
			@RequestParam("file") MultipartFile file) {
		try {
			if (!userService.validateEmail(email)) {
				logger.warn("Unauthorized access attempted to upload profile image for email: {}", email);
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized access");
			}
			userService.uploadProfileImage(email, file);
			logger.info("Profile image uploaded successfully for email: {}", email);
			return ResponseEntity.ok("Profile image uploaded successfully");
		} catch (IOException e) {
			logger.error("Failed to upload profile image for email: {}", email, e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to upload profile image");
		}
	}

	// Generate OTP for user signup
	@PostMapping("/signup/generate-otp")
	public ResponseEntity<String> signupOTP(@RequestBody Map<String, String> request) {
		String email = request.get("email");
		if (email != null) {
			if (userService.existsByEmail(email)) {
				logger.warn("Email already registered for OTP generation: {}", email);
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Email is already registered");
			}
			boolean otpGenerated = userService.generateAndSendOTP(email);
			if (otpGenerated) {
				logger.info("OTP generated successfully and sent to email: {}", email);
				return ResponseEntity.ok("OTP generated successfully and sent to email: " + email);
			} else {
				logger.error("Failed to generate OTP for email: {}", email);
				return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to generate OTP");
			}
		} else {
			logger.warn("No email provided for OTP generation");
			return ResponseEntity.badRequest().body("Please provide email in the request body.");
		}
	}

	// Generate OTP for password reset
	@PostMapping("/forget-password/generate-otp")
	public ResponseEntity<String> generateOTP(@RequestBody Map<String, String> request) {
		String email = request.get("email");
		if (email != null) {
			if (!userService.existsByEmail(email)) {
				logger.warn("Email not registered for password reset: {}", email);
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Email is not registered.");
			}
			boolean otpGenerated = userService.generateAndSendOTP(email);
			if (otpGenerated) {
				logger.info("OTP generated successfully and sent to email for password reset: {}", email);
				return ResponseEntity.ok("OTP generated successfully and sent to email: " + email);
			} else {
				logger.error("Failed to generate OTP for password reset for email: {}", email);
				return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to generate OTP");
			}
		} else {
			logger.warn("No email provided for OTP generation for password reset");
			return ResponseEntity.badRequest().body("Please provide email in the request body.");
		}
	}

	// Validate OTP
	@PostMapping("/validate-otp")
	public ResponseEntity<String> validateOTP(@RequestBody Map<String, String> request) {
		String otp = request.get("otp");
		if (otp != null) {
			boolean otpValidated = userService.validateOTP(otp);
			if (otpValidated) {
				logger.info("OTP validated successfully");
				return ResponseEntity.ok("OTP validated successfully for email");
			} else {
				logger.warn("Invalid OTP or OTP expired");
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid OTP or OTP expired.");
			}
		} else {
			logger.warn("No OTP provided for validation");
			return ResponseEntity.badRequest().body("Please provide otp in the request body.");
		}
	}

	// Update user profile
	@PutMapping("/update-profile/{email}")
	public ResponseEntity<String> updateUserProfile(@PathVariable String email, @Valid @RequestBody User updatedUser) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		UserDetails userDetails = (UserDetails) authentication.getPrincipal();
		String authenticatedEmail = userDetails.getUsername();
		if (!authenticatedEmail.equals(email)) {
			logger.warn("Unauthorized access attempted to update profile for email: {}", email);
			return ResponseEntity.status(HttpStatus.FORBIDDEN)
					.body("You are not authorized to update this user's profile.");
		}
		if (!email.equals(updatedUser.getEmail())) {
			logger.warn("Email in the request body does not match the email in the path variable: {}", email);
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body("Email in the request body does not match the email in the path variable.");
		}
		if (userService.updateUserProfile(email, updatedUser)) {
			logger.info("User profile updated successfully for email: {}", email);
			return ResponseEntity.ok("User profile updated successfully");
		} else {
			logger.warn("User profile not found for update for email: {}", email);
			return ResponseEntity.notFound().build();
		}
	}

	// Update password by email
	@PutMapping("/password")
	public ResponseEntity<String> updatePasswordByEmail(@RequestBody Map<String, String> request) {
		String email = request.get("email");
		String newPassword = request.get("password");
		if (email != null && !email.isEmpty() && newPassword != null && !newPassword.isEmpty()) {
			try {
				userService.updatePasswordByEmail(email, newPassword);
				logger.info("Password updated successfully for email: {}", email);
				return ResponseEntity.ok("Password updated successfully");
			} catch (Exception e) {
				logger.error("Failed to update password for email: {}", email, e);
				return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to update password");
			}
		} else {
			logger.warn("Email and password are required for password update");
			return ResponseEntity.badRequest().body("Email and password are required");
		}
	}

	// Update user active status
	@PutMapping("/update-active-status/{email}")
	public ResponseEntity<String> updateUserActiveStatus(@PathVariable String email) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		UserDetails userDetails = (UserDetails) authentication.getPrincipal();
		String authenticatedEmail = userDetails.getUsername();
		if (!authenticatedEmail.equals(email)) {
			logger.warn("Unauthorized access attempted to update active status for email: {}", email);
			return ResponseEntity.status(HttpStatus.FORBIDDEN)
					.body("You are not authorized to update active status for this user.");
		}
		User user = userService.findByEmail(email);
		if (user == null) {
			logger.warn("User not found for active status update for email: {}", email);
			return ResponseEntity.notFound().build();
		}
		char newActiveStatus = (user.getIsActive() == 'Y') ? 'N' : 'Y';
		if (userService.updateUserActiveStatus(email, newActiveStatus)) {
			if (newActiveStatus == 'Y') {
				logger.info("User account activated successfully for email: {}", email);
				return ResponseEntity.ok("User account activated successfully");
			} else {
				logger.info("User account deactivated successfully for email: {}", email);
				return ResponseEntity.ok("User account deactivated successfully");
			}
		}
		logger.error("Failed to update active status for email: {}", email);
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to update active status");
	}

	// Delete user profile
	@DeleteMapping("/delete-profile/{email}")
	public ResponseEntity<String> deleteUserProfile(@PathVariable String email) {
		if (userService.deleteUserProfile(email)) {
			logger.info("User profile deleted successfully for email: {}", email);
			return ResponseEntity.ok("User profile deleted successfully");
		} else {
			logger.warn("User profile not found for deletion for email: {}", email);
			return ResponseEntity.notFound().build();
		}
	}
}
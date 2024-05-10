package com.bytes.controller;

import java.io.IOException;
import java.util.Map;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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

import com.bytes.model.User;
import com.bytes.service.UserService;
import com.bytes.service.impl.JwtTokenUtil;

@RestController
public class UserController {

	private final UserService userService;

	private final JwtTokenUtil jwtTokenUtil;
	private final AuthenticationManager authenticationManager;
	private final BCryptPasswordEncoder passwordEncoder;

	@Autowired
	public UserController(UserService userService, JwtTokenUtil jwtTokenUtil, BCryptPasswordEncoder passwordEncoder,
			AuthenticationManager authenticationManager) {
		this.userService = userService;
		this.jwtTokenUtil = jwtTokenUtil;
		this.authenticationManager = authenticationManager;
		this.passwordEncoder = passwordEncoder;

	}

	@GetMapping("/profile/{email}")
	public ResponseEntity<User> getUserProfile(@PathVariable String email) {
		User user = userService.findByEmail(email);
		if (user != null) {
			return ResponseEntity.ok(user);
		} else {
			return ResponseEntity.notFound().build();
		}
	}

	// This endpoint should be restricted and used only for administrative purposes
	@GetMapping("/admin/profile/{email}/{plaintextPassword}")
	public ResponseEntity<String> getAdminUserProfile(@PathVariable String email,
			@PathVariable String plaintextPassword) {
		User user = userService.findByEmail(email);
		if (user != null) {
			// Check if the plaintext password matches the hashed password
			if (passwordEncoder.matches(plaintextPassword, user.getPassword())) {
				return ResponseEntity.ok("Password matches for user with email: " + email);
			} else {
				return ResponseEntity.ok("Password does not match for user with email: " + email);
			}
		} else {
			return ResponseEntity.notFound().build();
		}

	}

	@PostMapping("/signup/create-profile")
	public ResponseEntity<String> registerUser(@Valid @RequestBody User user) {
		if (userService.existsByEmail(user.getEmail())) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body("Email already exists, please choose another one.");
		} else {
			userService.save(user);
			return ResponseEntity.status(HttpStatus.CREATED).body("User registered successfully");
		}
	}

	@PostMapping("/profile/login")
	public ResponseEntity<?> loginUser(@RequestBody User user) {
		try {
			// Authenticate user
			Authentication authentication = authenticationManager
					.authenticate(new UsernamePasswordAuthenticationToken(user.getEmail(), user.getPassword()));

			// Set authentication in security context
			SecurityContextHolder.getContext().setAuthentication(authentication);

			// Retrieve UserDetails from authentication
			UserDetails userDetails = (UserDetails) authentication.getPrincipal();

			// Check if user is active
			User dbUser = userService.findByEmail(user.getEmail());
			if (dbUser.getActive() == 'N') {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Account is inactive.");
			}

			// Generate JWT token
			String token = jwtTokenUtil.generateToken(userDetails);

			// Return token in response
			return ResponseEntity.ok(token);
		} catch (AuthenticationException e) {
			// Handle authentication failure
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
		} catch (Exception e) {
			// Handle other unexpected errors
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal server error");
		}
	}

	@PostMapping("/upload-profile/{email}")
	public ResponseEntity<String> uploadProfileImage(@PathVariable String email,
			@RequestParam("file") MultipartFile file) {
		try {
			if (!userService.validateEmail(email)) {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized access");
			}
			userService.uploadProfileImage(email, file);
			return ResponseEntity.ok("Profile image uploaded successfully");
		} catch (IOException e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to upload profile image");
		}
	}

	@PostMapping("/signup/generateOTP")
	public ResponseEntity<String> signupOTP(@RequestBody Map<String, String> request) {
		String email = request.get("email");

		if (email != null) {
			// Check if the email is already registered
			if (userService.existsByEmail(email)) {
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Email is already registed");
			}

			// Generate and send OTP
			boolean otpGenerated = userService.generateAndSendOTP(email);
			if (otpGenerated) {
				return ResponseEntity.ok("OTP generated successfully and sent to email: " + email);
			} else {
				return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to generate OTP");
			}
		} else {
			return ResponseEntity.badRequest().body("Please provide email in the request body.");
		}
	}

	@PostMapping("/forget-password/generateOTP")
	public ResponseEntity<String> generateOTP(@RequestBody Map<String, String> request) {
		String email = request.get("email");

		if (email != null) {
			// Check if the email is already registered
			if (!userService.existsByEmail(email)) {
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Email is not registered.");
			}

			// Generate and send OTP
			boolean otpGenerated = userService.generateAndSendOTP(email);
			if (otpGenerated) {
				return ResponseEntity.ok("OTP generated successfully and sent to email: " + email);
			} else {
				return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to generate OTP");
			}
		} else {
			return ResponseEntity.badRequest().body("Please provide email in the request body.");
		}
	}

	@PostMapping("/validateOTP")
	public ResponseEntity<String> validateOTP(@RequestBody Map<String, String> request) {
//		String email = request.get("email");
		String otp = request.get("otp");

		if (otp != null) {
			boolean otpValidated = userService.validateOTP(otp);
			if (otpValidated) {
				return ResponseEntity.ok("OTP validated successfully for email");
			} else {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid OTP or OTP expired.");
			}
		} else {
			return ResponseEntity.badRequest().body("Please provide otp in the request body.");
		}
	}

	@PutMapping("/update-profile/{email}")
	public ResponseEntity<String> updateUserProfile(@PathVariable String email, @Valid @RequestBody User updatedUser) {
		// Get the authenticated user's email
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		UserDetails userDetails = (UserDetails) authentication.getPrincipal();
		String authenticatedEmail = userDetails.getUsername();

		// Check if the authenticated user's email matches the email in the path
		// variable
		if (!authenticatedEmail.equals(email)) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN)
					.body("You are not authorized to update this user's profile.");
		}

		// Check if the email in the request body matches the email in the path variable
		if (!email.equals(updatedUser.getEmail())) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body("Email in the request body does not match the email in the path variable.");
		}

		// Update the user profile
		if (userService.updateUserProfile(email, updatedUser)) {
			return ResponseEntity.ok("User profile updated successfully");
		} else {
			return ResponseEntity.notFound().build();
		}
	}
	
	@PutMapping("/password")
	public ResponseEntity<String> updatePasswordByEmail(@RequestBody Map<String, String> request) {
	    String email = request.get("email");
	    String newPassword = request.get("password");
	    
	    if (email != null && !email.isEmpty() && newPassword != null && !newPassword.isEmpty()) {
	        try {
	            userService.updatePasswordByEmail(email, newPassword);
	            return ResponseEntity.ok("Password updated successfully");
	        } catch (Exception e) {
	            // Log the error or handle it accordingly
	            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to update password");
	        }
	    } else {
	        return ResponseEntity.badRequest().body("Email and password are required");
	    }
	}

	@PutMapping("/update-active-status/{email}")
	public ResponseEntity<String> updateUserActiveStatus(@PathVariable String email) {
		// Get the authenticated user's email
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		UserDetails userDetails = (UserDetails) authentication.getPrincipal();
		String authenticatedEmail = userDetails.getUsername();

		if (!authenticatedEmail.equals(email)) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN)
					.body("You are not authorized to update active status for this user.");
		}

		User user = userService.findByEmail(email);
		if (user == null) {
			return ResponseEntity.notFound().build();
		}

		char newActiveStatus = (user.getActive() == 'Y') ? 'N' : 'Y'; // Toggle active status
		if (userService.updateUserActiveStatus(email, newActiveStatus)) {
			if (newActiveStatus == 'Y') {
				return ResponseEntity.ok("User account activated successfully");
			} else {
				return ResponseEntity.ok("User account deactivated successfully");
			}
		}

		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to update active status");
	}

	@DeleteMapping("/delete-profile/{email}")
	public ResponseEntity<String> deleteUserProfile(@PathVariable String email) {
		if (userService.deleteUserProfile(email)) {
			return ResponseEntity.ok("User profile deleted successfully");
		} else {
			return ResponseEntity.notFound().build();
		}
	}
}

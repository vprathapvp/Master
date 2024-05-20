package com.bytes.service.impl;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.bytes.model.User;
import com.bytes.repository.UserRepository;
import com.bytes.service.UserService;

@Service
public class UserServiceImpl implements UserService {

	private final UserRepository userRepository;
	private final JavaMailSender javaMailSender;
	private final BCryptPasswordEncoder passwordEncoder;
	private final Map<String, OtpInfo> otpMap = new HashMap<>();

	@Autowired
	public UserServiceImpl(UserRepository userRepository, JavaMailSender javaMailSender,
			BCryptPasswordEncoder passwordEncoder) {
		this.userRepository = userRepository;
		this.javaMailSender = javaMailSender;
		this.passwordEncoder = passwordEncoder;
	}

	// Check if user exists by email
	@Override
	public boolean existsByEmail(String email) {
		return userRepository.existsByEmail(email);
	}

	// Save new user
	@Override
	public void save(@Valid User user) {
		user.setCreatedDateTime(LocalDateTime.now());
		user.setUpdateDateTime(LocalDateTime.now());
		user.setPassword(passwordEncoder.encode(user.getPassword()));
		user.setIsActive('Y');
		userRepository.save(user);
	}

	// Find user by email
	@Override
	public User findByEmail(String email) {
		return userRepository.findByEmail(email);
	}

	// Delete user
	@Override
	public void delete(User user) {
		userRepository.delete(user);
	}

	// Authenticate user by email and password
	@Override
	public boolean authenticateUser(String email, String password) {
		User user = userRepository.findByEmail(email);
		return user != null && passwordEncoder.matches(password, user.getPassword());
	}

	// Upload user profile image
	@Override
	public void uploadProfileImage(String email, MultipartFile file) throws IOException {
		User user = userRepository.findByEmail(email);
		if (user == null) {
			throw new IllegalArgumentException("User not found with email: " + email);
		}

		if (file.isEmpty()) {
			user.setProfileImage(getProfileInitial(user.getFirstName()));
		} else {
			user.setProfileImage(file.getBytes());
		}
		user.setUpdateDateTime(LocalDateTime.now());
		userRepository.save(user);
	}

	// Get profile initial based on the first character of the name
	private byte[] getProfileInitial(String name) {
		if (!StringUtils.hasText(name)) {
			return null;
		}
		return new byte[] { (byte) name.charAt(0) };
	}

	// Update user profile
	@Override
	public boolean updateUserProfile(String email, User updatedUser) {
		User user = userRepository.findByEmail(email);
		if (user != null) {
			user.setFirstName(updatedUser.getFirstName());
			user.setLastName(updatedUser.getLastName());
			user.setLatitude(updatedUser.getLatitude());
			user.setLongitude(updatedUser.getLongitude());
			user.setUpdateDateTime(LocalDateTime.now());
			userRepository.save(user);
			return true;
		} else {
			return false;
		}
	}

	// Update password by email
	@Override
	public void updatePasswordByEmail(String email, String newPassword) {
		if (!StringUtils.hasText(email) || !StringUtils.hasText(newPassword)) {
			throw new IllegalArgumentException("Email and newPassword cannot be null or empty");
		}

		User user = userRepository.findByEmail(email);
		if (user == null) {
			throw new IllegalArgumentException("User with email " + email + " not found.");
		}

		String encodedPassword = passwordEncoder.encode(newPassword);
		user.setPassword(encodedPassword);
		userRepository.save(user);
	}

	// Update user active status
	@Override
	public boolean updateUserActiveStatus(String email, char active) {
		User user = userRepository.findByEmail(email);
		if (user != null) {
			user.setIsActive(active);
			userRepository.save(user);
			return true;
		}
		return false;
	}

	// Delete user profile
	@Override
	public boolean deleteUserProfile(String email) {
		User user = userRepository.findByEmail(email);
		if (user != null) {
			userRepository.delete(user);
			return true;
		} else {
			return false;
		}
	}

	// Generate and send OTP for user signup
	@Override
	public boolean generateAndSendOTP(String email) {
		String otp = generateRandomOTP();
		LocalDateTime expirationTime = LocalDateTime.now().plusMinutes(3);
		otpMap.put(email, new OtpInfo(otp, expirationTime));

		sendEmail(email, "OTP for Registration", "Your OTP is: " + otp);

		return true;
	}

	// Validate OTP
	@Override
	public boolean validateOTP(String otp) {
		for (OtpInfo otpInfo : otpMap.values()) {
			if (otpInfo != null && !otpInfo.isExpired() && otpInfo.getOtp().equals(otp)) {
				return true;
			}
		}
		return false;
	}

	// Validate email against logged in user
	@Override
	public boolean validateEmail(String email) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		String loggedInUserEmail = authentication.getName();
		return email.equals(loggedInUserEmail);
	}

	// Generate random OTP
	private String generateRandomOTP() {
		Random random = new Random();
		int otp = 100000 + random.nextInt(900000);
		return String.valueOf(otp);
	}

	// Send email with OTP
	private void sendEmail(String to, String subject, String text) {
		MimeMessage message = javaMailSender.createMimeMessage();
		try {
			MimeMessageHelper helper = new MimeMessageHelper(message, true);
			helper.setTo(to);
			helper.setSubject(subject);
			helper.setText(text, true);
		} catch (MessagingException e) {
			e.printStackTrace();
		}
		javaMailSender.send(message);
	}

	public Map<String, OtpInfo> getOtpMap() {
		return otpMap;
	}

	// Class to store OTP information
	public static class OtpInfo {
		private String otp;
		private LocalDateTime expirationTime;

		public OtpInfo(String otp, LocalDateTime expirationTime) {
			this.otp = otp;
			this.expirationTime = expirationTime;
		}

		public String getOtp() {
			return otp;
		}

		public boolean isExpired() {
			return LocalDateTime.now().isAfter(expirationTime);
		}
	}
}

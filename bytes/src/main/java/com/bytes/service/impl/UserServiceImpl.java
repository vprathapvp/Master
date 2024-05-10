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
	private final BCryptPasswordEncoder passwordEncoder; // Inject BCryptPasswordEncoder
	private final Map<String, OtpInfo> otpMap = new HashMap<>();

	@Autowired
	public UserServiceImpl(UserRepository userRepository, JavaMailSender javaMailSender,
			BCryptPasswordEncoder passwordEncoder) {
		this.userRepository = userRepository;
		this.javaMailSender = javaMailSender;
		this.passwordEncoder = passwordEncoder;
	}

	@Override
	public boolean existsByEmail(String email) {
		return userRepository.existsByEmail(email);
	}

	@Override
	public void save(@Valid User user) {
		user.setcreateddatetime(LocalDateTime.now());
		user.setupdateDateTime(LocalDateTime.now());
		user.setPassword(passwordEncoder.encode(user.getPassword()));
		user.setActive('Y');
		userRepository.save(user);
	}

	@Override
	public User findByEmail(String email) {
		return userRepository.findByEmail(email);
	}

	@Override
	public void delete(User user) {
		userRepository.delete(user);
	}

	@Override
	public boolean authenticateUser(String email, String password) {
		User user = userRepository.findByEmail(email);
		return user != null && passwordEncoder.matches(password, user.getPassword());
	}

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
		user.setupdateDateTime(LocalDateTime.now());
		userRepository.save(user);
	}

	
	@Override
	public boolean updateUserProfile(String email, User updatedUser) {
	    User user = userRepository.findByEmail(email);
	    if (user != null) {
	        user.setFirstName(updatedUser.getFirstName());
	        user.setLastName(updatedUser.getLastName());
	        user.setLatitude(updatedUser.getLatitude());
	        user.setLongitude(updatedUser.getLongitude());
			user.setupdateDateTime(LocalDateTime.now());
	        // Update any other fields as needed
	        userRepository.save(user);
	        return true;
	    } else {
	        return false;
	    }
	}
	
	@Override
	public void updatePasswordByEmail(String email, String newPassword) {
	    if (email != null && !email.isEmpty() && newPassword != null && !newPassword.isEmpty()) {
	        User user = userRepository.findByEmail(email);
	        if (user != null) {
	            String encodedPassword = passwordEncoder.encode(newPassword);
	            user.setPassword(encodedPassword);
	            userRepository.save(user);
	        } else {
	            // Handle case when user with the given email is not found
	            // You can throw an exception or log a message
	            System.err.println("User with email " + email + " not found.");
	        }
	    }
	}

	@Override
	public boolean updateUserActiveStatus(String email, char active) {
		User user = userRepository.findByEmail(email);
		if (user != null) {
			user.setActive(active);
			userRepository.save(user);
			return true;
		}
		return false;
	}

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

	private byte[] getProfileInitial(String name) {
		if (!StringUtils.hasText(name)) {
			return null;
		}
		return new byte[] { (byte) name.charAt(0) };
	}

	@Override
	public boolean generateAndSendOTP(String email) {
		String otp = generateRandomOTP();
		LocalDateTime expirationTime = LocalDateTime.now().plusMinutes(3);
		otpMap.put(email, new OtpInfo(otp, expirationTime));

		sendEmail(email, "OTP for Registration", "Your OTP is: " + otp);

		return true;
	}

	@Override
	public boolean validateOTP(String otp) {
	    for (OtpInfo otpInfo : otpMap.values()) {
	        if (otpInfo != null && !otpInfo.isExpired() && otpInfo.getOtp().equals(otp)) {
	            return true;
	        }
	    }
	    return false;
	}
	
	@Override
	public boolean validateEmail(String email) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		String loggedInUserEmail = authentication.getName();
		return email.equals(loggedInUserEmail);
	}

	private String generateRandomOTP() {
		Random random = new Random();
		int otp = 100000 + random.nextInt(900000);
		return String.valueOf(otp);
	}

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

	private static class OtpInfo {
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

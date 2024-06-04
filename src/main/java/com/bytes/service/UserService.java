package com.bytes.service;

import java.io.IOException;

import javax.validation.Valid;

import org.springframework.web.multipart.MultipartFile;

import com.bytes.model.User;

public interface UserService {

	boolean existsByEmail(String email);

	void save(@Valid User user);

	User findByEmail(String email);

	void delete(User user);

	void uploadProfileImage(String email, MultipartFile file) throws IOException;

	boolean updateUserProfile(String email, User updatedUser);
	
	public void updatePasswordByEmail(String email, String newPassword);

	public boolean updateUserActiveStatus(String email, char active);

	boolean deleteUserProfile(String email);

	boolean authenticateUser(String email, String password);

	boolean generateAndSendOTP(String email);

	boolean validateOTP(String otp);

	public boolean validateEmail(String email);
	
	 public byte[] getProfileImage(Long userId);
}

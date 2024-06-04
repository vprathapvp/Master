package com.example.signup;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;

import javax.mail.internet.MimeMessage;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.bytes.model.User;
import com.bytes.repository.UserRepository;
import com.bytes.service.impl.UserServiceImpl;

@RunWith(MockitoJUnitRunner.class)
@SpringBootTest

public class UserServiceImplTest {

	@Mock
	private UserRepository userRepository;

	@Mock
	private JavaMailSender javaMailSender;

	@Mock
	private BCryptPasswordEncoder passwordEncoder;

	@InjectMocks
	private UserServiceImpl userService;

	private User testUser;

	@Before
	public void setUp() {
		testUser = new User();
		testUser.setEmail("test@example.com");
		testUser.setPassword("password"); 
	}

	@Test
	public void testSaveUser() {
		when(userRepository.save(any(User.class))).thenReturn(testUser);

		userService.save(testUser);

		verify(userRepository, times(1)).save(any(User.class));
	}

	@Test
	public void testFindByEmail() {
		when(userRepository.findByEmail("test@example.com")).thenReturn(testUser);

		User foundUser = userService.findByEmail("test@example.com");

		assertEquals(testUser, foundUser);
	}

	@Test
	public void testUpdateUserProfile() {
		when(userRepository.findByEmail("test@example.com")).thenReturn(testUser);

		User updatedUser = new User();
		updatedUser.setFirstName("NewFirstName");
		updatedUser.setLastName("NewLastName");
		updatedUser.setLatitude(123.456);
		updatedUser.setLongitude(456.789);

		boolean result = userService.updateUserProfile("test@example.com", updatedUser);

		assertTrue(result);
		assertEquals("NewFirstName", testUser.getFirstName());
		assertEquals("NewLastName", testUser.getLastName());
		assertEquals(123.456, testUser.getLatitude(), 0.001);
		assertEquals(456.789, testUser.getLongitude(), 0.001);
	}

	@Test
	public void testUpdatePasswordByEmail() {
		User testUser = new User();
		testUser.setEmail("test@example.com");
		testUser.setPassword("password"); 

		when(userRepository.findByEmail("test@example.com")).thenReturn(testUser);

		String encodedPassword = "encodedPassword"; 
		when(passwordEncoder.encode("newPassword")).thenReturn(encodedPassword);

		userService.updatePasswordByEmail("test@example.com", "newPassword");

		verify(userRepository, times(1)).save(any(User.class));

		ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
		verify(userRepository).save(userCaptor.capture());

		User capturedUser = userCaptor.getValue();
		assertNotNull(capturedUser.getPassword());
		assertEquals(encodedPassword, capturedUser.getPassword()); 
	}

	@Test
	public void testUpdateUserActiveStatus() {
		when(userRepository.findByEmail("test@example.com")).thenReturn(testUser);

		boolean result = userService.updateUserActiveStatus("test@example.com", 'N');

		assertTrue(result);
		assertEquals('N', testUser.getIsActive());
	}

	@Test
	public void testDeleteUserProfile() {
		when(userRepository.findByEmail("test@example.com")).thenReturn(testUser);

		boolean result = userService.deleteUserProfile("test@example.com");

		assertTrue(result);
		verify(userRepository, times(1)).delete(testUser);
	}

	@Test
	public void testGenerateAndSendOTP() {
		when(javaMailSender.createMimeMessage()).thenReturn(mock(MimeMessage.class));

		assertTrue(userService.generateAndSendOTP("test@example.com"));
	}

	@Test
	public void testValidateOTP() {
		String email = "test@example.com";
		String otp = "123456"; // Assume this is the generated OTP
		LocalDateTime expirationTime = LocalDateTime.now().plusMinutes(3);
		userService.getOtpMap().put(email, new UserServiceImpl.OtpInfo(otp, expirationTime));

		assertTrue(userService.validateOTP(otp));
	}

	@Test
	public void testValidateEmail() {
		Authentication authentication = mock(Authentication.class);
		SecurityContextHolder.getContext().setAuthentication(authentication);
		when(authentication.getName()).thenReturn("test@example.com");

		assertTrue(userService.validateEmail("test@example.com"));
	}
}

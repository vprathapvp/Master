package com.example.signup;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ContextConfiguration;

import com.bytes.message.ResponseBytesMessage;
import com.bytes.model.Bytes;
import com.bytes.model.User;
import com.bytes.repository.BytesRepository;
import com.bytes.repository.MetadataRepository;
import com.bytes.repository.UserRepository;
import com.bytes.service.impl.BytesServiceImpl;

import bytes.com.exception.ResourceNotFoundException;

@ExtendWith(MockitoExtension.class)
@ContextConfiguration
public class BytesServiceImplTest {

	@Mock
	private BytesRepository bytesRepository;

	@Mock
	private UserRepository userRepository;

	@Mock
	private MetadataRepository metadataRepository;

	@InjectMocks
	private BytesServiceImpl bytesService;

	@Test
	void testUploadFile_Success() throws IOException {
		File videoFile = new File("C:/Users/acer/Videos/test.mp4");
		assertTrue(videoFile.exists());

		User mockUser = new User();
		when(userRepository.findByEmail(anyString())).thenReturn(mockUser);

		when(bytesRepository.save(any(Bytes.class))).thenReturn(null);

		BytesServiceImpl spyService = spy(bytesService);
		doReturn(10L).when(spyService).getVideoDuration(any());

		ResponseBytesMessage response = spyService.uploadFile(videoFile, "test@test.com", "Description", "0", "0");

		assertNotNull(response);
		assertEquals("Uploaded the file successfully: test.mp4", response.getMessage());
	}

	@Test
	public void testUploadFile_NullFile() {
		File file = null;
		String email = "test@example.com";
		String description = "Test video";
		String latitude = "123.456";
		String longitude = "789.012";

		ResponseBytesMessage response = bytesService.uploadFile(file, email, description, latitude, longitude);

		assertEquals("Please provide file, email, description, latitude, and longitude.", response.getMessage());
	}

	@Test
	public void testPostComment_Success() {
		String id = "123";
		String firstName = "John";
		String lastName = "Doe";
		String comment = "Great video!";

		Bytes bytes = new Bytes();
		when(bytesRepository.findById(id)).thenReturn(Optional.of(bytes));
		when(bytesRepository.save(any(Bytes.class))).thenReturn(bytes);

		bytesService.postComment(id, firstName, lastName, comment);

		assertNotNull(bytes.getMetadata());
		assertTrue(bytes.getMetadata().getComments().contains(comment));
	}

	@Test
	public void testPostComment_VideoNotFound() {
		String id = "123";
		String firstName = "John";
		String lastName = "Doe";
		String comment = "Great video!";

		when(bytesRepository.findById(id)).thenReturn(Optional.empty());

		assertThrows(ResourceNotFoundException.class, () -> {
			bytesService.postComment(id, firstName, lastName, comment);
		});
	}

	@Test
	public void testGetFile_Success() {
		String id = "123";
		Bytes expectedBytes = new Bytes();

		when(bytesRepository.findById(id)).thenReturn(Optional.of(expectedBytes));

		Bytes result = bytesService.getFile(id);

		assertEquals(expectedBytes, result);
	}

	@Test
	public void testGetFile_FileNotFound() {
		String id = "123";

		when(bytesRepository.findById(id)).thenReturn(Optional.empty());

		assertNull(bytesService.getFile(id));
	}

	@Test
	public void testGetFileById_Success() {
		String id = "123";
		Bytes expectedBytes = new Bytes();

		when(bytesRepository.findById(id)).thenReturn(Optional.of(expectedBytes));

		Optional<Bytes> result = bytesService.getFileById(id);

		assertTrue(result.isPresent());
		assertEquals(expectedBytes, result.get());
	}

	@Test
	public void testGetFileById_FileNotFound() {
		String id = "123";

		when(bytesRepository.findById(id)).thenReturn(Optional.empty());

		Optional<Bytes> result = bytesService.getFileById(id);

		assertFalse(result.isPresent());
	}

	@Test
	public void testGetFilesByEmail_Success() {
		String email = "test@example.com";
		List<Bytes> expectedBytesList = new ArrayList<>();

		when(bytesRepository.findByUserEmail(email)).thenReturn(expectedBytesList);

		List<Bytes> result = bytesService.getFilesByEmail(email);

		assertEquals(expectedBytesList, result);
	}

	@Test
	public void testGetAllFiles_Success() {
		List<Bytes> expectedBytesList = new ArrayList<>();

		when(bytesRepository.findAll()).thenReturn(expectedBytesList);

		List<Bytes> result = bytesService.getAllFiles();

		assertEquals(expectedBytesList, result);
	}

	@Test
	public void testGetFileDetailsById_FileNotFound() {
		String id = "123";

		when(bytesRepository.findById(id)).thenReturn(Optional.empty());

		assertThrows(ResourceNotFoundException.class, () -> {
			bytesService.getFileDetailsById(id);
		});
	}

	@Test
	public void testUpdateFileDetails_Success() {
		String id = "123";
		String newFileName = "new_test_video.mp4";
		String newDescription = "Updated video description";

		Bytes existingBytes = new Bytes();
		existingBytes.setName("test_video.mp4"); // Set the initial name
		existingBytes.setDescription("Initial description"); // Set the initial description
		when(bytesRepository.findById(id)).thenReturn(Optional.of(existingBytes));
		when(bytesRepository.save(any(Bytes.class))).thenReturn(existingBytes);

		Bytes result = bytesService.updateFileDetails(id, newFileName, newDescription);

		assertEquals(newFileName, result.getName());
		assertEquals(newDescription, result.getDescription());
	}

	@Test
	public void testUpdateFileDetails_FileNotFound() {
		String id = "123";
		String newFileName = "new_test_video.mp4";
		String newDescription = "Updated video description";

		when(bytesRepository.findById(id)).thenReturn(Optional.empty());

		assertThrows(ResourceNotFoundException.class, () -> {
			bytesService.updateFileDetails(id, newFileName, newDescription);
		});
	}

	@Test
	public void testLikeFile_FileNotFound() {
		String id = "123";
		String email = "test@example.com";

		when(bytesRepository.findById(id)).thenReturn(Optional.empty());

		assertThrows(ResourceNotFoundException.class, () -> {
			bytesService.likeFile(id, email);
		});
	}

	@Test
	public void testDislikeFile_FileNotFound() {
		String id = "123";
		String email = "test@example.com";

		when(bytesRepository.findById(id)).thenReturn(Optional.empty());

		assertThrows(ResourceNotFoundException.class, () -> {
			bytesService.dislikeFile(id, email);
		});
	}

	@Test
	public void testGetUploaderIdByEmail_Success() {
		String email = "test@example.com";
		Long userId = 123L;

		User user = new User();
		user.setId(userId);
		when(userRepository.findByEmail(email)).thenReturn(user);

		String result = bytesService.getUploaderIdByEmail(email);

		assertEquals(userId.toString(), result);
	}

	@Test
	public void testGetUploaderIdByEmail_UserNotFound() {
		String email = "test@example.com";

		when(userRepository.findByEmail(email)).thenReturn(null);

		assertThrows(ResourceNotFoundException.class, () -> {
			bytesService.getUploaderIdByEmail(email);
		});
	}

	private File createTestFile() throws IOException {
		File file = Files.createTempFile("test_video", ".mp4").toFile();
		file.deleteOnExit();
		return file;
	}

	private void deleteTestFile(File file) {
		if (file != null && file.exists()) {
			file.delete();
		}
	}
}

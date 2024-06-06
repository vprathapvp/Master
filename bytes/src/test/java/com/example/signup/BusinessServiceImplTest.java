package com.example.signup;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;

import com.bytes.model.Business;
import com.bytes.repository.BusinessRepository;
import com.bytes.service.impl.BusinessServiceImpl;

@RunWith(MockitoJUnitRunner.class)
@SpringBootTest

public class BusinessServiceImplTest {

	@Mock
	private BusinessRepository businessRepository;

	@InjectMocks
	private BusinessServiceImpl businessService;

	private Business testBusiness;

	@Before
	public void setUp() {
		testBusiness = new Business();
		testBusiness.setId(1L);
	}

	@Test
	public void testGetAvailableSubscriptions() {
		String[] subscriptions = businessService.getAvailableSubscriptions();
		assertNotNull(subscriptions);
		assertEquals(4, subscriptions.length);
	}

	@Test
	public void testGetAvailableRanges() {
		String[] ranges = businessService.getAvailableRanges();
		assertNotNull(ranges);
		assertEquals(4, ranges.length);
	}

	@Test
	public void testCalculatePayment() {
		String[] subscription = { "Monthly" };
		String[] distanceRange = { "20km" };
		double payment = businessService.calculatePayment(subscription, distanceRange);
		assertEquals(2000.0, payment, 0.001);
	}

	@Test
	public void testUploadAds() throws IOException {
		MockMultipartFile file = new MockMultipartFile("file", "test.mp4", "video/mp4", "test data".getBytes());
		String subscription = "Monthly";
		String distanceRange = "20km";
		String latitude = "123.456";
		String longitude = "456.789";
		String email = "test@example.com";

		BusinessServiceImpl spyService = spy(businessService);
		doReturn(10L).when(spyService).getVideoDuration(any());

		spyService.uploadAds(file, subscription, distanceRange, latitude, longitude, email);
	}

	@Test
	public void testGetAdById() {
		when(businessRepository.findById(1L)).thenReturn(Optional.of(testBusiness));

		Optional<Business> foundBusiness = businessService.getAdById(1L);

		assertTrue(foundBusiness.isPresent());
		assertEquals(testBusiness, foundBusiness.get());
	}

	@Test
	public void testDownloadFile() {
		byte[] testData = "Test advertisement data".getBytes();
		testBusiness.setAd(testData);

		when(businessRepository.findById(1L)).thenReturn(Optional.of(testBusiness));

		ResponseEntity<Resource> response = businessService.downloadFile("1");

		assertEquals(HttpStatus.OK, response.getStatusCode());

		assertNotNull(response.getBody());

		assertEquals(MediaType.parseMediaType("video/mp4"), response.getHeaders().getContentType());

		assertEquals("inline; filename=\"file_1\"", response.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION));

		byte[] responseBody = new byte[0];
		try {
			responseBody = response.getBody().getInputStream().readAllBytes();
		} catch (IOException e) {
			e.printStackTrace();
		}
		assertArrayEquals(testData, responseBody);
	}

	@Test
	public void testDownloadFile_NotFound() {

		when(businessRepository.findById(1L)).thenReturn(Optional.empty());

		ResponseEntity<Resource> response = businessService.downloadFile("1");

		assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
	}

	@Test
	public void testIsValidOption() {
		String option = "Monthly";
		String[] validOptions = { "Monthly", "Quarterly", "Half-yearly", "Annual" };
		assertTrue(businessService.isValidOption(option, validOptions));
	}

	@Test
	public void testValidateEmail() {
		String loggedInUserEmail = "user@example.com";
		String videoUploaderEmail = "user@example.com";
		assertTrue(businessService.validateEmail(loggedInUserEmail, videoUploaderEmail));
	}

	@Test
	public void testSaveBusiness() {
		Business businessToSave = new Business();
		businessService.saveBusiness(businessToSave);
	}

	@Test
	public void testGetAllAds() {
		when(businessRepository.findAll()).thenReturn(List.of(testBusiness));
		assertEquals(1, businessService.getAllAds().size());
	}

	@Test
	public void testDeleteAd() {
		businessService.deleteAd(1L);
	}

	@Test(expected = BusinessServiceImpl.AdNotFoundException.class)
	public void testUpdateAd() {
		when(businessRepository.findById(1L)).thenReturn(Optional.empty());
		businessService.updateAd(1L, "Monthly", "20km", "123.456", "456.789", "test@example.com");
	}

	@Test(expected = BusinessServiceImpl.AdNotFoundException.class)
	public void testUpdateAd_NotFound() {
		when(businessRepository.findById(1L)).thenReturn(Optional.empty());
		businessService.updateAd(1L, "Monthly", "20km", "123.456", "456.789", "test@example.com");
	}

	@Test(expected = BusinessServiceImpl.UnauthorizedOperationException.class)
	public void testUpdateAd_Unauthorized() {
		Business unauthorizedBusiness = new Business();
		unauthorizedBusiness.setId(1L);
		unauthorizedBusiness.setemail("other@example.com");
		when(businessRepository.findById(1L)).thenReturn(Optional.of(unauthorizedBusiness));
		businessService.updateAd(1L, "Monthly", "20km", "123.456", "456.789", "test@example.com");
	}
}

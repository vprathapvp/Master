package com.bytes.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.bytes.model.Business;
import com.bytes.service.BusinessService;

@RestController
@RequestMapping("/business")
public class BusinessController {

	private final BusinessService businessService;
	private static final Logger logger = LoggerFactory.getLogger(BusinessController.class);

	@Autowired
	public BusinessController(BusinessService businessService) {
		this.businessService = businessService;
	}

	// Retrieves available subscription options
	@GetMapping("/subscriptions")
	public ResponseEntity<String[]> getSubscriptions() {
		logger.info("Fetching available subscriptions");
		String[] subscriptions = businessService.getAvailableSubscriptions();
		return ResponseEntity.ok(subscriptions);
	}

	// Retrieves available distance range options
	@GetMapping("/ranges")
	public ResponseEntity<String[]> getRanges() {
		logger.info("Fetching available distance ranges");
		String[] distanceRange = businessService.getAvailableRanges();
		return ResponseEntity.ok(distanceRange);
	}

	// Calculates payment amount based on subscription and distance range
	@GetMapping("/calculate-payment")
	public ResponseEntity<Double> calculatePayment(@RequestParam String subscription,
			@RequestParam String distanceRange) {
		logger.info("Calculating payment for subscription: {} and distanceRange: {}", subscription, distanceRange);
		double paymentAmount = businessService.calculatePayment(subscription.split(","), distanceRange.split(","));
		return ResponseEntity.ok(paymentAmount);
	}

	// Retrieves advertisement by ID
	@GetMapping("/api/ad/{id}")
	public ResponseEntity<Map<String, Object>> getAdById(@PathVariable Long id) {
		logger.info("Fetching ad by ID: {}", id);
		Optional<Business> adOptional = businessService.getAdById(id);
		if (adOptional.isPresent()) {
			Business ad = adOptional.get();
			String adUrl = ServletUriComponentsBuilder.fromCurrentContextPath().path("/business/api/files/download/")
					.path(ad.getId().toString()).toUriString();

			Map<String, Object> response = new HashMap<>();
			response.put("ad", adUrl);
			response.put("distance_range", ad.getDistanceRange());
			response.put("payment_amount", ad.getPaymentAmount());
			response.put("subscription", ad.getSubscription());

			logger.info("Ad found: {}", response);
			return ResponseEntity.ok(response);
		} else {
			logger.warn("Ad not found with ID: {}", id);
			return ResponseEntity.notFound().build();
		}
	}

	// Retrieves all advertisements
	@GetMapping("/api/ads")
	public ResponseEntity<List<Map<String, Object>>> getAllAds() {
		logger.info("Fetching all ads");
		List<Business> allAds = businessService.getAllAds();
		List<Map<String, Object>> response = new ArrayList<>();

		for (Business ad : allAds) {
			String adUrl = ServletUriComponentsBuilder.fromCurrentContextPath().path("/business/api/files/download/")
					.path(ad.getId().toString()).toUriString();

			Map<String, Object> adInfo = new HashMap<>();
			adInfo.put("ad_id", ad.getId());
			adInfo.put("ad_url", adUrl);
			adInfo.put("distance_range", ad.getDistanceRange());
			adInfo.put("payment_amount", ad.getPaymentAmount());
			adInfo.put("subscription", ad.getSubscription());

			response.add(adInfo);
		}

		logger.info("All ads fetched successfully");
		return ResponseEntity.ok(response);
	}

	// Downloads advertisement file
	@GetMapping("/api/files/download/{fileId}")
	public ResponseEntity<Resource> downloadFile(@PathVariable String fileId) {
		logger.info("Downloading file with ID: {}", fileId);
		return businessService.downloadFile(fileId);
	}

	// Retrieves advertisements and details by email
	@GetMapping("/api/ads-by-email")
	public ResponseEntity<List<Map<String, Object>>> getAdsByEmail(@RequestParam String email) {
		logger.info("Fetching ads by email: {}", email);
		List<Business> adsByEmail = businessService.getAdsByEmail(email);
		if (adsByEmail.isEmpty()) {
			logger.warn("No ads found for email: {}", email);
			return ResponseEntity.notFound().build();
		}

		List<Map<String, Object>> response = new ArrayList<>();

		for (Business ad : adsByEmail) {
			String adUrl = ServletUriComponentsBuilder.fromCurrentContextPath().path("/business/api/files/download/")
					.path(ad.getId().toString()).toUriString();

			Map<String, Object> adInfo = new HashMap<>();
			adInfo.put("ad_id", ad.getId());
			adInfo.put("ad_url", adUrl);
			adInfo.put("distance_range", ad.getDistanceRange());
			adInfo.put("payment_amount", ad.getPaymentAmount());
			adInfo.put("subscription", ad.getSubscription());
			adInfo.put("latitude", ad.getlatitude());
			adInfo.put("longitude", ad.getlongitude());

			response.add(adInfo);
		}

		logger.info("Ads by email fetched successfully");
		return ResponseEntity.ok(response);
	}

	// Uploads advertisement
	@PostMapping("/upload-ad")
	public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file,
			@RequestParam("subscription") String subscription, @RequestParam("distanceRange") String distanceRange,
			@RequestParam("latitude") String latitude, @RequestParam("longitude") String longitude,
			@RequestParam("email") String email, Authentication authentication) {
		logger.info("Uploading ad for email: {}", email);

		String[] availableSubscriptions = businessService.getAvailableSubscriptions();
		String[] availableRanges = businessService.getAvailableRanges();

		if (!businessService.isValidOption(subscription, availableSubscriptions)
				|| !businessService.isValidOption(distanceRange, availableRanges)) {
			logger.warn("Invalid subscription or distance range provided");
			return ResponseEntity.badRequest().body("Invalid subscription or distance range.");
		}

		String loggedInUserEmail = authentication.getName();

		if (!businessService.validateEmail(loggedInUserEmail, email)) {
			logger.warn(
					"Access Denied: Email in token does not match provided email. Token email: {}, Provided email: {}",
					loggedInUserEmail, email);
			return ResponseEntity.status(HttpStatus.FORBIDDEN)
					.body("Access Denied: Email in token does not match provided email");
		}

		try {
			businessService.uploadAds(file, subscription, distanceRange, latitude, longitude, email);
			double paymentAmount = businessService.calculatePayment(subscription.split(","), distanceRange.split(","));
			String responseMessage = "Uploaded the file successfully: " + file.getOriginalFilename()
					+ ". Payment amount: " + paymentAmount;
			logger.info("File uploaded successfully: {}. Payment amount: {}", file.getOriginalFilename(),
					paymentAmount);
			return ResponseEntity.ok(responseMessage);
		} catch (Exception e) {
			logger.error("Failed to upload file: {}", file.getOriginalFilename(), e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("Failed to upload file: " + e.getMessage());
		}
	}

	// Updates advertisement
	@PutMapping("/update-ad/{adId}")
	public ResponseEntity<String> updateAd(@PathVariable("adId") Long adId,
			@RequestParam("subscription") String subscription, @RequestParam("distanceRange") String distanceRange,
			@RequestParam("latitude") String latitude, @RequestParam("longitude") String longitude,
			@RequestParam("email") String email, Authentication authentication) {
		logger.info("Updating ad with ID: {}", adId);

		String[] availableSubscriptions = businessService.getAvailableSubscriptions();
		String[] availableRanges = businessService.getAvailableRanges();

		if (!businessService.isValidOption(subscription, availableSubscriptions)
				|| !businessService.isValidOption(distanceRange, availableRanges)) {
			logger.warn("Invalid subscription or distance range provided");
			return ResponseEntity.badRequest().body("Invalid subscription or distance range.");
		}

		String loggedInUserEmail = authentication.getName();

		if (!businessService.validateEmail(loggedInUserEmail, email)) {
			logger.warn(
					"Access Denied: Email in token does not match provided email. Token email: {}, Provided email: {}",
					loggedInUserEmail, email);
			return ResponseEntity.status(HttpStatus.FORBIDDEN)
					.body("Access Denied: Email in token does not match provided email");
		}

		try {
			businessService.updateAd(adId, subscription, distanceRange, latitude, longitude, email);

			double paymentAmount = businessService.calculatePayment(subscription.split(","), distanceRange.split(","));
			String responseMessage = "Ad updated successfully. Payment amount: " + paymentAmount;
			logger.info("Ad updated successfully. ID: {}, Payment amount: {}", adId, paymentAmount);
			return ResponseEntity.ok(responseMessage);
		} catch (Exception e) {
			logger.error("Failed to update ad with ID: {}", adId, e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("Failed to update ad: " + e.getMessage());
		}
	}

	// Deletes advertisement by ID
	@DeleteMapping("/api/ad/{id}")
	public ResponseEntity<String> deleteAd(@PathVariable Long id) {
		logger.info("Deleting ad with ID: {}", id);
		Optional<Business> adOptional = businessService.getAdById(id);
		if (adOptional.isPresent()) {
			businessService.deleteAd(id);
			logger.info("Ad deleted successfully with ID: {}", id);
			return ResponseEntity.ok("Ad deleted successfully");
		} else {
			logger.warn("Ad not found with ID: {}", id);
			return ResponseEntity.notFound().build();
		}
	}
}

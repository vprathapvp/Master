package com.bytes.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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

	@Autowired
	public BusinessController(BusinessService businessService) {
		this.businessService = businessService;
	}

	// Retrieves available subscription options
	@GetMapping("/subscriptions")
	public ResponseEntity<String[]> getSubscriptions() {
		String[] subscriptions = businessService.getAvailableSubscriptions();
		return ResponseEntity.ok(subscriptions);
	}

	// Retrieves available distance range options
	@GetMapping("/ranges")
	public ResponseEntity<String[]> getRanges() {
		String[] distanceRange = businessService.getAvailableRanges();
		return ResponseEntity.ok(distanceRange);
	}

	// Calculates payment amount based on subscription and distance range
	@GetMapping("/calculate-payment")
	public ResponseEntity<Double> calculatePayment(@RequestParam String subscription,
			@RequestParam String distanceRange) {
		double paymentAmount = businessService.calculatePayment(subscription.split(","), distanceRange.split(","));
		return ResponseEntity.ok(paymentAmount);
	}

	// Retrieves advertisement by ID
	@GetMapping("/api/ad/{id}")
	public ResponseEntity<Map<String, Object>> getAdById(@PathVariable Long id) {
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

			return ResponseEntity.ok(response);
		} else {
			return ResponseEntity.notFound().build();
		}
	}

	// Retrieves all advertisements
	@GetMapping("/api/ads")
	public ResponseEntity<List<Map<String, Object>>> getAllAds() {
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

		return ResponseEntity.ok(response);
	}

	// Downloads advertisement file
	@GetMapping("/api/files/download/{fileId}")
	public ResponseEntity<Resource> downloadFile(@PathVariable String fileId) {
		return businessService.downloadFile(fileId);
	}

	// Uploads advertisement
	@PostMapping("/upload-ad")
	public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file,
			@RequestParam("subscription") String subscription, @RequestParam("distanceRange") String distanceRange,
			@RequestParam("latitude") String latitude, @RequestParam("longitude") String longitude,
			@RequestParam("email") String email, Authentication authentication) {

		String[] availableSubscriptions = businessService.getAvailableSubscriptions();
		String[] availableRanges = businessService.getAvailableRanges();

		if (!businessService.isValidOption(subscription, availableSubscriptions)
				|| !businessService.isValidOption(distanceRange, availableRanges)) {
			return ResponseEntity.badRequest().body("Invalid subscription or distance range.");
		}

		String loggedInUserEmail = authentication.getName();

		if (!businessService.validateEmail(loggedInUserEmail, email)) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN)
					.body("Access Denied: Email in token does not match provided email");
		}

		try {
			businessService.uploadAds(file, subscription, distanceRange, latitude, longitude, email);
			double paymentAmount = businessService.calculatePayment(subscription.split(","), distanceRange.split(","));
			String responseMessage = "Uploaded the file successfully: " + file.getOriginalFilename()
					+ ". Payment amount: " + paymentAmount;
			return ResponseEntity.ok(responseMessage);
		} catch (Exception e) {
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

		String[] availableSubscriptions = businessService.getAvailableSubscriptions();
		String[] availableRanges = businessService.getAvailableRanges();

		if (!businessService.isValidOption(subscription, availableSubscriptions)
				|| !businessService.isValidOption(distanceRange, availableRanges)) {
			return ResponseEntity.badRequest().body("Invalid subscription or distance range.");
		}

		String loggedInUserEmail = authentication.getName();

		if (!businessService.validateEmail(loggedInUserEmail, email)) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN)
					.body("Access Denied: Email in token does not match provided email");
		}

		try {
			businessService.updateAd(adId, subscription, distanceRange, latitude, longitude, email);

			double paymentAmount = businessService.calculatePayment(subscription.split(","), distanceRange.split(","));
			String responseMessage = "Ad updated successfully. Payment amount: " + paymentAmount;
			return ResponseEntity.ok(responseMessage);
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("Failed to update ad: " + e.getMessage());
		}
	}

	// Deletes advertisement by ID
	@DeleteMapping("/api/ad/{id}")
	public ResponseEntity<String> deleteAd(@PathVariable Long id) {
		Optional<Business> adOptional = businessService.getAdById(id);
		if (adOptional.isPresent()) {
			businessService.deleteAd(id);
			return ResponseEntity.ok("Ad deleted successfully");
		} else {
			return ResponseEntity.notFound().build();
		}
	}
}

package com.bytes.service.impl;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.bytes.model.Business;
import com.bytes.repository.BusinessRepository;
import com.bytes.service.BusinessService;

@Service
public class BusinessServiceImpl implements BusinessService {

	private final BusinessRepository businessRepository;

	public BusinessServiceImpl(BusinessRepository businessRepository) {
		this.businessRepository = businessRepository;
	}

	// Returns available subscription options
	@Override
	public String[] getAvailableSubscriptions() {
		return new String[] { "Monthly", "Quarterly", "Half-yearly", "Annual" };
	}

	// Returns available distance range options
	@Override
	public String[] getAvailableRanges() {
		return new String[] { "20km", "50km", "80km", "200km" };
	}

	// Calculates the total payment amount based on subscription and distance range
	@Override
	public double calculatePayment(String[] subscription, String[] distanceRange) {
		double totalAmount = 0.0;

		for (String sub : subscription) {
			switch (sub) {
			case "Monthly":
				totalAmount += 1000.0;
				break;
			case "Quarterly":
				totalAmount += 3000.0;
				break;
			case "Half-yearly":
				totalAmount += 5000.0;
				break;
			case "Annual":
				totalAmount += 9000.0;
				break;
			default:
				break;
			}
		}

		for (String range : distanceRange) {
			switch (range) {
			case "20km":
				totalAmount += 1000.0;
				break;
			case "50km":
				totalAmount += 3000.0;
				break;
			case "80km":
				totalAmount += 5000.0;
				break;
			case "200km":
				totalAmount += 9000.0;
				break;
			default:
				break;
			}
		}

		return totalAmount;
	}

	// Uploads advertisement
	@Override
	public void uploadAds(MultipartFile file, String subscription, String distanceRange, String latitude,
			String longitude, String email) {
		try {
			File tempFile = File.createTempFile("temp", file.getOriginalFilename());

			file.transferTo(tempFile);

			long videoDuration = getVideoDuration(tempFile);
			if (videoDuration > 15) {
				tempFile.delete();
				throw new RuntimeException(
						"Video duration is more than 15 seconds. Please upload a video with duration less than or equal to 15 seconds.");
			} else {
				Business business = new Business();
				business.setAd(Files.readAllBytes(tempFile.toPath())); // Read file content into byte array
				business.setDistanceRange(distanceRange);
				business.setSubscription(subscription);
				business.setlatitude(latitude);
				business.setlongitude(longitude);
				business.setemail(email);

				double paymentAmount = calculatePayment(subscription.split(","), distanceRange.split(","));
				business.setPaymentAmount(paymentAmount);

				saveBusiness(business);

				tempFile.delete();
			}
		} catch (IOException e) {
			throw new RuntimeException("Failed to upload file: " + e.getMessage());
		}
	}

	// Retrieves video duration using FFmpeg
	@Override
	public long getVideoDuration(File file) throws IOException {
		try {
			ProcessBuilder builder = new ProcessBuilder();
			builder.command("ffmpeg", "-i", file.getAbsolutePath());

			Process process = builder.start();
			process.waitFor();

			String output = new String(process.getErrorStream().readAllBytes());
			System.out.println("FFmpeg Command Output: " + output);

			if (output.contains("Duration:")) {
				String durationLine = output.substring(output.indexOf("Duration:"));
				String[] durationParts = durationLine.split(",")[0].split(" ")[1].split(":");
				double durationInSeconds = TimeUnit.HOURS.toSeconds(Long.parseLong(durationParts[0]))
						+ TimeUnit.MINUTES.toSeconds(Long.parseLong(durationParts[1]))
						+ Double.parseDouble(durationParts[2]);
				return (long) durationInSeconds;
			} else {
				throw new IOException("Failed to get video duration");
			}
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
			throw new IOException("Error executing FFmpeg command: " + e.getMessage());
		}
	}

	// Checks if the provided option is valid
	@Override
	public boolean isValidOption(String option, String[] validOptions) {
		for (String validOption : validOptions) {
			if (validOption.equalsIgnoreCase(option)) {
				return true;
			}
		}
		return false;
	}

	// Validates email ownership
	@Override
	public boolean validateEmail(String loggedInUserEmail, String videoUploaderEmail) {
		return loggedInUserEmail.equals(videoUploaderEmail);
	}

	// Retrieves advertisement by ID
	@Override
	public Optional<Business> getAdById(Long id) {
		return businessRepository.findById(id);
	}

	// Saves advertisement
	@Override
	public void saveBusiness(Business business) {
		businessRepository.save(business);
	}

	// Downloads advertisement file
	@Override
	public ResponseEntity<Resource> downloadFile(String fileId) {
		try {
			Long id = Long.parseLong(fileId);
			Optional<Business> adOptional = businessRepository.findById(id);

			if (adOptional.isPresent()) {
				Business ad = adOptional.get();
				ByteArrayResource resource = new ByteArrayResource(ad.getAd());
				MediaType mediaType = determineMediaTypeFromAd(ad);

				return ResponseEntity.ok()
						.header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"file_" + id + "\"")
						.contentLength(ad.getAd().length).contentType(mediaType).body(resource);
			} else {
				return ResponseEntity.notFound().build();
			}
		} catch (NumberFormatException e) {
			return ResponseEntity.badRequest().build();
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}

	// Determines media type from advertisement
	private MediaType determineMediaTypeFromAd(Business ad) {
		String fileExtension = "mp4";
		switch (fileExtension.toLowerCase()) {
		case "mp4":
			return MediaType.parseMediaType("video/mp4");
		case "webm":
			return MediaType.parseMediaType("video/webm");
		default:
			return MediaType.APPLICATION_OCTET_STREAM;
		}
	}

	// Retrieves all advertisements
	@Override
	public List<Business> getAllAds() {
		return businessRepository.findAll();
	}

	// Deletes advertisement by ID
	@Override
	public void deleteAd(Long id) {
		businessRepository.deleteById(id);
	}

	// Updates advertisement
	@Override
	public void updateAd(Long adId, String subscription, String distanceRange, String latitude, String longitude, String email) {
	    Optional<Business> optionalAd = businessRepository.findById(adId);
	    if (!optionalAd.isPresent()) {
	        throw new AdNotFoundException("Ad not found with ID: " + adId);
	    }

	    Business existingAd = optionalAd.get();

	    if (!existingAd.getemail().equals(email)) {
	        throw new UnauthorizedOperationException("You are not authorized to update this ad.");
	    }

	    if (subscription != null && !subscription.isEmpty()) {
	        existingAd.setSubscription(subscription);
	    }
	    if (distanceRange != null && !distanceRange.isEmpty()) {
	        existingAd.setDistanceRange(distanceRange);
	    }
	    if (latitude != null && !latitude.isEmpty()) {
	        existingAd.setlatitude(latitude);
	    }
	    if (longitude != null && !longitude.isEmpty()) {
	        existingAd.setlatitude(longitude);
	    }

	    // Calculate payment amount
	    double paymentAmount = calculatePayment(subscription.split(","), distanceRange.split(","));
	    existingAd.setPaymentAmount(paymentAmount);

	    businessRepository.save(existingAd);
	}
	
	// Retrieves advertisements by email
	@Override
	public List<Business> getAdsByEmail(String email) {
	    return businessRepository.findByEmail(email);
	}


	// Custom exception for unauthorized operation
	public class UnauthorizedOperationException extends RuntimeException {
		public UnauthorizedOperationException(String message) {
			super(message);
		}
	}

	// Custom exception for ad not found
	public class AdNotFoundException extends RuntimeException {
		public AdNotFoundException(String message) {
			super(message);
		}
	}

}

package com.bytes.service;

import java.util.List;
import java.util.Optional;

import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import com.bytes.model.Business;

public interface BusinessService {
	String[] getAvailableSubscriptions();

	String[] getAvailableRanges();

	double calculatePayment(String[] subscription, String[] distanceRange);
	
	public void uploadAds(MultipartFile file, String subscription, String distanceRange, String latitude,
            String longitude, String email);
	
	public boolean validateEmail(String loggedInUserEmail, String videoUploaderEmail);

	public boolean isValidOption(String option, String[] validOptions);

	void saveBusiness(Business business);

	public Optional<Business> getAdById(Long id);

    public ResponseEntity<Resource> downloadFile(String fileId);
    
	public List<Business> getAllAds();
	
    public void deleteAd(Long id);
    
    public void updateAd(Long adId, MultipartFile file, String subscription, String distanceRange,
            String latitude, String longitude, String email);
}

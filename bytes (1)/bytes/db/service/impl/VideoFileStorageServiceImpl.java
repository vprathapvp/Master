package com.bezkoder.spring.files.upload.db.service.impl;

import java.io.IOException;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.bezkoder.spring.files.upload.db.model.VideoFileDB;
import com.bezkoder.spring.files.upload.db.repository.VideoFileDBRepository;
import com.bezkoder.spring.files.upload.db.service.VideoFileStorageService;

@Service
public class VideoFileStorageServiceImpl implements VideoFileStorageService {

    @Autowired
    private VideoFileDBRepository videoFileDBRepository;



	@Override
	public VideoFileDB getFile(String id) {
		   return videoFileDBRepository.findById(id).orElse(null);
	}

	@Override
	public Stream<VideoFileDB> getAllFiles() {
	      return videoFileDBRepository.findAll().stream();
		
	}

	@Override
	public VideoFileDB store(MultipartFile file, String email, String firstName, String lastName, String phone,
			Long latitude, Long longitude) throws IOException {
		String fileName = StringUtils.cleanPath(file.getOriginalFilename());
      VideoFileDB fileDB = new VideoFileDB(fileName, file.getContentType(), file.getBytes());
      fileDB.setEmail(email);
      fileDB.setFirstName(firstName);
      fileDB.setLastName(lastName);
      fileDB.setPhone(phone);
      fileDB.setLatitude(latitude);
      fileDB.setLongitude(longitude);
      return videoFileDBRepository.save(fileDB);
	}


}

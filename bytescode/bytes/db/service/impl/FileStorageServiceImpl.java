package com.bezkoder.spring.files.upload.db.service.impl;

import java.io.IOException;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.bezkoder.spring.files.upload.db.model.FileDB;
import com.bezkoder.spring.files.upload.db.repository.FileDBRepository;
import com.bezkoder.spring.files.upload.db.service.FileStorageService;

@Service
public class FileStorageServiceImpl implements FileStorageService {

    @Autowired
    private FileDBRepository fileDBRepository;

    public FileDB store(MultipartFile file, String email, String firstName, String lastName, String phone, Long latitude, Long longitude) throws IOException {
        String fileName = StringUtils.cleanPath(file.getOriginalFilename());
        FileDB fileDB = new FileDB(fileName, file.getContentType(), file.getBytes());
        fileDB.setEmail(email);
        fileDB.setFirstName(firstName);
        fileDB.setLastName(lastName);
        fileDB.setPhone(phone);
        fileDB.setLatitude(latitude);
        fileDB.setLongitude(longitude);
        return fileDBRepository.save(fileDB);
    }

    public FileDB getFile(String id) {
        return fileDBRepository.findById(id).orElse(null);
    }

    public Stream<FileDB> getAllFiles() {
        return fileDBRepository.findAll().stream();
    }
}

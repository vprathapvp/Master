package com.bezkoder.spring.files.upload.db.service;

import java.io.IOException;
import java.util.stream.Stream;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.bezkoder.spring.files.upload.db.model.FileDB;


@Service
public interface FileStorageService {


    public FileDB store(MultipartFile file, String email, String firstName, String lastName, String phone,
			Long latitude, Long longitude) throws IOException;

	public FileDB getFile(String id);

	public Stream<FileDB> getAllFiles();
}

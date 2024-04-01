package com.bezkoder.spring.files.upload.db.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import com.bezkoder.spring.files.upload.db.model.FileDB;
import com.bezkoder.spring.files.upload.db.repository.FileDBRepository;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import java.util.stream.Stream;

@Service
public class FileStorageService {

    @Autowired
    private FileDBRepository fileDBRepository;

    @Value("${upload.directory}")
    private String uploadDir;

    public FileDB store(MultipartFile file) throws IOException {
        String fileName = StringUtils.cleanPath(file.getOriginalFilename());

        // Generate a unique file name to prevent naming conflicts
        String uniqueFileName = UUID.randomUUID().toString() + "_" + fileName;
        
        // Path to save the file
        Path filePath = Paths.get(uploadDir + "/" + uniqueFileName);
        
        // Save the file to the local file system
        Files.write(filePath, file.getBytes());

        // Save file details to the database
        FileDB fileDB = new FileDB(uniqueFileName, file.getContentType(), filePath.toString());
        return fileDBRepository.save(fileDB);
    }

    public FileDB getFile(String id) {
        return fileDBRepository.findById(id).orElse(null);
    }

    public Stream<FileDB> getAllFiles() {
        return fileDBRepository.findAll().stream();
    }
}

package com.bezkoder.spring.files.upload.db.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.bezkoder.spring.files.upload.db.message.ResponseVideoFile;
import com.bezkoder.spring.files.upload.db.message.ResponseVideoMessage;
import com.bezkoder.spring.files.upload.db.model.VideoFileDB;
import com.bezkoder.spring.files.upload.db.service.VideoFileStorageService;

@RestController
@RequestMapping("/api")
@CrossOrigin("http://localhost:8081")
public class VideoController {

    @Autowired
    private VideoFileStorageService storageService;

    @PostMapping("/upload")
    public ResponseEntity<ResponseVideoMessage> uploadFile(@RequestParam("video") MultipartFile file,
                                                       @RequestParam("email") String email,
                                                       @RequestParam("firstName") String firstName,
                                                       @RequestParam("lastName") String lastName,
                                                       @RequestParam("phone") String phone,
                                                       @RequestParam("latitude") Long latitude,
                                                       @RequestParam("longitude") Long longitude) {
        String message = "";
        try {
            // Check if the uploaded file is a video
            if (!file.getContentType().startsWith("video/")) {
                message = "Only video files are allowed!";
                System.err.println("Invalid file type: " + file.getContentType());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseVideoMessage(message));
            }

            // Check the duration of the video
            long videoDurationInSeconds = getVideoDurationInSeconds(file);
            if (videoDurationInSeconds != 30) {
                message = "Uploaded video should be exactly 30 seconds long!";
                System.err.println("Invalid video duration: " + videoDurationInSeconds);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseVideoMessage(message));
            }

            // Save file and user info
            storageService.store(file, email, firstName, lastName, phone, latitude, longitude);

            message = "Uploaded the file successfully: " + file.getOriginalFilename();
            System.out.println("Upload successful: " + file.getOriginalFilename());
            return ResponseEntity.status(HttpStatus.OK).body(new ResponseVideoMessage(message));
        } catch (Exception e) {
            message = "Could not upload the file: " + file.getOriginalFilename() + "!";
            System.err.println("Error uploading file: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ResponseVideoMessage(message));
        }
    }

    @GetMapping("/files")
    public ResponseEntity<List<ResponseVideoFile>> getListFiles() {
        try {
            List<ResponseVideoFile> files = storageService.getAllFiles().map(dbFile -> {
                String fileDownloadUri = ServletUriComponentsBuilder
                        .fromCurrentContextPath()
                        .path("/files/")
                        .path(dbFile.getId())
                        .toUriString();

                return new ResponseVideoFile(
                        dbFile.getName(),
                        fileDownloadUri,
                        dbFile.getType(),
                        dbFile.getData().length);
            }).collect(Collectors.toList());

            return ResponseEntity.status(HttpStatus.OK).body(files);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/files/{id}")
    public ResponseEntity<byte[]> getFile(@PathVariable String id) {
        try {
            VideoFileDB fileDB = storageService.getFile(id);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileDB.getName() + "\"")
                    .body(fileDB.getData());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    // Helper method to get video duration in seconds
    private long getVideoDurationInSeconds(MultipartFile videoFile) {
        // Your code to get the video duration in seconds
        // This can be implemented using a library like FFmpeg or MediaMetadataRetriever
        // Here's a pseudo-code for demonstration:
        // FFmpegProbeResult probeResult = FFmpegProbeResult = FFmpegProbeResult.probeInput(videoFile.getInputStream());
        // long durationInSeconds = probeResult.getFormat().duration;
        // return durationInSeconds;
        return 30; // Returning a dummy value for demonstration
    }
}
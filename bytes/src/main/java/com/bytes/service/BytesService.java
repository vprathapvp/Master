package com.bytes.service;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.bytes.message.ResponseBytesMessage;
import com.bytes.message.ResponseGetall;
import com.bytes.message.ResponseMetadata;
import com.bytes.model.Bytes;

@Service
public interface BytesService {

	Bytes getFile(String id);

	public String updateFileDetails(String id, ResponseMetadata request);

	ResponseMetadata getFileDetailsById(String id);

	List<Bytes> getAllFiles();

	List<Bytes> getFilesByEmail(String email);

	public ResponseBytesMessage uploadFile(File file, String email, String description, String latitude,
			String longitude);

	List<ResponseGetall> searchFilesByDescription(String keyword);

	ResponseEntity<Resource> downloadFile(String fileId);

	Bytes updateFileDetails(String id, String newFileName, String newDescription);

	Optional<Bytes> getFileById(String id);

	void deleteFile(String id);

	void postComment(String Id, String firstName, String lastName, String comment);

	String getCommentsJson(String id);

	void deleteAllFiles();

	String getUploaderIdForVideo(String videoId);

	boolean validateEmail(String loggedInUserEmail, String videoUploaderEmail);

	void save(Bytes bytes);

	boolean validateEmail(String email);

	public void likeFile(String id, String email);

	public void dislikeFile(String id, String email);

	public List<Bytes> getSubscriptionVideos(Authentication authentication);

	public long getVideoDuration(File file) throws IOException;

	public String getUploaderIdByEmail(String email);

}
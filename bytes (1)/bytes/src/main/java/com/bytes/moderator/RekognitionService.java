package com.bytes.moderator;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.rekognition.AmazonRekognition;
import com.amazonaws.services.rekognition.AmazonRekognitionClientBuilder;
import com.amazonaws.services.rekognition.model.GetContentModerationRequest;
import com.amazonaws.services.rekognition.model.GetContentModerationResult;
import com.amazonaws.services.rekognition.model.S3Object;
import com.amazonaws.services.rekognition.model.StartContentModerationRequest;
import com.amazonaws.services.rekognition.model.StartContentModerationResult;
import com.amazonaws.services.rekognition.model.Video;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;

@Service
public class RekognitionService {

	private final AmazonRekognition rekognitionClient;
	private final AmazonS3 s3Client;
	private final String bucketName;

	public RekognitionService(@Value("${aws.accessKeyId}") String accessKey,
			@Value("${aws.secretKey}") String secretKey) {
		BasicAWSCredentials awsCreds = new BasicAWSCredentials(accessKey, secretKey);
		this.rekognitionClient = AmazonRekognitionClientBuilder.standard().withRegion(Regions.US_EAST_1)
				.withCredentials(new AWSStaticCredentialsProvider(awsCreds)).build();

		this.s3Client = AmazonS3ClientBuilder.standard().withRegion(Regions.US_EAST_1)
				.withCredentials(new AWSStaticCredentialsProvider(awsCreds)).build();

		this.bucketName = "moderation";
	}

	public boolean moderateVideo(File videoFile) throws IOException {
		String objectKey = UUID.randomUUID().toString(); // Use UUID for unique object key

		try {
			// Upload video to moderation S3 bucket
			s3Client.putObject(bucketName, objectKey, videoFile);

			StartContentModerationRequest request = new StartContentModerationRequest()
					.withVideo(new Video().withS3Object(new S3Object().withBucket(bucketName).withName(objectKey)))
					.withMinConfidence(75F);

			StartContentModerationResult result = rekognitionClient.startContentModeration(request);

			// Wait for the job to complete
			String jobId = result.getJobId();
			GetContentModerationResult moderationResult;
			do {
				try {
					Thread.sleep(10000); // Adjust as needed
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				moderationResult = rekognitionClient
						.getContentModeration(new GetContentModerationRequest().withJobId(jobId));
			} while (moderationResult.getJobStatus().equals("IN_PROGRESS"));

			// Check if any moderation labels were detected
			boolean isSafe = moderationResult.getModerationLabels().isEmpty();

			// Delete the video from S3 bucket to avoid billing
			s3Client.deleteObject(bucketName, objectKey);

			// Return whether the video is safe
			return isSafe;
		} finally {
			// Delete the uploaded video object from S3 bucket
			s3Client.deleteObject(bucketName, objectKey);
		}
	}
}
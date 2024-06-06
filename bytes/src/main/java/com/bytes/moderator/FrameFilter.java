package com.bytes.moderator;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.amazonaws.AmazonClientException;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.rekognition.AmazonRekognition;
import com.amazonaws.services.rekognition.AmazonRekognitionClientBuilder;
import com.amazonaws.services.rekognition.model.DetectModerationLabelsRequest;
import com.amazonaws.services.rekognition.model.DetectModerationLabelsResult;
import com.amazonaws.services.rekognition.model.Image;
import com.amazonaws.services.rekognition.model.ModerationLabel;

public class FrameFilter {

	private static AmazonRekognition rekognitionClient;

	static {
		rekognitionClient = AmazonRekognitionClientBuilder.standard().withRegion(Regions.AP_SOUTHEAST_1) // Ensure the correct region																							
				.build();
	}

	public static List<String> extractAndFilterFrames(File videoFile, String outputDir)
			throws IOException, InterruptedException {
		List<String> extractedFrames = extractFrames(videoFile, outputDir);
		return filterFrames(outputDir, extractedFrames);
	}

	private static List<String> filterFrames(String outputDir, List<String> extractedFrames) throws IOException {
		List<String> sensitiveFrames = new ArrayList<>();
		File outputDirectory = new File(outputDir);

		if (!outputDirectory.isDirectory()) {
			throw new IOException("The specified output directory does not exist or is not a directory: " + outputDir);
		}

		extractedFrames.parallelStream().forEach(frameFileName -> {
			File frameFile = new File(outputDirectory, frameFileName);
			if (frameFile.isFile() && isImageFile(frameFile)) {
				if (containsInappropriateContent(frameFile)) {
					synchronized (sensitiveFrames) {
						sensitiveFrames.add(frameFile.getName());
					}
				}
			}
		});

		return sensitiveFrames;
	}

	private static boolean isImageFile(File file) {
		String[] validExtensions = { "png", "jpg", "jpeg", "bmp", "gif" };
		String fileName = file.getName().toLowerCase();
		for (String ext : validExtensions) {
			if (fileName.endsWith(ext)) {
				return true;
			}
		}
		return false;
	}

	public static boolean containsInappropriateContent(File imageFile) {
		try {
			byte[] bytes = Files.readAllBytes(imageFile.toPath());
			Image image = new Image().withBytes(ByteBuffer.wrap(bytes));

			DetectModerationLabelsRequest request = new DetectModerationLabelsRequest().withImage(image)
					.withMinConfidence(75F);

			DetectModerationLabelsResult result = rekognitionClient.detectModerationLabels(request);
			List<ModerationLabel> labels = result.getModerationLabels();

			for (ModerationLabel label : labels) {
				System.out.println("Label: " + label.getName() + ", Confidence: " + label.getConfidence());
				if (label.getName().equalsIgnoreCase("Explicit Nudity")
						|| label.getName().equalsIgnoreCase("Suggestive")
						|| label.getName().equalsIgnoreCase("Non-Explicit Nudity")
						|| label.getName().equalsIgnoreCase("Partially Exposed Buttocks")
						|| label.getName().equalsIgnoreCase("Violence")
						|| label.getName().equalsIgnoreCase("Graphic Violence")
						|| label.getName().equalsIgnoreCase("Gore")
						|| label.getName().equalsIgnoreCase("Weapon Violence")) {
					return true;
				}
			}
		} catch (AmazonClientException e) {
			System.err.println("Amazon Client Exception: Unable to execute request to Rekognition service.");
			e.printStackTrace();
		} catch (IOException e) {
			System.err.println("IOException: Error reading the image file.");
			e.printStackTrace();
		} catch (Exception e) {
			System.err.println("Unexpected Exception: " + e.getMessage());
			e.printStackTrace();
		}

		return false;
	}

	private static List<String> extractFrames(File videoFile, String outputDir)
			throws IOException, InterruptedException {
		File outputDirectory = new File(outputDir);
		if (!outputDirectory.exists()) {
			if (!outputDirectory.mkdirs()) {
				throw new IOException("Failed to create output directory: " + outputDir);
			}
		}

		String videoFilePath = videoFile.getAbsolutePath();
		Path outputPath = Paths.get(outputDir, "frame_%04d.png");

		ProcessBuilder processBuilder = new ProcessBuilder("ffmpeg", "-i", videoFilePath, "-vf", "fps=2",
				outputPath.toString());

		processBuilder.redirectErrorStream(true);
		Process process = processBuilder.start();

		try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
			String line;
			while ((line = reader.readLine()) != null) {
				System.err.println(line);
			}
		}

		if (!process.waitFor(2, TimeUnit.MINUTES)) {
			throw new IOException("FFmpeg process timed out");
		}

		int exitCode = process.exitValue();
		if (exitCode != 0) {
			throw new IOException("FFmpeg process failed with exit code " + exitCode);
		}

		return Files.list(Paths.get(outputDir)).filter(path -> path.toString().endsWith(".png")).map(Path::getFileName)
				.map(Path::toString).collect(Collectors.toList());
	}

	public static void main(String[] args) {
		if (args.length != 2) {
			System.err.println("Usage: java FrameFilter <videoFile> <outputDir>");
			System.exit(1);
		}

		String videoFilePath = args[0];
		String outputDir = args[1];

		try {
			File videoFile = new File(videoFilePath);
			List<String> sensitiveFrames = extractAndFilterFrames(videoFile, outputDir);
			if (sensitiveFrames.isEmpty()) {
				System.out.println("No sensitive content found.");
			} else {
				System.out.println("Sensitive content found in frames: " + sensitiveFrames);
			}
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
	}
}

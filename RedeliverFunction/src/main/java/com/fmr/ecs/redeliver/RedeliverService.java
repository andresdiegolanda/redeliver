package com.fmr.ecs.redeliver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.fasterxml.jackson.databind.ObjectMapper;

public class RedeliverService {

	private static final Logger logger = LogManager.getLogger(RedeliverService.class);

	public static final String JWT_TOKEN = "eyJhbGciOiJSUzI1NiJ9.eyJpYXQiOjE2NzQ4MjA1NjQsImV4cCI6MTY3NDgyMTIyNCwiaXNzIjoiMjI2NzI1In0.aKsMQtHus-HEdm_OxTBPFGkKoI-gQvfNzWcnJyt5F-ud0CfL0PHxrZ-S5I_MkWfbnzaASd-Hc_m_nIELD1KbmFYLV7jteixBcMWDJuFrFmh2XGebEf7d1vBRkHtxqBgwpDC4IaN9M0oMfKcprSuGY22awFr9PAmNxkKaNoQJfWoqMkd86ljgznUf2bqYvC7SZoYZGBsdW8beRVc7RE1soIHGu1xHXrqo5j-DFLoa6XDI6ajvmwWvnG7ClSVTHJgwXNCi7qRbE8YpM7GQeL6_AYIWgSoTEWtox20OYWgBiL7PuGhBnPtsQalXF7Wr6K3hNkgIKcT8C4Caz2ilyH6jVw";

	public static final String DELIVERIES_URL = "https://api.github.com/app/hook/deliveries";

	public String redeliver() {
		try {
			String result = "result:" + getDeliveries().body() + "--at:" + LocalDateTime.now();
			String watermark = readWatermark();
			processDeliveries(getDeliveries().body(), watermark);
			logger.info("@@@Result:{} ", result);

			return result;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public String readWatermark() throws IOException {
		Regions clientRegion = Regions.EU_WEST_1;
		String bucketName = "redeliverpocbucket0";
		String key = "watermark.txt";
		String fileContent = "";

		S3Object fullObject = null;
		try {
			AmazonS3 s3Client = AmazonS3ClientBuilder.standard().withRegion(clientRegion)
					.withCredentials(new ProfileCredentialsProvider()).build();

			// Get an object and print its contents.
			logger.info("@@@Downloading watermark");
			fullObject = s3Client.getObject(new GetObjectRequest(bucketName, key));
			// displayTextInputStream(fullObject.getObjectContent());
			BufferedReader reader = new BufferedReader(new InputStreamReader(fullObject.getObjectContent()));
			fileContent = reader.lines().collect(java.util.stream.Collectors.joining());
			logger.info("@@@Watermark: " + fileContent);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			// To ensure that the network connection doesn't remain open, close any open
			// input streams.
			if (fullObject != null) {
				fullObject.close();
			}
		}
		return fileContent;
	}

	private void processDeliveries(String body, String watermak)
			throws URISyntaxException, IOException, InterruptedException {
		ObjectMapper mapper = new ObjectMapper();
		List<Map<String, Object>> deliveries = mapper.readValue(body, List.class);
		Map<String, Object> firstDelivery = deliveries.get(0);
		HttpClient client = HttpClient.newBuilder().version(HttpClient.Version.HTTP_2).build();
		HttpRequest request = HttpRequest
				.newBuilder(new URI(DELIVERIES_URL + "/" + firstDelivery.get("id") + "/attempts"))
				.headers("Accept", "application/vnd.github+json", "X-GitHub-Api-Version", "2022-11-28", "Authorization",
						"Bearer " + JWT_TOKEN)
				.version(HttpClient.Version.HTTP_2).POST(BodyPublishers.ofString("")).build();
		HttpResponse<String> response = client.send(request, BodyHandlers.ofString());
		logger.info("@@@@@deliveries:{}", deliveries);

	}

	public HttpResponse<String> getDeliveries() throws URISyntaxException, IOException, InterruptedException {
		HttpClient client = HttpClient.newHttpClient();
		HttpRequest request = HttpRequest.newBuilder().version(HttpClient.Version.HTTP_2)
				.uri(URI.create(DELIVERIES_URL)).headers("Accept", "application/vnd.github+json",
						"X-GitHub-Api-Version", "2022-11-28", "Authorization", "Bearer " + JWT_TOKEN)
				.build();
		HttpResponse<String> response = client.send(request, BodyHandlers.ofString());

		String responseBody = response.body();
		int responseStatusCode = response.statusCode();

		logger.info("@@@Response body: {}", responseBody);
		logger.info("@@@Response status code: {}", responseStatusCode);
		return response;
	}
}

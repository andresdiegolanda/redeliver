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
	public static final String JWT_TOKEN = "eyJhbGciOiJSUzI1NiJ9.eyJpYXQiOjE2NzQ4MzU4NDUsImV4cCI6MTY3NDgzNjUwNSwiaXNzIjoiMjI2NzI1In0.vzhO8GQiZoMzCE9k2a2sIhccQzpH_z52DFcLAGdg6t1PM4VVL086-mCRClhNDYuGUQNMakXanjpEnjPxrS1Y6iQ1dkkLD3sw8TwDp_uUNKgjZlsxPfCT79BL08_PyMQUA1PxIEBcJU-qldxadyPPVxtrqcTSgxO2rGdyRGrAbCHPc9HjbeyqK1348r0TrEmeqBCqkfCkuZVwOuR4rkYkAPWMoX1zF0-lQs5jO4dExAw_mXKVMD7y1BwyuSJWyDIORlSxJ7NUcu5GJ1OrXWE4wmcBzexBeGAIpedHXKkvlpjKgdt-saTuKVu2Il_LyyEf5QfqMRyB6TcsYZsEcDpQbg";

	/* client for local pc */
	private static final AmazonS3 s3Client = AmazonS3ClientBuilder.standard().withRegion(Regions.EU_WEST_1)
			.withCredentials(new ProfileCredentialsProvider()).build();
	/* client for lambda */
//	private static final AmazonS3 s3Client = AmazonS3ClientBuilder.standard()
//			.withCredentials(DefaultAWSCredentialsProviderChain.getInstance()).build();
	private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder().version(HttpClient.Version.HTTP_2).build();
	public static final String DELIVERIES_URL = "https://api.github.com/app/hook/deliveries";
	public static final String BUCKET_NAME = "redeliverpocbucket0";
	public static final String KEY = "watermark.txt";
	public static ObjectMapper mapper = new ObjectMapper();

	public String redeliver() {
		try {
			String body = getDeliveries().body();
			String result = "result:" + body + "--at:" + LocalDateTime.now();
			Long watermark = Long.parseLong(readWatermark());
			List<Map<String, Object>> deliveries = mapper.readValue(body, List.class);
			processDeliveries(deliveries, watermark);
			writeWatermark(deliveries);
			logger.info("@@@Result:{} ", result);
			return result;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private void writeWatermark(List<Map<String, Object>> deliveries) {
		String watermark = deliveries.get(0).get("id").toString();
		s3Client.putObject(BUCKET_NAME, KEY, watermark);
		logger.info("@@@new watermark:{} ", watermark);

	}

	public String readWatermark() throws IOException {
		String fileContent = "";
		S3Object fullObject = null;
		try {
			// Get an object and print its contents.
			logger.info("@@@Downloading watermark");
			fullObject = s3Client.getObject(new GetObjectRequest(BUCKET_NAME, KEY));
			// displayTextInputStream(fullObject.getObjectContent());
			BufferedReader reader = new BufferedReader(new InputStreamReader(fullObject.getObjectContent()));
			fileContent = reader.lines().collect(java.util.stream.Collectors.joining());
			logger.info("@@@Watermark: " + fileContent);
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			// To ensure that the network connection doesn't remain open, close any open
			// input streams.
			if (fullObject != null) {
				fullObject.close();
			}
		}
		return fileContent;
	}

	private void processDeliveries(List<Map<String, Object>> deliveries, Long watermark)
			throws URISyntaxException, IOException, InterruptedException {
		for (Map<String, Object> delivery : deliveries) {
			if (delivery.get("id").equals(watermark)) {
				logger.info("@@@@@watermark found!");
				break;
			} else {
				HttpRequest request = HttpRequest
						.newBuilder(new URI(DELIVERIES_URL + "/" + delivery.get("id") + "/attempts"))
						.headers("Accept", "application/vnd.github+json", "X-GitHub-Api-Version", "2022-11-28",
								"Authorization", "Bearer " + JWT_TOKEN)
						.version(HttpClient.Version.HTTP_2).POST(BodyPublishers.ofString("")).build();
				HttpResponse<String> response = HTTP_CLIENT.send(request, BodyHandlers.ofString());
				logger.info("@@@@@response:{}", response);
			}
		}
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

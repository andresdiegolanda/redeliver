package com.fmr.ecs.redeliver;

import java.io.IOException;
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

import com.fasterxml.jackson.databind.ObjectMapper;

public class RedeliverService {

	private static final Logger logger = LogManager.getLogger(RedeliverService.class);

	public static final String JWT_TOKEN = "eyJhbGciOiJSUzI1NiJ9.eyJpYXQiOjE2NzQ3NTEwNzQsImV4cCI6MTY3NDc1MTczNCwiaXNzIjoiMjI2NzI1In0.ZFD_7LA7GYdTVp8mmgaLkQEWpdxJIGhOzsRH2K2aeboMyX8odasp1qRY_99s-ov0oJ3SuACNHJACCDTq8v8Fltoe8Xk0V7k5EsaOzoYU5sb3gxcy1bs3q5f9eixeqFDhqsj97fnjta61d3Qo055p4h8aGOBuSreeMJkYspMamsCSrnMU4CJkCkGr7HwKDRwnv0LUEbx5_QXZOLNpD81v2ZlqOBapQ3y9xMhoqgoV_TUEvf5RsWvkkK4OYhqcp1xAHZ8UXMcIAKxWyZP4k33nwilQUbI0lhoeFKwJaRTW5G3d0y-r0Ig7-yooH4YxOX-Xqv4B18iFOQGQ8WldyJ660g";

	public static final String DELIVERIES_URL = "https://api.github.com/app/hook/deliveries";

	public String redeliver() {
		try {
			String result = "result:" + getDeliveries().body() + "--at:" + LocalDateTime.now();
			processDeliveries(getDeliveries().body());
			logger.info("@@@Result:{} ", result);

			return result;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private void processDeliveries(String body) throws URISyntaxException, IOException, InterruptedException {
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

package com.fmr.ecs.redeliver;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.LocalDateTime;

public class RedeliverService {

	public static final String jwtToken = " eyJhbGciOiJSUzI1NiJ9.eyJpYXQiOjE2NzQwNDA1MjIsImV4cCI6MTY3NDA0MTE4MiwiaXNzIjoiMjI2NzI1In0.al5DVTttr2X_i0t4X7EPbrHD2XEwu9dPk82j9J7i2qGfPexC7MIujFlV9wzHGbmwN5JnVIqQWkuOJcw3-Jwj5K1dczam1Eq3ZPYCIoIA55E6uCsdB1xWjH__2683zhGiTCoXZVZ6ErZVQ8OlWzIDhAbub2JtRipFvsVyMqI7N1uINTHR_13CwmzUVb643BCG9cKohF7Y0bf7DhkkReH1aHxKyuxSVfY1A4GXs5QxJDoQLcLsPG3LcKczcQoODwA19ot6RMgOY8N7wf-jM96ieNCws_ykKy34OwKS96SOzc5sOYjWn48szVkG7YLrbS5Dz8Qwz59P8mrQz9QmWL3YfA";

	public String redeliver() {
		try {
			String result = "result:" + getDeliveries() + "--at:" + LocalDateTime.now();
			System.out.println("@@@Result: " + result);
			return result;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public String getDeliveries() throws URISyntaxException, IOException, InterruptedException {
		HttpClient client = HttpClient.newHttpClient();
		HttpRequest request = HttpRequest.newBuilder().version(HttpClient.Version.HTTP_2)
				.uri(URI.create("https://api.github.com/app/hook/deliveries"))
				.headers("Accept", "application/vnd.github+json", "X-GitHub-Api-Version", "2022-11-28", "Authorization",
						"Bearer " + jwtToken)
				.build();
		HttpResponse<String> response = client.send(request, BodyHandlers.ofString());

		String responseBody = response.body();
		int responseStatusCode = response.statusCode();

		System.out.println("@@@Response body: " + responseBody);
		System.out.println("@@@Response status code: " + responseStatusCode);
		return Integer.valueOf(responseStatusCode).toString();
	}
}

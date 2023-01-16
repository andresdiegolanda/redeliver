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

	public String redeliver() {
		try {
			httpGetRequest();
			return "result ok --" + LocalDateTime.now();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static void httpGetRequest() throws URISyntaxException, IOException, InterruptedException {
		HttpClient client = HttpClient.newHttpClient();
		HttpRequest request = HttpRequest.newBuilder().version(HttpClient.Version.HTTP_2)
				.uri(URI.create("http://jsonplaceholder.typicode.com/posts/1"))
				.headers("Accept-Enconding", "gzip, deflate").build();
		HttpResponse<String> response = client.send(request, BodyHandlers.ofString());

		String responseBody = response.body();
		int responseStatusCode = response.statusCode();

		System.out.println("httpGetRequest: " + responseBody);
		System.out.println("httpGetRequest status code: " + responseStatusCode);
	}
}

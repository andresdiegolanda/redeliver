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

	public static final String jwtToken = "eyJhbGciOiJSUzI1NiJ9.eyJpYXQiOjE2NzQ2NTI5NTksImV4cCI6MTY3NDY1MzYxOSwiaXNzIjoiMjI2NzI1In0.MZ-Rq5UMwYVqOX-WyT2N9Wvz3mvy1oEASlmgoa8nR6OfmssPo4UiOC1SiWNNxX4uTrHlHmlZn4yJWhz9ok6K79hB-6nHW22P5DMaSgEkOI7WqJPHCndmvKJ9ppGKoDJ37GE1J9mx1b8P8Uw3ylDjvz-W98J6xy-2yzcLaOqikZAM2s3ZRO_aTfMuyeE0mQXPttgbECUR5TAcRX52XG5eyobBKU8scH5FEfgtcI5o6zvPHwjhtMTGGUIkIwf7KImDxMR3amE5uUSZ2lXQgQ11htMI2iIBW8fOR1EmIilQ16bwxRRlgfqilIgLlCe9i7L-hgrJISndMWQ5at8epkGZbw";

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

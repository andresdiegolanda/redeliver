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

	public static final String jwtToken = "eyJhbGciOiJSUzI1NiJ9.eyJpYXQiOjE2NzQwNDIxNTcsImV4cCI6MTY3NDA0MjgxNywiaXNzIjoiMjI2NzI1In0.JWe_vSv4r7j7f5iEPo5aw_wo5xmnm1tM4O6Ozp8O_GavouBQpQY6UY9HV-j1iSzv0EhSWzEAxA2mPbzwFnxHiRgbCKdXSGs8I1raKQhxBuWMDN1TLD5npRg1qPJwR8pmjyq25tNLOegQ6b2qbHLdQrBAmcBTckHGErFmd074wlU41D2hsoqTTgk8jCPHkWfqJcrVnheMWdH-QNd9pYNeibXJ_eTOojIXY_4RAKT3-2o-j3JJPOQ806zskBt02-BDOyw0_T2ZmE9nHob_QmEsl8jeORk1XJrDKw4l30Ppg6IDV_s6nihkhXxIugb58ntqKyrbhY5cwNGMcaLP8hwdpw";

	public String redeliver() {
		try {
			getDeliveries();
			return "result ok --" + LocalDateTime.now();
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

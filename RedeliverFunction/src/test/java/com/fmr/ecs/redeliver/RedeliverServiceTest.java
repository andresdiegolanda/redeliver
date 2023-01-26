package com.fmr.ecs.redeliver;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.http.HttpResponse;

import org.junit.jupiter.api.Test;

public class RedeliverServiceTest {

	@Test
	public void shouldGetDeliveries() throws URISyntaxException, IOException, InterruptedException {
		RedeliverService redeliverService = new RedeliverService();
		HttpResponse<String> result = redeliverService.getDeliveries();
		assertEquals("200", "" + result.statusCode());
	}

	@Test
	public void shouldRedeliver() throws URISyntaxException, IOException, InterruptedException {
		RedeliverService redeliverService = new RedeliverService();
		String result = redeliverService.redeliver();
		assertTrue(result.startsWith("result:[{"));
	}

}

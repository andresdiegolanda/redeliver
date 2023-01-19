package com.fmr.ecs.redeliver;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.net.URISyntaxException;

import org.junit.Test;

public class RedeliverServiceTest {

	@Test
	public void shouldGetDeliveries() throws URISyntaxException, IOException, InterruptedException {
		RedeliverService redeliverService = new RedeliverService();
		String result = redeliverService.getDeliveries();
		assertEquals("200", result);
	}

}

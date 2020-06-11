package com.github.crgz.transactions.model;

import static org.junit.Assert.assertNotNull;
import org.junit.Test;
import io.vertx.core.json.Json;

/**
 * @author Conrado M.
 */
public class InputTest
{
	@Test
	public void test()
	{
		assertNotNull(Json.encodePrettily(new Input(1, 1)));

		assertNotNull(Json.decodeValue("{\"amount\" : 2,\"timestamp\" : 0}", Input.class));
	}
}

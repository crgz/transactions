package com.github.crgz.transactions.model;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import com.github.crgz.transactions.model.Statistics;
import io.vertx.core.json.Json;

/**
 * @author Conrado M.
 */
public class StatisticsTest
{
	@Test
	public void marshallUnmarshalTest()
	{
		assertNotNull(Json.encodePrettily(new Statistics(1, 1, 1, 1, 1)));
		
		assertNotNull(Json.decodeValue("{\"sum\" : 2,\"max\" : 0,\"min\" : 0,\"count\" : 2,\"avg\" : 0.0}", Statistics.class));
	}
}

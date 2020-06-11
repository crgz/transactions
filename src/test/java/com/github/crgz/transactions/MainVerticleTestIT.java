package com.github.crgz.transactions;

import static com.jayway.restassured.RestAssured.get;
import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.jayway.restassured.RestAssured;

/**
 * These tests checks our REST API.
 * 
 * @author Conrado M.
 */
public class MainVerticleTestIT
{
	private static final Logger logger = LogManager.getLogger(MainVerticleTestIT.class.getName());

	@BeforeClass
	public static void configureRestAssured()
	{
		RestAssured.baseURI = "http://localhost";
		final Integer port = Integer.getInteger("http.port", 8080);
		logger.info("Port in use: {}", port);

		RestAssured.port = port;
	}

	@AfterClass
	public static void unconfigureRestAssured()
	{
		RestAssured.reset();
	}

	@Test
	public void checkThatWeCanRetrieveStatistics() throws InterruptedException
	{

		long currentTime = Instant.now().minusSeconds(3).getEpochSecond();
		
		final int count = 10;
		final float max = count - 1;
		final float min = 0;
		final float sum = (max * max + max) / 2;
		final float avg = sum / count;
		
		logger.info("Hitting the service with {} requests", count);

		for (int i = 0; i < count; i++)
		{
			given().body("{\"amount\":\""+i+"\", \"timestamp\":\"" + (currentTime + i) + "\"}")
				.request().post("/transactions")
				.then()
				.assertThat().statusCode(201);
		}

		TimeUnit.MILLISECONDS.sleep(6000);

		// Now get the transactions and check the content
		get("/statistics").then()
			.assertThat()
			.statusCode(200)
			.body("sum", equalTo(sum))
			.body("max", equalTo(max))
			.body("min", equalTo(min))
			.body("count", equalTo(count))
			.body("avg", equalTo(avg));
	}
}

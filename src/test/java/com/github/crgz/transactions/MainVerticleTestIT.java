/*******************************************************************************
 * Copyright (C) 2017, Conrado M. Rodriguez
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * 3. Neither the name of the copyright holder nor the names of its contributors
 * may be used to endorse or promote products derived from this software
 * without specific prior written permission.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 ******************************************************************************/
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
	static final Logger logger = LogManager.getLogger(MainVerticleTestIT.class.getName());

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

		TimeUnit.MILLISECONDS.sleep(5000);

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

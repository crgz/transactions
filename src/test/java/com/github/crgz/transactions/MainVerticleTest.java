///*******************************************************************************
// * Copyright (C) 2017, Conrado M. Rodriguez
// * All rights reserved.
// * 
// * Redistribution and use in source and binary forms, with or without
// * modification, are permitted provided that the following conditions are met:
// * 
// * 1. Redistributions of source code must retain the above copyright notice,
// *    this list of conditions and the following disclaimer.
// * 
// * 2. Redistributions in binary form must reproduce the above copyright notice,
// *    this list of conditions and the following disclaimer in the documentation
// *    and/or other materials provided with the distribution.
// * 
// * 3. Neither the name of the copyright holder nor the names of its contributors
// *    may be used to endorse or promote products derived from this software
// *    without specific prior written permission.
// * 
// * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
// * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
// * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
// * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
// * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
// * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
// * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
// * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
// * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
// * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
// * POSSIBILITY OF SUCH DAMAGE.
// ******************************************************************************/
//package com.github.crgz.transactions;
//
//import com.github.crgz.transactions.model.RawEvent;
//import com.github.crgz.transactions.model.Statistics;
//import io.vertx.core.DeploymentOptions;
//import io.vertx.core.Vertx;
//import io.vertx.core.http.HttpClient;
//import io.vertx.core.json.Json;
//import io.vertx.core.json.JsonObject;
//import io.vertx.ext.unit.Async;
//import io.vertx.ext.unit.TestContext;
//import io.vertx.ext.unit.junit.VertxUnitRunner;
//
//import org.apache.logging.log4j.LogManager;
//import org.apache.logging.log4j.Logger;
//import org.junit.After;
//import org.junit.Before;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//
//import java.io.IOException;
//import java.net.ServerSocket;
//import java.time.Instant;
//
///**
// * This is our JUnit test for our verticle. The test uses vertx-unit, so we declare a custom runner.
// * These tests checks our REST API.
// * 
// * @author Conrado M.
// */
//@RunWith(VertxUnitRunner.class)
//public class MainVerticleTest
//{
//	private static final Logger logger = LogManager.getLogger(MainVerticle.class.getName());
//
//	private Vertx vertx;
//	private Integer port;
//
//	/**
//	 * Before executing our test, let's deploy our verticle.
//	 * <p/>
//	 * This method instantiates a new Vertx and deploy the verticle. Then, it waits in the verticle has successfully
//	 * completed its start sequence (thanks to `context.asyncAssertSuccess`).
//	 *
//	 * @param context the test context.
//	 */
//	@Before
//	public void setUp(TestContext context) throws IOException
//	{
//		vertx = Vertx.vertx();
//
//		// Let's configure the verticle to listen on the 'test' port (randomly picked).
//		// We create deployment options and set the _configuration_ json object:
//		ServerSocket socket = new ServerSocket(0);
//		port = socket.getLocalPort();
//		socket.close();
//
//		DeploymentOptions options = new DeploymentOptions()
//			.setConfig(new JsonObject().put("http.port", port));
//
//		// We pass the options as the second parameter of the deployVerticle method.
//		vertx.deployVerticle(MainVerticle.class.getName(), options, context.asyncAssertSuccess());
//	}
//
//	/**
//	 * This method, called after our test, just cleanup everything by closing the vert.x instance
//	 *
//	 * @param context the test context
//	 */
//	@After
//	public void tearDown(TestContext context)
//	{
//		vertx.close(context.asyncAssertSuccess());
//	}
//
//	/**
//	 * Let's ensure that our application behaves correctly.
//	 *
//	 * @param context the test context
//	 */
//	@Test
//	public void checkThatWeCanAddAndRetrieve(TestContext context)
//	{
//		Async async = context.async();
//		final RawEvent event = new RawEvent(Instant.now().toEpochMilli(), 1);
//		final String json = Json.encodePrettily(event);
//
//		final HttpClient client = vertx.createHttpClient();
//
//		client.post(port, "localhost", "/transactions")
//			.putHeader("content-type", "application/json")
//			.putHeader("content-length", Integer.toString(json.length()))
//			.handler(response ->
//			{
//				context.assertEquals(response.statusCode(), 201);
//				logger.info("Event:" + event);
//
//				client.getNow(port, "localhost", "/statistics", transactionsResponse ->
//				{
//					context.assertEquals(transactionsResponse.statusCode(), 200);
//					context.assertEquals(transactionsResponse.headers().get("content-type"), "application/json; charset=utf-8");
//					transactionsResponse.bodyHandler(body ->
//					{
//						final Statistics statistics = Json.decodeValue(body.toString(), Statistics.class);
//						context.assertEquals(statistics.getSum(), 1.0);
//						context.assertEquals(statistics.getAvg(), 1.0);
//						context.assertEquals(statistics.getMax(), 1.0);
//						context.assertEquals(statistics.getMin(),1.0);
//						context.assertEquals(statistics.getCount(), 1l);
//						async.complete();
//					});
//				});
//			})
//			.write(json)
//			.end();
//	}
//}

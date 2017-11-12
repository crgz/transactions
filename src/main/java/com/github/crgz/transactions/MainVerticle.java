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

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.github.crgz.transactions.model.Event;
import com.github.crgz.transactions.model.Input;
import com.github.crgz.transactions.model.Statistics;
import com.github.crgz.transactions.service.Controller;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.json.Json;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;

/**
 * Stream processing is important for analyzing continuous streams of data in real time. Sliding-window aggregation is both needed for many
 * streaming applications and surprisingly hard to do efficiently. Picking the wrong aggregation algorithm causes poor performance, and knowledge of
 * the right algorithms and when to use them is scarce. Sliding-window aggregation is a widely-used approach for extracting insights from the most
 * recent portion of a data stream.
 * 
 * @author Conrado M.
 */
public class MainVerticle extends AbstractVerticle
{
	private static final int DEFAULT_TIME_WINDOW = 60;
	private static final Logger logger = LogManager.getLogger(MainVerticle.class.getName());

	private Controller controller;

	/**
	 * This method is called when the verticle is deployed. It creates a HTTP server and registers a simple request
	 * handler.
	 * 
	 * @param future the future
	 */
	@Override
	public void start(Future<Void> future)
	{
		Router router = Router.router(vertx);

		router.route("/transactions*").handler(BodyHandler.create());
		router.post("/transactions").handler(this::transactions);
		router.get("/statistics").handler(this::statistics);

		vertx // Create the HTTP server and pass the "accept" method to the request handler.
			.createHttpServer()
			.requestHandler(router::accept)
			.listen( // Retrieve the port from the configuration, default to 8080.
				config().getInteger("http.port", 8080),
				result ->
				{
					if (result.succeeded())
					{
						future.complete();
					}
					else
					{
						future.fail(result.cause());
					}
				});

		Duration window = Duration.ofSeconds(config().getInteger("time.window.seconds", DEFAULT_TIME_WINDOW));
		logger.info("Collecting real time transactions of events for the last {} seconds", window.getSeconds());
		this.controller = new Controller(window);

	}

	private void transactions(RoutingContext routingContext)
	{
		// Read the request's content and create an instance of event.
		Event event = new Event(Json.decodeValue(routingContext.getBodyAsString(), Input.class));

		controller.accept(event);

		routingContext.response().setStatusCode(event.getDelay(TimeUnit.SECONDS) < DEFAULT_TIME_WINDOW ? 201 : 204).end();
	}

	private void statistics(RoutingContext routingContext)
	{
		final Statistics statistics = controller.get();
		logger.info("Statistics: " + statistics);
		routingContext.response().putHeader("content-type", "application/json; charset=utf-8").end(Json.encode(statistics));
	}

}

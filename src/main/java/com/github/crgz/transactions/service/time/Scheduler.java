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
package com.github.crgz.transactions.service.time;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.github.crgz.transactions.model.Event;

/**
 * The TimeWarpController handle time discrepancies in incoming events by calculation the difference between the event timestamp and the present.
 * This difference define a dynamic quarantine period. The events are stored in a priority queue and kept for the quarantine time. This time is
 * considered enough to catch up with other rambling events.
 * 
 * @author Conrado M.
 */
public class Scheduler
{
	private static final Logger logger = LogManager.getLogger(Scheduler.class.getName());

	private final DelayQueue<Event> queue = new DelayQueue<>();
	private final Consumer<Event> consumer;
	private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

	private Instant evacuationTime = Instant.now();

	public Scheduler(Consumer<Event> consumer)
	{
		this.consumer = consumer;
	}

	public synchronized Duration accept(Event event)
	{
		final Instant now = Instant.now();
		final Duration eventDelay = Duration.between(event.getInstant(), now);
		final Duration quarantine = Duration.between(now, this.evacuationTime);

		if (eventDelay.compareTo(quarantine) > 0)
		{
			this.evacuationTime = now.plus(eventDelay);
			logger.warn("Next evacuation time is: {}. Event {} must evacuate in {} ms.", this.evacuationTime, event.getAmount(),
				eventDelay.toMillis());
		}

		queue.put(event);

		this.executor.schedule(() ->
		{
			Event acceptable = null;
			try
			{
				acceptable = this.queue.take();
			}
			catch (InterruptedException e)
			{
				logger.error("tasks interrupted");
				Thread.currentThread().interrupt();
			}
			this.consumer.accept(acceptable);
			final Event nextEvent = this.queue.peek();
			this.evacuationTime = nextEvent.getInstant();
			logger.info("Consumed event {}. Next evacuation time is at least {}", acceptable, this.evacuationTime);

		}, quarantine.toMillis(), TimeUnit.MILLISECONDS);

		return eventDelay;
	}

	public void shutdown()
	{
		try
		{
			logger.info("attempt to shutdown executor");
			executor.shutdown();
			executor.awaitTermination(5, TimeUnit.SECONDS);
		}
		catch (InterruptedException e)
		{
			logger.info("tasks interrupted", e);
			Thread.currentThread().interrupt();
		}
		finally
		{
			if (!executor.isTerminated())
			{
				logger.error("Unablew to shutdown. Please cancel non-finished tasks");
			}
			executor.shutdownNow();
			logger.error("shutdown finished");
		}
	}
}

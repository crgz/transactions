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

import static org.junit.Assert.assertEquals;

import java.time.Duration;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.LongAdder;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

import com.github.crgz.transactions.model.Event;
import com.github.crgz.transactions.service.time.Scheduler;

import static org.awaitility.Awaitility.await;

/**
 * @author Conrado M.
 */
public class SchedulerTest
{
	static final Logger logger = LogManager.getLogger(SchedulerTest.class.getName());
	private final Set<Integer> result = new HashSet<Integer>();

	@Test
	public void testTimeDiscrepancyController() throws Exception
	{
		Scheduler resource = new Scheduler(event -> result.add(Double.valueOf(event.getAmount()).intValue()));

		LongAdder sleepTime = new LongAdder();
		int max = 10;
		final Instant now = Instant.now();
		
		IntStream.range(0, max).forEach(i ->
		{
			Instant backInTime = now.minusMillis(100 * i);
			Event event = new Event(backInTime, max - i - 1);
			Duration duration = Duration.between(now, event.getInstant());
			logger.info("Submiting event {} with relative time {} ms and delay of: {}ms", event, duration.toMillis(),
				event.getDelay(TimeUnit.MILLISECONDS));
			sleepTime.add(resource.accept(event).toMillis());
		});

		await().atMost(sleepTime.longValue(), TimeUnit.MILLISECONDS).until(allEventsAreSent());

		assertEquals(IntStream.range(0, max).boxed().collect(Collectors.toSet()), result);
	}

	private Callable<Boolean> allEventsAreSent()
	{
		return new Callable<Boolean>()
		{
			public Boolean call() throws Exception
			{
				return result.size() == 10; // The condition that must be fulfilled
			}
		};
	}
}

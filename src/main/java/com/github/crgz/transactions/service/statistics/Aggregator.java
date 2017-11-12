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
package com.github.crgz.transactions.service.statistics;

import java.time.Duration;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.DoubleAdder;
import java.util.concurrent.atomic.LongAdder;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.github.crgz.transactions.model.Event;
import com.github.crgz.transactions.model.Statistics;
import com.github.crgz.transactions.service.statistics.dispersion.Maximizer;
import com.github.crgz.transactions.service.statistics.dispersion.Minimizer;
import com.github.crgz.transactions.service.statistics.tendency.Accumulator;

/**
 * @author Conrado M.
 */
public class Aggregator
{
	private final DoubleAdder sum = new DoubleAdder();
	private final LongAdder count = new LongAdder();
	private final DoubleAdder max = new DoubleAdder();
	private final DoubleAdder min = new DoubleAdder();

	private final Solver[] solvers;
	private final ExecutorService executor;
	private final LongAdder counter;
	private final Statistics[] cache;

	static final Logger logger = LogManager.getLogger(Aggregator.class.getName());

	public Aggregator(final Duration duration)
	{
		this.solvers = new Solver[] { new Accumulator(duration, sum, count), new Maximizer(duration, max), new Minimizer(duration, min) };
		this.executor = Executors.newFixedThreadPool(this.solvers.length);
		this.counter = new LongAdder();
		this.cache = new Statistics[] { new Statistics(), new Statistics() };
	}

	public Duration accept(final Event event)
	{
		logger.debug("Receiving event {}", event);

		final Stream<Solver> stream = Arrays.stream(this.solvers);
		try
		{
			CompletableFuture.allOf(stream
				.map(task -> CompletableFuture.runAsync(() -> task.feed(event), executor))
				.toArray(CompletableFuture[]::new)).join();
		}
		finally
		{
			stream.close();
		}
		
		Statistics transactions = new Statistics(sum.sum(), count.sum(), max.doubleValue(), min.doubleValue(), sum.doubleValue() / count.sum());
		logger.debug("Current Stats are {}", transactions);

		this.cache[(counter.byteValue() + 1) & 1] = transactions;
		counter.increment();
		return Duration.ZERO;
	}

	public Statistics get()
	{
		return this.cache[counter.byteValue() & 1];
	}
}

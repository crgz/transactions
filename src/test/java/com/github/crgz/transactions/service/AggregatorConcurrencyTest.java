/*******************************************************************************
 * Copyright (C) 2017, Conrado M. Rodriguez
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * 3. Neither the name of the copyright holder nor the names of its contributors
 *    may be used to endorse or promote products derived from this software
 *    without specific prior written permission.
 * 
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
package com.github.crgz.transactions.service;

import static org.junit.Assert.assertEquals;

import java.text.DecimalFormat;
import java.time.Duration;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

import com.github.crgz.transactions.model.Event;
import com.github.crgz.transactions.model.Statistics;
import com.github.crgz.transactions.service.statistics.Aggregator;

/**
 * @author Conrado M.
 */
public class AggregatorConcurrencyTest
{
	private static final int LOAD = 1000;

	private static final CyclicBarrier barrier = new CyclicBarrier(LOAD + 1);
	private static final ExecutorService pool = Executors.newCachedThreadPool();

	private static Logger logger = LogManager.getLogger(AggregatorConcurrencyTest.class);

	@Test
	public void test()
	{
		logger.info("Test concurrency with {} threads.", LOAD);

		Aggregator solver = new Aggregator(Duration.ofMillis(1000));

		for (int key = 0; key < LOAD; ++key)
		{
			pool.execute(new Producer(solver, new Event(key, key)));
		}
		long result = 0;
		try
		{
			barrier.await(); // wait for all threads to be ready
			logger.info("ConcurrencyTest: {} Starting", Thread.currentThread().getName());
			long start = System.nanoTime();
			barrier.await(); // wait for all threads to finish
			result = System.nanoTime() - start;
		}
		catch (InterruptedException | BrokenBarrierException e)
		{
			logger.error("Please check your test!");
		}
		final String format = new DecimalFormat("#,###.00").format(result);
		logger.info("ConcurrencyTest: {} Finished in {} nanoseconds", Thread.currentThread().getName(), format);

		final int max = LOAD - 1;

		final int sum = (max * max + max) / 2;
		final int count = LOAD;
		float avg = (float) sum / count;

		assertEquals(new Statistics(sum, count, max, 0, avg), solver.get());

		pool.shutdown();
	}

	class Producer implements Runnable
	{
		private Aggregator solver;
		private Event event;

		public Producer(Aggregator solver, Event event)
		{
			this.solver = solver;
			this.event = event;
		}

		public void run()
		{
			try
			{
				barrier.await();
				solver.accept(event);
				barrier.await();
			}
			catch (Exception e)
			{
				logger.error("Please check your test!");
			}
		}
	}
}

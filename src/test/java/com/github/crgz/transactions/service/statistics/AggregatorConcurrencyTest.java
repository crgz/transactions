package com.github.crgz.transactions.service.statistics;

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
	private static final Logger logger = LogManager.getLogger(AggregatorConcurrencyTest.class);

	private static final int LOAD = 1000;
	private static final CyclicBarrier barrier = new CyclicBarrier(LOAD + 1);
	private static final ExecutorService pool = Executors.newCachedThreadPool();

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

	static class Producer implements Runnable
	{
		private final Aggregator solver;
		private final Event event;

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

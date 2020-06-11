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
	private static final Logger logger = LogManager.getLogger(Aggregator.class.getName());

	private final DoubleAdder sum = new DoubleAdder();
	private final LongAdder count = new LongAdder();
	private final DoubleAdder max = new DoubleAdder();
	private final DoubleAdder min = new DoubleAdder();

	private final Solver[] solvers;
	private final ExecutorService executor;
	private final LongAdder counter;
	private final Statistics[] cache;

	public Aggregator(final Duration duration)
	{
		this.solvers = new Solver[] { new Accumulator(duration, sum, count), new Maximizer(duration, max), new Minimizer(duration, min) };
		this.executor = Executors.newFixedThreadPool(this.solvers.length);
		this.counter = new LongAdder();
		this.cache = new Statistics[] { new Statistics(), new Statistics() };
	}

	public void accept(final Event event)
	{
		logger.debug("Receiving event {}", event);

		try (Stream<Solver> stream = Arrays.stream(this.solvers)) {
			CompletableFuture.allOf(stream
					.map(task -> CompletableFuture.runAsync(() -> task.feed(event), executor))
					.toArray(CompletableFuture[]::new)).join();
		}
		
		Statistics transactions = new Statistics(sum.sum(), count.sum(), max.doubleValue(), min.doubleValue(), sum.doubleValue() / count.sum());
		logger.debug("Current Stats are {}", transactions);

		this.cache[(counter.byteValue() + 1) & 1] = transactions;
		counter.increment();
	}

	public Statistics get()
	{
		return this.cache[counter.byteValue() & 1];
	}
}

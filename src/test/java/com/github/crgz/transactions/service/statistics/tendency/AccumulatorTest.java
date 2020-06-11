package com.github.crgz.transactions.service.statistics.tendency;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.time.Duration;
import java.util.Collection;
import java.util.concurrent.atomic.DoubleAdder;
import java.util.concurrent.atomic.LongAdder;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

import com.github.crgz.transactions.model.Event;
import com.github.crgz.transactions.service.statistics.tendency.Accumulator;
import com.github.crgz.utils.EventStatisticsMapper;
import com.github.crgz.utils.WorkbookData;
import com.github.crgz.utils.WorkbookDataException;

/**
 * @author Conrado M.
 */
public class AccumulatorTest
{
	private Collection<Object[]> data;

	private static final Logger logger = LogManager.getLogger(AccumulatorTest.class.getName());
	private static final double DELTA = 1e-15;

	private final LongAdder windowCounter = new LongAdder();
	private final DoubleAdder windowAdder = new DoubleAdder();
	private Accumulator accumulator;

	@Before
	public void setUp() throws IOException, WorkbookDataException
	{
		this.data = new WorkbookData.Builder("src/test/resources/sample.xls").sheet(0).build().collection();

		this.accumulator = new Accumulator(Duration.ofMillis(3000), windowAdder, windowCounter);
	}

	@Test
	public void testSum() {
		logger.info("Test sum");

		this.data.stream().map(EventStatisticsMapper::map).forEach(x ->
		{
			final double expected = x.getValue().getSum();
			final Event event = x.getKey();

			this.accumulator.feed(event);

			final double actual = this.windowAdder.sum();

			logger.info("event -> {} expected -> {} actual -> {}", event, expected, actual);

			assertEquals(expected, actual, DELTA);
		});
	}

	@Test
	public void testCount() {
		logger.info("Test count");

		this.data.stream().map(EventStatisticsMapper::map).forEach(x ->
		{
			final long expected = x.getValue().getCount();
			final Event event = x.getKey();

			this.accumulator.feed(event);

			final long actual = this.windowCounter.sum();

			logger.info("event -> {} expected -> {} actual -> {}", event, expected, actual);

			assertEquals(expected, actual);
		});
	}
}

package com.github.crgz.transactions.service.statistics.dispersion;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.time.Duration;
import java.util.Collection;
import java.util.concurrent.atomic.DoubleAdder;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

import com.github.crgz.transactions.model.Event;
import com.github.crgz.utils.EventStatisticsMapper;
import com.github.crgz.utils.WorkbookData;
import com.github.crgz.utils.WorkbookDataException;

/**
 * @author Conrado M.
 */
public class MaximizerTest
{
	private Collection<Object[]> data;

	private static final Logger logger = LogManager.getLogger(MaximizerTest.class.getName());
	private static final double DELTA = 1e-15;

	private final DoubleAdder maximizer = new DoubleAdder();
	private Maximizer resource;

	@Before
	public void setUp() throws IOException, WorkbookDataException
	{
		this.data = new WorkbookData.Builder("src/test/resources/sample.xls").sheet(0).build().collection();

		this.resource = new Maximizer(Duration.ofMillis(3000), maximizer);
	}

	@Test
	public void testMax() {
		logger.info("Test sum");

		this.data.stream().map(EventStatisticsMapper::map).forEach(x ->
		{
			final double expected = x.getValue().getMax();
			final Event event = x.getKey();

			this.resource.feed(event);

			final double actual = this.maximizer.sum();

			logger.info("event -> {} expected -> {} actual -> {}", event, expected, actual);

			assertEquals(expected, actual, DELTA);
		});
	}

}

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
public class MinimizerTest
{
	private Collection<Object[]> data;

	private static final Logger logger = LogManager.getLogger(MinimizerTest.class.getName());
	private static final double DELTA = 1e-15;

	private final DoubleAdder minimizer = new DoubleAdder();
	private Minimizer resource;

	@Before
	public void setUp() throws IOException, WorkbookDataException
	{
		this.data = new WorkbookData.Builder("src/test/resources/sample.xls").sheet(0).build().collection();

		this.resource = new Minimizer(Duration.ofMillis(3000), minimizer);
	}

	@Test
	public void testMin() {
		logger.info("Test sum");

		this.data.stream().map(EventStatisticsMapper::map).forEach(x ->
		{
			final double expected = x.getValue().getMin();
			final Event event = x.getKey();

			this.resource.feed(event);

			final double actual = this.minimizer.sum();

			logger.info("event -> {} expected -> {} actual -> {}", event, expected, actual);

			assertEquals(expected, actual, DELTA);
		});
	}

}

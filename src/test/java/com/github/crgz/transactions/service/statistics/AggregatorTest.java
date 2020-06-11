package com.github.crgz.transactions.service.statistics;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.time.Duration;
import java.util.Collection;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

import com.github.crgz.transactions.model.Event;
import com.github.crgz.transactions.model.Statistics;
import com.github.crgz.transactions.service.statistics.Aggregator;
import com.github.crgz.utils.EventStatisticsMapper;
import com.github.crgz.utils.WorkbookData;
import com.github.crgz.utils.WorkbookDataException;

/**
 * Using a parameterized test with an Excel spreadsheet.
 * 
 * @author Conrado M.
 */
public class AggregatorTest
{
	private static final Logger logger = LogManager.getLogger(AggregatorTest.class.getName());

	private Aggregator aggregator;
	private Collection<Object[]> data;

	@Before
	public void setUp() throws IOException, WorkbookDataException
	{
		this.data = new WorkbookData.Builder("src/test/resources/sample.xls").sheet(0).build().collection();
		this.aggregator = new Aggregator(Duration.ofMillis(3000));
	}

	@Test
	public void testAggregator() {
		logger.info("Test data collection");

		this.data.stream().map(EventStatisticsMapper::map).forEach(x ->
		{
			final Statistics expected = x.getValue();
			final Event event = x.getKey();

			logger.info("event -> {} expected -> {}", event, expected);

			this.aggregator.accept(event);

			final Statistics actual = this.aggregator.get();

			assertEquals(expected, actual);
		});
	}
}

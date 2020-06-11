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
	private static final Logger logger = LogManager.getLogger(SchedulerTest.class.getName());
	
	private final Set<Integer> result = new HashSet<>();

	@Test
	public void testTimeDiscrepancyController() {
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
		return () -> {
			return result.size() == 10; // The condition that must be fulfilled
		};
	}
}

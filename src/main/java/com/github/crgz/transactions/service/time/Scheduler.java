package com.github.crgz.transactions.service.time;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.github.crgz.transactions.model.Event;

/**
 * The TimeWarpController handle time discrepancies in incoming events by calculation the difference between the event timestamp and the present.
 * This difference define a dynamic quarantine period. The events are stored in a priority queue and kept for the quarantine time. This time is
 * considered enough to catch up with other rambling events.
 * 
 * @author Conrado M.
 */
public class Scheduler
{
	private static final Logger logger = LogManager.getLogger(Scheduler.class.getName());

	private final DelayQueue<Event> queue = new DelayQueue<>();
	private final Consumer<Event> consumer;
	private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

	private Instant evacuationTime = Instant.now();

	public Scheduler(Consumer<Event> consumer)
	{
		this.consumer = consumer;
	}

	public synchronized Duration accept(Event event)
	{
		final Instant now = Instant.now();
		final Duration eventDelay = Duration.between(event.getInstant(), now);
		final Duration quarantine = Duration.between(now, this.evacuationTime);

		if (eventDelay.compareTo(quarantine) > 0)
		{
			this.evacuationTime = now.plus(eventDelay);
			logger.warn("Next evacuation time is: {}. Event {} must evacuate in {} ms.", this.evacuationTime, event.getAmount(),
				eventDelay.toMillis());
		}

		queue.put(event);

		this.executor.schedule(() ->
		{
			Event acceptable = null;
			try
			{
				acceptable = this.queue.take();
			}
			catch (InterruptedException e)
			{
				logger.error("tasks interrupted");
				Thread.currentThread().interrupt();
			}
			this.consumer.accept(acceptable);
			final Event nextEvent = this.queue.peek();
			assert nextEvent != null;
			this.evacuationTime = nextEvent.getInstant();
			logger.info("Consumed event {}. Next evacuation time is at least {}", acceptable, this.evacuationTime);

		}, quarantine.toMillis(), TimeUnit.MILLISECONDS);

		return eventDelay;
	}

	public void shutdown()
	{
		try
		{
			logger.info("attempt to shutdown executor");
			executor.shutdown();
			executor.awaitTermination(5, TimeUnit.SECONDS);
		}
		catch (InterruptedException e)
		{
			logger.info("tasks interrupted", e);
			Thread.currentThread().interrupt();
		}
		finally
		{
			if (!executor.isTerminated())
			{
				logger.error("Unablew to shutdown. Please cancel non-finished tasks");
			}
			executor.shutdownNow();
			logger.error("shutdown finished");
		}
	}
}

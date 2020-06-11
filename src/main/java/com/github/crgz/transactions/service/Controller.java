package com.github.crgz.transactions.service;

import java.time.Duration;

import com.github.crgz.transactions.model.Event;
import com.github.crgz.transactions.model.Statistics;
import com.github.crgz.transactions.service.time.Scheduler;
import com.github.crgz.transactions.service.statistics.Aggregator;

/**
 * @author Conrado M.
 */
public class Controller
{
	private final Aggregator aggregator;
	private final Scheduler scheduler;

	public Controller(Duration timeWindow)
	{
		this.aggregator = new Aggregator(timeWindow);
		this.scheduler = new Scheduler(this.aggregator::accept);
	}

	public synchronized Duration accept(Event event)
	{
		return this.scheduler.accept(event);
	}

	public synchronized Statistics get()
	{
		return this.aggregator.get();
	}
}

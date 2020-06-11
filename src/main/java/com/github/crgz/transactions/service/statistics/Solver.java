package com.github.crgz.transactions.service.statistics;

import java.time.Duration;
import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;

import com.github.crgz.transactions.model.Event;

/**
 * @author Conrado M.
 */
public abstract class Solver
{
	private final Deque<Event> queue = new ConcurrentLinkedDeque<>();
	private final Duration window;

	public Solver(final Duration duration)
	{
		this.window = duration;
	}
	
	public abstract void feed(Event event);

	protected Deque<Event> getQueue()
	{
		return this.queue;
	}

	protected Duration getWindow()
	{
		return this.window;
	}
	
}

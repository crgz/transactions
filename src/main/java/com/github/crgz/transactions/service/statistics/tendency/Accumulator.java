package com.github.crgz.transactions.service.statistics.tendency;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.concurrent.atomic.DoubleAdder;
import java.util.concurrent.atomic.LongAdder;
import java.util.concurrent.locks.StampedLock;

import com.github.crgz.transactions.model.Event;
import com.github.crgz.transactions.service.statistics.Solver;

/**
 * To compute the mean of all the values in your window, separately compute (a) the sum of all the values in the window, and (b) a count of the number
 * of values in the window. To keep track of the sum of all the values in the sliding window, each time you receive a new value, add it to the sum.
 * Each time a value leaves the window, subtract it from the sum. Now you'll always have a sum of the values in the window.
 * To keep track of the number of values in the window, each time you receive a new value, increment the counter. Each time a value leaves the window,
 * decrement the counter. Finally, the mean is the sum divided by the count.
 * 
 * @author Conrado M.
 */
public final class Accumulator extends Solver
{
	private final DoubleAdder windowAdder;
	private final LongAdder windowCounter;
	private final StampedLock lock = new StampedLock();

	public Accumulator(final Duration duration, DoubleAdder windowAdder, LongAdder windowCounter)
	{
		super(duration);
		this.windowAdder = windowAdder;
		this.windowCounter = windowCounter;
	}

	@Override
	public void feed(final Event event)
	{
		long stamp = lock.writeLock();
		try
		{
			super.getQueue().addLast(event);
			windowCounter.increment();
			windowAdder.add(event.getAmount());
			
			while (!super.getQueue().isEmpty())
			{
				if (!(gap(Objects.requireNonNull(this.getQueue().peek())).compareTo(getWindow()) >= 0)) break;
				windowCounter.decrement();
				windowAdder.add(-this.getQueue().pop().getAmount());
			}
		}
		finally
		{
			lock.unlockWrite(stamp);
		}
	}

	private Duration gap(final Event oldest)
	{
		final Instant newest = super.getQueue().getLast().getInstant();
		return Duration.between(oldest.getInstant(), newest);
	}
}

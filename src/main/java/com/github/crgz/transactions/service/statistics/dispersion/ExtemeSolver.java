package com.github.crgz.transactions.service.statistics.dispersion;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.concurrent.atomic.DoubleAdder;
import java.util.concurrent.locks.StampedLock;
import com.github.crgz.transactions.model.Event;
import com.github.crgz.transactions.service.statistics.Solver;

/**
 * @author Conrado M.
 */
public abstract class ExtemeSolver extends Solver
{
	private final DoubleAdder reference;
	private final StampedLock lock = new StampedLock();

	public ExtemeSolver(final Duration duration, DoubleAdder reference)
	{
		super(duration);
		this.reference = reference;
	}

	@Override
	public void feed(final Event event)
	{
		long stamp = lock.writeLock();
		try
		{
			while (!super.getQueue().isEmpty())
			{
				if (!criteria(event.getAmount() - Objects.requireNonNull(super.getQueue().peekLast()).getAmount())) break;
				super.getQueue().removeLast();
			}
			
			super.getQueue().addLast(event);

			while (!super.getQueue().isEmpty() && duration(event).compareTo(getWindow()) >= 0)
			{
				super.getQueue().removeFirst();
			}
			
			if (!super.getQueue().isEmpty())
			{
				this.reference.add(Objects.requireNonNull(super.getQueue().peek()).getAmount() - this.reference.doubleValue());
			}
		}
		finally
		{
			lock.unlockWrite(stamp);
		}
	}

	private Duration duration(final Event event)
	{
		final Instant newest = event.getInstant();
		final Instant oldest = Objects.requireNonNull(super.getQueue().peek()).getInstant();
		return Duration.between(oldest, newest);
	}

	protected abstract boolean criteria(double b);
}

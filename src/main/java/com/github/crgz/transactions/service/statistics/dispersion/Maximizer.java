package com.github.crgz.transactions.service.statistics.dispersion;

import java.time.Duration;
import java.util.concurrent.atomic.DoubleAdder;

/**
 * @author Conrado M.
 */
public final class Maximizer extends ExtemeSolver
{
	public Maximizer(final Duration duration, DoubleAdder maximizer)
	{
		super(duration, maximizer);
	}

	@Override
	protected boolean criteria(final double b)
	{
		return b >= 0;
	}
}

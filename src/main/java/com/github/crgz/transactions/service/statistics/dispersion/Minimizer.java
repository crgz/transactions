package com.github.crgz.transactions.service.statistics.dispersion;

import java.time.Duration;
import java.util.concurrent.atomic.DoubleAdder;

/**
 * @author Conrado M.
 */
public final class Minimizer extends ExtemeSolver
{
	public Minimizer(final Duration duration, DoubleAdder minimizer)
	{
		super(duration, minimizer);
	}

	@Override
	protected boolean criteria(final double b)
	{
		return b <= 0;
	}
}

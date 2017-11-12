/*******************************************************************************
 * Copyright (C) 2017, Conrado M. Rodriguez
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * 3. Neither the name of the copyright holder nor the names of its contributors
 *    may be used to endorse or promote products derived from this software
 *    without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 ******************************************************************************/
package com.github.crgz.transactions.service.statistics.dispersion;

import java.time.Duration;
import java.time.Instant;
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
			while (!super.getQueue().isEmpty() && criteria(event.getAmount() - super.getQueue().peekLast().getAmount()))
			{
				super.getQueue().removeLast();
			}
			
			super.getQueue().addLast(event);

			while (!super.getQueue().isEmpty() && duration(event).compareTo(getWindow()) >= 0)
			{
				super.getQueue().removeFirst();
			}
			
			if (!super.getQueue().isEmpty())
			{
				this.reference.add(super.getQueue().peek().getAmount() - this.reference.doubleValue());
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
		final Instant oldest = super.getQueue().peek().getInstant();
		return Duration.between(oldest, newest);
	}

	protected abstract boolean criteria(double b);
}

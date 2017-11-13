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
package com.github.crgz.transactions.service.statistics.tendency;

import java.time.Duration;
import java.time.Instant;
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
			
			while (!super.getQueue().isEmpty() && gap(this.getQueue().peek()).compareTo(getWindow()) >= 0)
			{
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

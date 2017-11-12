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
package com.github.crgz.utils;

import java.util.AbstractMap.SimpleEntry;

import com.github.crgz.transactions.model.Event;
import com.github.crgz.transactions.model.Statistics;

/**
 * @author Conrado M.
 */
public class EventStatisticsMapper
{
	public static SimpleEntry<Event, Statistics> map(Object[] x)
	{
		final long time = ((Double) x[0]).longValue();
		final double value = ((Double) x[1]).doubleValue();
		final double sum = ((Double) x[3]).doubleValue();
		final long count = ((Double) x[4]).longValue();
		double avg = (double) sum / count;
		final double max = ((Double) x[5]).doubleValue();
		final double min = ((Double) x[6]).doubleValue();
		return new SimpleEntry<Event, Statistics>(new Event(time * 1000, value), new Statistics(sum, count, max, min, avg));
	}
}

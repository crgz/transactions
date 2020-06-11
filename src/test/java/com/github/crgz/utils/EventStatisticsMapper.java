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
		final double value = (Double) x[1];
		final double sum = (Double) x[3];
		final long count = ((Double) x[4]).longValue();
		double avg = sum / count;
		final double max = (Double) x[5];
		final double min = (Double) x[6];
		return new SimpleEntry<>(new Event(time * 1000, value), new Statistics(sum, count, max, min, avg));
	}
}

package com.github.crgz.transactions.model;

import java.time.Instant;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

import io.vertx.core.json.Json;

public class Event implements Delayed
{
	private final Instant instant;
	private final double amount;

	public Event(Input raw)
	{
		this.instant = Instant.ofEpochSecond(raw.getTimestamp());		
		this.amount = raw.getAmount();
	}

	public Event(long timestamp, double amount)
	{
		this.instant = Instant.ofEpochMilli(timestamp);
		this.amount = amount;
	}

	public Event(Instant instant, double amount)
	{
		this.instant = instant;
		this.amount = amount;
	}

	public Event()
	{
		this.instant = Instant.MAX;
		this.amount = Integer.MAX_VALUE;
	}

	/**
	 * @return the instant
	 */
	public Instant getInstant()
	{
		return instant;
	}

	/**
	 * @return the count
	 */
	public double getAmount()
	{
		return amount;
	}

	@Override
	public long getDelay(TimeUnit unit)
	{
		long diff = this.instant.toEpochMilli() - System.currentTimeMillis();
		return unit.convert(diff, TimeUnit.MILLISECONDS);
	}

	@Override
	public int compareTo(Delayed o)
	{
		return Long.compare(this.getDelay(TimeUnit.MILLISECONDS), o.getDelay(TimeUnit.MILLISECONDS));
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(amount);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + ((instant == null) ? 0 : instant.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
		{
			return true;
		}
		if (obj == null)
		{
			return false;
		}
		if (getClass() != obj.getClass())
		{
			return false;
		}
		Event other = (Event) obj;
		if (Double.doubleToLongBits(amount) != Double.doubleToLongBits(other.amount))
		{
			return false;
		}
		if (instant == null)
		{
			return other.instant == null;
		}
		else return instant.equals(other.instant);
	}

	@Override
	public String toString()
	{
		return Json.encode(this);
	}
}

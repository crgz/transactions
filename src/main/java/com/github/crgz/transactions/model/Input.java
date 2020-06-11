package com.github.crgz.transactions.model;

import io.vertx.core.json.Json;

/**
 * @author Conrado M.
 */
public class Input
{
	private final long timestamp;
	private final double amount;

	public Input(long timestamp, int count)
	{
		this.timestamp = timestamp;
		this.amount = count;
	}

	/**
	 * Required for JSON Marshaling
	 */
	public Input()
	{
		this.timestamp = 0;
		this.amount = 0;
	}

	public long getTimestamp()
	{
		return timestamp;
	}

	public double getAmount()
	{
		return amount;
	}

	@Override
	public String toString()
	{
		return Json.encode(this);
	}
}

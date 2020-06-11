package com.github.crgz.transactions.model;

import io.vertx.core.json.Json;

/**
 * @author Conrado M.
 */
public class Statistics
{
	/**
	 * Total sum of transaction value
	 */
	private final double sum;

	/**
	 * Single highest transaction value
	 */
	private final double max;
	/**
	 * Single lowest transaction value
	 */
	private final double min;
	/**
	 * Total number of transactions happened
	 */
	private final long count;
	/**
	 * Average amount of transaction value
	 */
	private final double avg;

	/**
	 * This Data transfer object is used for JSON Marshaling 
	 */
	public Statistics(final double sum, final long count, final double max, final double min, final double avg)
	{
		this.sum = sum;
		this.avg = avg;
		this.max = max;
		this.min = min;
		this.count = count;
	}

	/**
	 *  Required for JSON Marshaling
	 */
	public Statistics()
	{
		this.sum = 0;
		this.avg = 0;
		this.max = 0;
		this.min = 0;
		this.count = 0;
	}

	/**
	 * @return Total sum of transactions
	 */
	public double getSum()
	{
		return sum;
	}

	/**
	 * @return Average amount of transactions per batch
	 */
	public double getAvg()
	{
		return avg;
	}

	/**
	 * @return Maximum of transactions per batch
	 */
	public double getMax()
	{
		return max;
	}

	/**
	 * @return Minimum amount of items per batch
	 */
	public double getMin()
	{
		return min;
	}

	/**
	 * @return the count
	 */
	public long getCount()
	{
		return count;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode()
	{
		int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(avg);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + (int) (count ^ (count >>> 32));
		temp = Double.doubleToLongBits(max);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(min);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(sum);
		result = prime * result + (int) (temp ^ (temp >>> 32));
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
		if (obj == null || getClass() != obj.getClass())
		{
			return false;
		}
		final Statistics other = (Statistics) obj;
		return !(Double.doubleToLongBits(avg) != Double.doubleToLongBits(other.avg) || count != other.count
				|| Double.doubleToLongBits(max) != Double.doubleToLongBits(other.max)
				|| Double.doubleToLongBits(min) != Double.doubleToLongBits(other.min)
				|| Double.doubleToLongBits(sum) != Double.doubleToLongBits(other.sum));
	}

	@Override
	public String toString()
	{
		return Json.encode(this);
	}
}

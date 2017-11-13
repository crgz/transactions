/*******************************************************************************
 * Copyright (C) 2017, Conrado M. Rodriguez
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * 3. Neither the name of the copyright holder nor the names of its contributors
 * may be used to endorse or promote products derived from this software
 * without specific prior written permission.
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

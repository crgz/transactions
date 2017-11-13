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
package com.github.crgz.transactions.service.statistics.dispersion;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.time.Duration;
import java.util.Collection;
import java.util.concurrent.atomic.DoubleAdder;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

import com.github.crgz.transactions.model.Event;
import com.github.crgz.utils.EventStatisticsMapper;
import com.github.crgz.utils.WorkbookData;
import com.github.crgz.utils.WorkbookDataException;

/**
 * @author Conrado M.
 */
public class MinimizerTest
{
	private Collection<Object[]> data;

	private static final Logger logger = LogManager.getLogger(MinimizerTest.class.getName());
	private static final double DELTA = 1e-15;

	private final DoubleAdder minimizer = new DoubleAdder();
	private Minimizer resource;

	@Before
	public void setUp() throws IOException, WorkbookDataException
	{
		this.data = new WorkbookData.Builder("src/test/resources/sample.xls").sheet(0).build().collection();

		this.resource = new Minimizer(Duration.ofMillis(3000), minimizer);
	}

	@Test
	public void testMin() throws IOException, WorkbookDataException
	{
		logger.info("Test sum");

		this.data.stream().map(EventStatisticsMapper::map).forEach(x ->
		{
			final double expected = x.getValue().getMin();
			final Event event = x.getKey();

			this.resource.feed(event);

			final double actual = this.minimizer.sum();

			logger.info("event -> {} expected -> {} actual -> {}", event, expected, actual);

			assertEquals(expected, actual, DELTA);
		});
	}

}

package de.jowo.pspac.jobs;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class HashcatJobTest {
	@Test
	public void toStringTest() {
		HashcatJob job = new HashcatJob("?d?d");

		assertEquals("HashcatJob [?d?d]", job.toString());
	}

	@Test
	public void toStringTestNull() {
		HashcatJob job = new HashcatJob(null);

		assertEquals("HashcatJob [null]", job.toString());
	}
}

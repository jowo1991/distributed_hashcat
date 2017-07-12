package de.jowo.pspac.jobs;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import de.jowo.pspac.LoggingProgressMonitor;

public class HashcatJobTest {

	/**
	 * password = test
	 */
	private final String MD5_SIMPLE = "098f6bcd4621d373cade4e832627b4f6";

	@BeforeClass
	public static void setup() {
		Logger.getLogger(HashcatParser.class).setLevel(Level.ALL);
	}

	@Test
	public void md5_test() throws Exception {
		String mask = "?l?l?l?l";
		String args = "-m 0 -a 3 {hash} {mask} -D 2";

		HashcatJob hashcatJob = new HashcatJob(MD5_SIMPLE, mask, args);
		Object result = hashcatJob.call(new LoggingProgressMonitor(1L));

		Assert.assertEquals(MD5_SIMPLE + ":test", result);
	}

}

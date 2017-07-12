package de.jowo.pspac.jobs;

import java.util.Arrays;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import de.jowo.pspac.LoggingProgressMonitor;

public class BatchHashcatJobTest {

	/**
	 * password = test123
	 */
	private final String OFFICE2010_HASH = "$office$*2010*100000*128*16*78cc471d2df7bb0b61380aa0d6372486*911393d3df3d753d62ea673a15c591d7*4cc1997d054804732e8355a32b9202cfc1256e081423ad13e1d4fe184bf35b91";
	/**
	 * password = test
	 */
	private final String OFFICE2010_SIMPLE_HASH = "$office$*2010*100000*128*16*17adfd305a20c867fa6bdd56f5b44439*0060ddc681576e0064231d2483c2b440*b1a0e3efe4cfb656a61887607d0dc22e36e01d621d7b29f78984524d35136e36";

	/**
	 * password = 1
	 */
	private final String OFFICE2010_VERYSIMPLE_HASH = "$office$*2010*100000*128*16*32da587e6f45cbc3b050346b45278dbb*2221911a5169547056eedbc66fe848ac*09316ed425efc2be967c469f7b2711144a7e9cc848d08032084a5255f6714157";
	/**
	 * password = 1
	 */
	private final String OFFICE2010_VERYSIMPLE_HASH_FILENAME = "VerySimple.xlsx:$office$*2010*100000*128*16*32da587e6f45cbc3b050346b45278dbb*2221911a5169547056eedbc66fe848ac*09316ed425efc2be967c469f7b2711144a7e9cc848d08032084a5255f6714157";

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
		Iterable<String> masks = Arrays.asList("?d", "?d?d", "?l", "?l?l?l?l");
		String args = "-m 0";

		BatchHashcatJob hashcatJob = new BatchHashcatJob(MD5_SIMPLE, masks, args);
		Object result = hashcatJob.call(new LoggingProgressMonitor(1L));

		Assert.assertEquals(MD5_SIMPLE + ":test", result);
	}

	@Test
	public void Office2010_test() throws Exception {
		Iterable<String> masks = Arrays.asList("?d", "?d?d", "?l", "?l?l?l?l?d?d?d");
		String args = "-m 9500";

		BatchHashcatJob hashcatJob = new BatchHashcatJob(OFFICE2010_HASH, masks, args);
		Object result = hashcatJob.call(new LoggingProgressMonitor(1L));

		Assert.assertEquals(OFFICE2010_HASH + ":test123", result);
	}

	@Test
	public void Office2010_testsimple() throws Exception {
		Iterable<String> masks = Arrays.asList("?d", "?d?d", "?l?l?l?l");
		String args = "-m 9500";

		BatchHashcatJob hashcatJob = new BatchHashcatJob(OFFICE2010_SIMPLE_HASH, masks, args);
		Object result = hashcatJob.call(new LoggingProgressMonitor(1L));

		Assert.assertEquals(OFFICE2010_SIMPLE_HASH + ":test", result);
	}

	@Test
	public void Office2010_testverysimple() throws Exception {
		Iterable<String> masks = Arrays.asList("?d");
		String args = "-m 9500";

		BatchHashcatJob hashcatJob = new BatchHashcatJob(OFFICE2010_VERYSIMPLE_HASH, masks, args);
		Object result = hashcatJob.call(new LoggingProgressMonitor(1L));

		Assert.assertEquals(OFFICE2010_VERYSIMPLE_HASH + ":1", result);
	}

	@Test
	public void Office2010_testverysimple_with_filename() throws Exception {
		Iterable<String> masks = Arrays.asList("?d");
		String args = "-m 9500 --username";

		BatchHashcatJob hashcatJob = new BatchHashcatJob(OFFICE2010_VERYSIMPLE_HASH_FILENAME, masks, args);
		Object result = hashcatJob.call(new LoggingProgressMonitor(1L));

		Assert.assertEquals(JobInterface.HASH_CRACKED_BUT_NOT_CAPTURED, result);
	}
}

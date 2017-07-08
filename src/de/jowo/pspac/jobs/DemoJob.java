package de.jowo.pspac.jobs;

import java.io.Serializable;
import java.util.Arrays;

import org.apache.log4j.Logger;

import de.jowo.pspac.remote.ProgressReporter;
import de.jowo.pspac.remote.dto.ProgressInfo;

public class DemoJob implements JobInterface {
	private static final long serialVersionUID = 4759550327769714923L;
	private final static Logger logger = Logger.getLogger(DemoJob.class);

	private final String mask;

	public DemoJob(String mask) {
		this.mask = mask;
	}

	@Override
	public Serializable call(ProgressReporter reporter) throws Exception {
		for (int i = 1; i <= 5; i++) {
			Thread.sleep(100);
			logger.info("Progress: i = " + i + " for job = DemoJob");
			reporter.reportProgress(ProgressInfo.active(i * 10, "i = " + i));
		}

		// There is no "final" result, so always return null.
		return null;
	}

	@Override
	public Iterable<String> getMasks() {
		return Arrays.asList(mask);
	}

	@Override
	public String toString() {
		return "DemoJob [mask=" + mask + "]";
	}
}

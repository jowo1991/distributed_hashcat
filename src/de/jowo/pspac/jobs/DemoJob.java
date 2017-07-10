package de.jowo.pspac.jobs;

import java.io.Serializable;
import java.util.Collection;

import org.apache.log4j.Logger;

import de.jowo.pspac.remote.ProgressReporter;
import de.jowo.pspac.remote.dto.ProgressInfo;

public class DemoJob implements JobInterface {
	private static final long serialVersionUID = 4759550327769714923L;
	private final static Logger logger = Logger.getLogger(DemoJob.class);

	private final Collection<String> masks;
	private final int delayMillis;

	public DemoJob(Collection<String> masks, int delayMillis) {
		this.masks = masks;
		this.delayMillis = delayMillis;
	}

	@Override
	public Serializable call(ProgressReporter reporter) throws Exception {
		int i = 1;
		for (String mask : masks) {
			Thread.sleep(delayMillis);
			logger.info("Progress: mask = " + mask + " for job = DemoJob");

			int progress = (int) ((double) i / masks.size() * 100);
			reporter.reportProgress(ProgressInfo.active(progress, "i = " + i));

			i++;
		}

		// There is no "final" result, so always return null.
		return null;
	}

	@Override
	public Iterable<String> getMasks() {
		return masks;
	}

	@Override
	public String toString() {
		return "DemoJob [masks=" + masks + "]";
	}
}

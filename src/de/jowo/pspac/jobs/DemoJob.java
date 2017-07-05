package de.jowo.pspac.jobs;

import org.apache.log4j.Logger;

import de.jowo.pspac.remote.ProgressMonitor;
import de.jowo.pspac.remote.dto.ProgressInfo;

public class DemoJob implements JobInterface {
	private static final long serialVersionUID = 4759550327769714923L;
	private final static Logger logger = Logger.getLogger(DemoJob.class);

	@Override
	public Object call(ProgressMonitor monitor) throws Exception {
		for (int i = 1; i <= 5; i++) {
			Thread.sleep(100);
			logger.info("Progress: i = " + i + " for job = DemoJob");
			monitor.reportProgress(ProgressInfo.active(i * 10, "i = " + i));
		}

		// There is no "final" result, so always return null.
		return null;
	}
}

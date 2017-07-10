package de.jowo.pspac.factories;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

import org.apache.log4j.Logger;

import de.jowo.pspac.AbstractFactory;
import de.jowo.pspac.LoggingProgressMonitor;
import de.jowo.pspac.jobs.DemoJob;
import de.jowo.pspac.jobs.JobInterface;
import de.jowo.pspac.remote.ProgressMonitor;
import de.jowo.pspac.remote.ProgressReporter;

public class DemoFactory extends AbstractFactory {
	private static final Logger logger = Logger.getLogger(DemoFactory.class);
	private LoggingProgressMonitor monitor = null;

	private final int delayMillis;
	private final int batchSize;

	public DemoFactory() {
		delayMillis = Integer.parseInt(System.getProperty("delaymillis", "100"));
		batchSize = Integer.parseInt(System.getProperty("batchsize", "10"));

		logger.info("delayMillis = '" + delayMillis + "'");
		logger.info("batchSize = '" + batchSize + "'");
	}

	@Override
	public JobInterface createJob(Queue<String> maskQueue) {
		List<String> masks = new ArrayList<>(batchSize);
		for (int i = 0; i < batchSize; i++) {
			if (maskQueue.isEmpty()) {
				break;
			}
			masks.add(maskQueue.poll());
		}

		return new DemoJob(masks, delayMillis);
	}

	private LoggingProgressMonitor getMonitorReporter(long workerId) {
		if (monitor == null) {
			monitor = new LoggingProgressMonitor(workerId);
		}
		return monitor;
	}

	@Override
	public ProgressMonitor createMonitor(long workerId) {
		return getMonitorReporter(workerId);
	}

	@Override
	public ProgressReporter createReporter(long workerId) {
		return getMonitorReporter(workerId);
	}

}

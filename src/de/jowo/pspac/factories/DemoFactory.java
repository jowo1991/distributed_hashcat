package de.jowo.pspac.factories;

import java.util.Queue;

import de.jowo.pspac.AbstractFactory;
import de.jowo.pspac.LoggingProgressMonitor;
import de.jowo.pspac.jobs.DemoJob;
import de.jowo.pspac.jobs.JobInterface;
import de.jowo.pspac.remote.ProgressMonitor;
import de.jowo.pspac.remote.ProgressReporter;

public class DemoFactory extends AbstractFactory {
	private LoggingProgressMonitor monitor = null;

	@Override
	public JobInterface createJob(Queue<String> maskQueue) {
		String mask = maskQueue.poll();
		return new DemoJob(mask);
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

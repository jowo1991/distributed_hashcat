package de.jowo.pspac;

import java.util.HashMap;
import java.util.Map;
import java.util.Queue;

import de.jowo.pspac.jobs.JobInterface;
import de.jowo.pspac.remote.ProgressMonitor;
import de.jowo.pspac.remote.ProgressReporter;

/**
 * A factory for creating jobs ({@link JobInterface}) and appropriate monitors ({@link ProgressMonitor}).
 */
public abstract class AbstractFactory {

	protected final Map<Long, LoggingProgressMonitor> monitors = new HashMap<>();

	protected LoggingProgressMonitor getMonitorReporter(long workerId) {
		if (!monitors.containsKey(workerId)) {
			monitors.put(workerId, new LoggingProgressMonitor(workerId));
		}

		return monitors.get(workerId);
	}

	/**
	 * Creates a new job.
	 *
	 * @param maskQueue the mask queue
	 * @return the job
	 */
	public abstract JobInterface createJob(Queue<String> maskQueue);

	/**
	 * Creates a new monitor
	 *
	 * @param workerId the worker id
	 * @return the monitor
	 */
	public abstract ProgressMonitor createMonitor(long workerId);

	/**
	 * Creates a new reporter
	 *
	 * @param workerId the worker id
	 * @return the progress reporter
	 */
	public abstract ProgressReporter createReporter(long workerId);
}

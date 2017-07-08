package de.jowo.pspac;

import java.util.Queue;

import de.jowo.pspac.jobs.JobInterface;
import de.jowo.pspac.remote.ProgressMonitor;
import de.jowo.pspac.remote.ProgressReporter;

/**
 * A factory for creating jobs ({@link JobInterface}) and appropriate monitors ({@link ProgressMonitor}).
 */
public abstract class AbstractFactory {

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

package de.jowo.pspac.remote;

import de.jowo.pspac.Master;
import de.jowo.pspac.remote.dto.ProgressInfo;
import de.jowo.pspac.remote.dto.ProgressStatus;

/**
 * The object that is used by the {@link Master} to monitor the worker execution. <br>
 * Usually the {@link ProgressMonitor} is combined with the {@link ProgressReporter}.
 */
public interface ProgressMonitor {

	/**
	 * Gets the worker id for which this monitor is responsible
	 *
	 * @return the worker id
	 */
	public long getWorkerId();

	/**
	 * Block until the job finished
	 *
	 * @return Last progress reported by the worker. <br>
	 *         The {@link ProgressInfo#getStatus()} is {@link ProgressStatus#FINISHED} unless an error occurred.
	 * @throws InterruptedException the interrupted exception
	 */
	public ProgressInfo waitForWorker() throws InterruptedException;

	/**
	 * Sets the thread that is used for monitoring
	 *
	 * @param thread the new thread
	 */
	public void setThread(Thread thread);

	/**
	 * Gets the thread that is used for monitoring
	 *
	 * @return the thread
	 */
	public Thread getThread();
}

package de.jowo.pspac.remote;

import java.rmi.Remote;
import java.rmi.RemoteException;

import de.jowo.pspac.Master;
import de.jowo.pspac.Worker;
import de.jowo.pspac.remote.dto.ProgressInfo;

/**
 * The object that is used by the {@link Worker} to report the progress of the job execution
 * ({@link WorkerInterface#submitJob(de.jowo.pspac.jobs.JobInterface, ProgressReporter)} to the {@link Master}.
 */
public interface ProgressReporter extends Remote {

	/**
	 * Reports the progress to the <b>master</b>.
	 *
	 * @param progressInfo the progress info
	 */
	void reportProgress(ProgressInfo progressInfo) throws RemoteException;

}
package de.jowo.pspac;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;

import de.jowo.pspac.exceptions.NodeBusyException;
import de.jowo.pspac.jobs.JobInterface;
import de.jowo.pspac.remote.JobControl;
import de.jowo.pspac.remote.ProgressMonitor;
import de.jowo.pspac.remote.WorkerInterface;
import de.jowo.pspac.remote.dto.ProgressInfo;

/**
 * The implementation of the Worker.
 */
public class Worker implements WorkerInterface {
	private final static Logger logger = Logger.getLogger(Worker.class);

	final ExecutorService pool = Executors.newFixedThreadPool(1);

	@Override
	public JobControl submitJob(JobInterface job, ProgressMonitor monitor) throws RemoteException, NodeBusyException {
		logger.trace("submitJob(" + job + ")");

		pool.submit(() -> {
			try {
				Serializable result = job.call(monitor);

				ProgressInfo finishedProgress = ProgressInfo.finished(result);
				try {
					monitor.reportProgress(finishedProgress);
				} catch (RemoteException ex) {
					logger.error("Failed to report finished execution to Master: " + finishedProgress, ex);
				}
			} catch (Exception err) {
				logger.error("Job execution failed", err);
				try {
					monitor.reportProgress(ProgressInfo.exception(err));
				} catch (RemoteException e) {
					logger.error("Failed to report Exception to Master", err);
				}
			}
		});

		return null;
	}

	@Override
	public void terminate() throws RemoteException {
		logger.info("Master called node to terminate. Terminating.");

		// Shutdown asynchronously to ensure RMI call succeeds.
		pool.submit(() -> {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// ignored
			}
			Runtime.getRuntime().exit(0);
		});
	}
}

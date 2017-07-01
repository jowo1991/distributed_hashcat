package de.jowo.pspac;

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
			for (int i = 1; i <= 10; i++) {
				try {
					Thread.sleep(50);
					logger.info("Progress: i = " + i + " for job = " + job);
					monitor.reportProgress(new ProgressInfo(i * 10, "i = " + i));
				} catch (InterruptedException e) {
					logger.error("Execution was interrupted", e);
				} catch (RemoteException e) {
					logger.error("Remote Exception", e);
				}
			}
		});

		return null;
	}

	@Override
	public void terminate() throws RemoteException {
		throw new IllegalStateException("Not implemented");
	}
}

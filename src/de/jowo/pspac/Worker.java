package de.jowo.pspac;

import java.io.Serializable;
import java.lang.management.ManagementFactory;
import java.rmi.RemoteException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

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

	private long workerId;

	final AtomicLong jobCounter = new AtomicLong();
	final AtomicLong totalJobDuration = new AtomicLong();

	@Override
	public JobControl submitJob(JobInterface job, ProgressMonitor monitor) throws RemoteException, NodeBusyException {
		logger.debug("submitJob(" + job + ")");

		jobCounter.incrementAndGet();

		pool.submit(() -> {
			try {
				long start = System.currentTimeMillis();
				Serializable result = job.call(monitor);
				long end = System.currentTimeMillis();
				long duration = end - start;

				totalJobDuration.addAndGet(duration);
				logger.info(String.format("Finished job '%s' in '%d ms'", job, duration));

				ProgressInfo finishedProgress = ProgressInfo.finished(result);
				try {
					monitor.reportProgress(finishedProgress);
				} catch (RemoteException ex) {
					logger.error("Failed to report finished execution to Master: " + finishedProgress);
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

		long uptime = ManagementFactory.getRuntimeMXBean().getUptime();
		logger.info("#id = " + workerId);
		logger.info("#Total worker runtime: " + Utils.convertMilliSecondToHHMMSSString(uptime));
		logger.info("#submitJob = " + jobCounter.get());
		logger.info("#totalJobDuration = " + Utils.convertMilliSecondToHHMMSSString(totalJobDuration.get()));

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

	public void setWorkerId(long workerId) {
		this.workerId = workerId;
	}
}

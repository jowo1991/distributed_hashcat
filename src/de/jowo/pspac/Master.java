package de.jowo.pspac;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

import de.jowo.pspac.exceptions.NodeBusyException;
import de.jowo.pspac.exceptions.RegistrationFailedException;
import de.jowo.pspac.jmx.MasterMXBean;
import de.jowo.pspac.jobs.HashcatJob;
import de.jowo.pspac.remote.MasterInterface;
import de.jowo.pspac.remote.WorkerInterface;

/**
 * The implementation of the Master.
 */
public class Master implements MasterInterface, MasterMXBean {
	private static final Logger logger = Logger.getLogger(Master.class);

	private final AtomicLong workerIdCounter = new AtomicLong(0);
	private final Map<Long, WorkerInterface> workers = new HashMap<>();

	private boolean acceptWorkers = true;

	private Queue<String> maskfileQueue;
	private List<LoggingProgressMonitor> monitors = Collections.synchronizedList(new ArrayList<>());

	private List<String> maskfileRows;

	private boolean jobRunning = false;
	private long jobStartTime;
	private long jobEndTime;
	/**
	 * If set to false, {@link #runJob()} will <b>not</b> be called when the first worker calls {@link #register(WorkerInterface)}. <br>
	 * Defaults to {@code false}.
	 */
	private boolean startExecutionManually = false;

	private String hashcatArguments;
	private String hash;

	public Master() throws IOException {
		readMaskfile();

		startExecutionManually = System.getProperty("startmanually") != null;
		hashcatArguments = System.getProperty("hashcatargs");
		hash = System.getProperty("hash");

		logger.info("startExecutionManually = " + startExecutionManually);
		logger.info("hashcatArguments = " + hashcatArguments);
		logger.info("hash = " + hash);

		if (hashcatArguments == null) {
			throw new IllegalStateException("hashcatargs is mandatory.");
		}

		if (hash == null) {
			throw new IllegalStateException("hash is mandatory.");
		}
	}

	private void readMaskfile() throws IOException {
		if (System.getProperty("maskfile") == null) {
			throw new IllegalArgumentException("Missing mandatory parameter 'maskfile'");
		}
		Path maskfile = Paths.get(System.getProperty("maskfile"));

		logger.debug("Reading maskfile: " + maskfile);

		maskfileRows = Files.readAllLines(maskfile);
		maskfileQueue = new LinkedBlockingQueue<>(maskfileRows);
	}

	@Override
	public synchronized long register(WorkerInterface worker) throws RemoteException, RegistrationFailedException {
		if (!acceptWorkers) {
			throw new RegistrationFailedException("No more workers allowed");
		}

		long workerId = workerIdCounter.incrementAndGet();
		workers.put(workerId, worker);

		logger.info(String.format("New worker '%d' registered with the master (%s)", workerId, worker));

		if (jobRunning) {
			startDispatcherThread(workerId, worker);
		} else if (!jobRunning && !startExecutionManually) {
			runJob();
		}

		return workerId;
	}

	@Override
	public synchronized void runJob() throws RemoteException {
		if (jobRunning) {
			throw new IllegalArgumentException("Job already running");
		}
		logger.trace("runJob()");
		jobRunning = true;
		jobStartTime = System.currentTimeMillis();

		for (Map.Entry<Long, WorkerInterface> entry : workers.entrySet()) {
			startDispatcherThread(entry.getKey(), entry.getValue());
		}

		logger.trace("Finished 'runJob()'. Currently active monitors: " + monitors.size());
	}

	private class MonitorThread extends Thread {

		final LoggingProgressMonitor monitor;
		final WorkerInterface worker;
		final long workerId;

		public MonitorThread(long workerId, LoggingProgressMonitor monitor, WorkerInterface worker) {
			super("Monitor-" + workerId);

			this.monitor = monitor;
			this.workerId = workerId;
			this.worker = worker;
		}

		@Override
		public void run() {
			String workMask;
			while (true) {
				synchronized (maskfileQueue) {
					if (maskfileQueue.isEmpty()) {
						break;
					} else {
						workMask = maskfileQueue.poll();
					}
				}

				try {
					HashcatJob job = new HashcatJob(hash, workMask, hashcatArguments);
					logger.info("Submitting job '" + job + "' with worker " + workerId);
					worker.submitJob(job, monitor);

					// Block until monitor signals that the job finished
					monitor.lock.lock();
					monitor.workerFinished.await();
					monitor.lock.unlock();

				} catch (InterruptedException | RemoteException | NodeBusyException e) {
					logger.error("Error while executing Job on worker '" + workerId + "'. Stopped execution on that worker", e);
					break;
				}
			}

			logger.info(String.format("Worker '%d' out of work. Terminating worker and monitoring Thread", workerId));

			try {
				worker.terminate();

				synchronized (workers) {
					workers.remove(monitor.getWorkerId());
				}
			} catch (RemoteException e) {
				logger.warn(String.format("Failed to terminate worker '%d'", workerId));
			}

			if (workers.size() == 0) {
				jobEndTime = System.currentTimeMillis();
				String duration = convertSecondToHHMMSSString((jobEndTime - jobStartTime) / 1000);
				logger.info(String.format("runJob() finished after '%s'. All workers terminated. Shutting down.", duration));

				Runtime.getRuntime().exit(0);
			}
		}
	}

	private void startDispatcherThread(final long workerId, final WorkerInterface worker) throws RemoteException {
		final LoggingProgressMonitor monitor = new LoggingProgressMonitor(workerId);
		UnicastRemoteObject.exportObject(monitor, 0);

		// Create Monitoring-Thread
		final Thread t = new MonitorThread(workerId, monitor, worker);

		monitor.setThread(t);
		monitors.add(monitor);

		t.start();
	}

	private String convertSecondToHHMMSSString(long seconds) {
		return LocalTime.MIN.plusSeconds(seconds).toString();
	}

	@Override
	public List<String> getMonitors() {
		return monitors.stream().map(item -> item.toString()).collect(Collectors.toList());
	}

	@Override
	public List<String> getMaskRows() {
		return maskfileRows;
	}

	@Override
	public String getProgress() {
		if (jobRunning) {
			int queueSize = maskfileQueue.size();
			int fileSize = maskfileRows.size();

			// Avoid div by zero
			if (fileSize == 0) {
				fileSize = -1;
			}
			return String.format("total: %d, processed: %d", fileSize, fileSize - queueSize);
		} else {
			return "-";
		}
	}

	@Override
	public double getProgressPercentage() {
		if (jobRunning) {
			int queueSize = maskfileQueue.size();
			int fileSize = maskfileRows.size();

			return (fileSize - queueSize) / (double) fileSize * 100;
		} else {
			return 0;
		}
	}

	@Override
	public List<String> getWorkers() {
		synchronized (workers) {
			return workers.entrySet().stream().map(e -> {
				return String.format("%d - %s", e.getKey(), e.getValue().toString());
			}).collect(Collectors.toList());
		}
	}

	@Override
	public int getWorkerCount() {
		synchronized (workers) {
			return workers.size();
		}
	}
}

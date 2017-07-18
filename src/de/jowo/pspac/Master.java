package de.jowo.pspac;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
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
import de.jowo.pspac.jobs.JobInterface;
import de.jowo.pspac.remote.MasterInterface;
import de.jowo.pspac.remote.ProgressMonitor;
import de.jowo.pspac.remote.ProgressReporter;
import de.jowo.pspac.remote.WorkerInterface;
import de.jowo.pspac.remote.dto.ProgressInfo;
import de.jowo.pspac.util.Utils;

/**
 * The implementation of the Master.
 */
public class Master implements MasterInterface, MasterMXBean {
	private static final Logger logger = Logger.getLogger(Master.class);

	private final AtomicLong workerIdCounter = new AtomicLong(0);
	private final Map<Long, WorkerInterface> workers = new HashMap<>();

	private boolean acceptWorkers = true;

	private Queue<String> maskfileQueue;
	private List<ProgressMonitor> monitors = Collections.synchronizedList(new ArrayList<>());

	private List<String> maskfileRows;

	private boolean jobRunning = false;
	private long jobStartTime;
	private long jobEndTime;
	/**
	 * If set to false, {@link #runJob()} will <b>not</b> be called when the first worker calls {@link #register(WorkerInterface)}. <br>
	 * Defaults to {@code false}.
	 */
	private boolean startExecutionManually = false;

	private Serializable finalResult;

	private final AbstractFactory factory;

	public Master() throws IOException, IllegalStateException {
		readMaskfile();

		String factoryClass = System.getProperty("factory");
		startExecutionManually = System.getProperty("startmanually") != null;

		logger.info("startExecutionManually = '" + startExecutionManually + "'");

		if (factoryClass == null || factoryClass.equals("")) {
			throw new IllegalArgumentException("'factory' is mandatory");
		} else {
			try {
				factoryClass = "de.jowo.pspac.factories." + factoryClass;
				factory = (AbstractFactory) Class.forName(factoryClass).newInstance();
				logger.info("Successfully created factory: '" + factory.getClass().getName() + "'");
			} catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
				String error = String.format("Unable to instantiate factory '%s'", factoryClass);
				logger.fatal(error, e);

				throw new IllegalStateException(error, e);
			}
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

		logger.info("Using maskfile with " + maskfileRows.size() + " rows");
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
	public synchronized void runJob() throws RemoteException, IllegalArgumentException {
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

	private void startDispatcherThread(final long workerId, final WorkerInterface worker) throws RemoteException {
		final ProgressMonitor monitor = factory.createMonitor(workerId);
		final ProgressReporter reporter = factory.createReporter(workerId);

		UnicastRemoteObject.exportObject(reporter, 0);

		// Create Monitoring-Thread
		final Thread t = new MonitorThread(workerId, monitor, reporter, worker);

		monitor.setThread(t);
		monitors.add(monitor);

		t.start();
	}

	private void interruptOtherWorkers() {
		synchronized (monitors) {
			for (ProgressMonitor monitor : monitors) {
				if (monitor.getThread() != Thread.currentThread()) {
					monitor.getThread().interrupt();
				}
			}
		}
	}

	private class MonitorThread extends Thread {
		final ProgressMonitor monitor;
		final ProgressReporter reporter;
		final WorkerInterface worker;
		final long workerId;

		public MonitorThread(long workerId, ProgressMonitor monitor, ProgressReporter reporter, WorkerInterface worker) {
			super("Monitor-" + workerId);

			this.monitor = monitor;
			this.reporter = reporter;
			this.workerId = workerId;
			this.worker = worker;
		}

		private Serializable getJobResult(ProgressInfo info) throws IllegalStateException {
			switch (info.getStatus()) {
				case ERROR:
					throw new IllegalStateException(info.getMessage().toString());
				case EXCEPTION:
					throw new IllegalStateException((Exception) info.getMessage());
				case FINISHED:
					return info.getMessage();
				case ACTIVE:
				default:
					throw new IllegalStateException("Unexpected status of jobResult");
			}
		}

		@Override
		public void run() {
			int workMaskIndex;
			boolean hasError = false;
			JobInterface job;

			while (true) {
				synchronized (maskfileQueue) {
					if (maskfileQueue.isEmpty()) {
						break;
					} else {
						workMaskIndex = maskfileRows.size() - maskfileQueue.size() + 1;
						job = factory.createJob(maskfileQueue);
					}
				}

				try {
					logger.info("Submitting job '" + job + "' with worker " + workerId);
					worker.submitJob(job, reporter);

					ProgressInfo lastProgress = monitor.waitForWorker();
					Serializable jobResult = getJobResult(lastProgress);

					String maskProgress = String.format("(%d / %d)", workMaskIndex, maskfileRows.size());
					// We found the final result!
					if (jobResult != null) {
						logger.info(String.format("[%s] Finished job '%s' with match on '%d': %s", maskProgress, job, workerId, jobResult));
						finalResult = jobResult;

						interruptOtherWorkers();
						break;
					} else {
						logger.info(String.format("[%s] Finished job '%s' without match on '%d'", maskProgress, job, workerId));
					}

				} catch (RemoteException | NodeBusyException | IllegalStateException e) {
					hasError = true;
					logger.error(String.format("Error while processing job '%s' on worker '%d'. Stopped execution on that worker", job, workerId), e);
					// re-submit mask into queue so another worker can process it
					synchronized (maskfileQueue) {
						for (String mask : job.getMasks()) {
							maskfileQueue.add(mask);
						}
					}
					break;
				} catch (InterruptedException e) {
					// Only reason for interrupt is that another worker found the final result
					// -> silently terminate
					logger.debug(String.format("Thread '%s' interrupted. Probably final result (=%s) found.", Thread.currentThread().getName(), finalResult));
					break;
				}
			}

			if (hasError) {
				logger.info(String.format("Stopping worker '%d' and monitoring Thread because of an error", workerId));
			} else {
				logger.info(String.format("Worker '%d' out of work. Terminating worker and monitoring Thread", workerId));
			}

			try {
				worker.terminate();

				synchronized (workers) {
					workers.remove(monitor.getWorkerId());
				}
			} catch (RemoteException e) {
				logger.warn(String.format("Failed to terminate worker '%d'", workerId));
			}

			synchronized (workers) {
				if (workers.size() == 0) {
					jobEndTime = System.currentTimeMillis();
					String duration = Utils.convertMilliSecondToHHMMSSString(jobEndTime - jobStartTime);
					logger.info(String.format("runJob() finished after '%s'. All workers terminated. Shutting down.", duration));
					logger.info(String.format("jobResult = '%s'", finalResult));
					logger.info(String.format("remaining masks = '%d'", maskfileQueue.size()));

					Runtime.getRuntime().exit(0);
				}
			}
		}
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

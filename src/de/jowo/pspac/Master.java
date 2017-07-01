package de.jowo.pspac;

import java.io.IOException;
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

import org.apache.log4j.Logger;

import de.jowo.pspac.exceptions.NodeBusyException;
import de.jowo.pspac.exceptions.RegistrationFailedException;
import de.jowo.pspac.jobs.HashcatJob;
import de.jowo.pspac.jobs.JobInterface;
import de.jowo.pspac.remote.MasterInterface;
import de.jowo.pspac.remote.WorkerInterface;

public class Master implements MasterInterface {
	private static final Logger logger = Logger.getLogger(Master.class);

	private final AtomicLong workerIdCounter = new AtomicLong(0);
	private final Map<Long, WorkerInterface> workers = new HashMap<>();

	private boolean acceptWorkers = true;

	private Queue<String> maskfileRows;
	private List<LoggingProgressMonitor> monitors = Collections.synchronizedList(new ArrayList<>());

	@Override
	public synchronized long register(WorkerInterface worker) throws RemoteException, RegistrationFailedException {
		if (!acceptWorkers) {
			throw new RegistrationFailedException("No more workers allowed");
		}

		long workerId = workerIdCounter.incrementAndGet();
		workers.put(workerId, worker);

		logger.info(String.format("New worker '%d' registered with the master (%s)", workerId, worker));

		return workerId;
	}

	private void readMaskfile() throws IOException {
		Path maskfile = Paths.get(System.getProperty("maskfile"));

		logger.debug("Reading maskfile: " + maskfile);
		maskfileRows = new LinkedBlockingQueue<>(Files.readAllLines(maskfile));
	}

	public synchronized void runJob() throws IOException, RemoteException {
		logger.trace("runJob()");

		readMaskfile();

		for (Map.Entry<Long, WorkerInterface> entry : workers.entrySet()) {
			startDispatcherThread(entry.getKey(), entry.getValue());
		}

		logger.trace("Finished 'runJob()'. Currently active monitors: " + monitors.size());
	}

	private void startDispatcherThread(final long workerId, final WorkerInterface worker) throws RemoteException {
		final LoggingProgressMonitor monitor = new LoggingProgressMonitor(workerId);
		UnicastRemoteObject.exportObject(monitor, 0);

		final Thread t = new Thread(new Runnable() {

			@Override
			public void run() {

				String workRow;
				while (true) {
					synchronized (maskfileRows) {
						if (maskfileRows.isEmpty()) {
							break;
						} else {
							workRow = maskfileRows.poll();
						}
					}

					try {
						JobInterface job = new HashcatJob(workRow);
						logger.debug("Submitting job '" + job + "' with worker " + workerId);
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

				logger.info(String.format("Worker '%d' out of work. Terminating monitoring thread.", workerId));
			}
		});

		monitor.setThread(t);
		monitors.add(monitor);

		t.setName("Monitor-" + workerId);
		t.start();
	}
}

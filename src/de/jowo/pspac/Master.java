package de.jowo.pspac;

import java.rmi.RemoteException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.log4j.Logger;

import de.jowo.pspac.remote.MasterInterface;
import de.jowo.pspac.remote.WorkerInterface;

public class Master implements MasterInterface {
	private static final Logger logger = Logger.getLogger(Master.class);

	private final AtomicLong workerIdCounter = new AtomicLong(0);
	private final ConcurrentMap<Long, WorkerInterface> workers = new ConcurrentHashMap<>();

	@Override
	public long register(WorkerInterface worker) throws RemoteException {
		long workerId = workerIdCounter.incrementAndGet();
		workers.put(workerId, worker);

		logger.info(String.format("Registered worker '%d' with the master (%s)", workerId, worker));

		return workerId;
	}

}

package de.jowo.pspac;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.log4j.Logger;

import de.jowo.pspac.remote.ProgressMonitor;
import de.jowo.pspac.remote.dto.ProgressInfo;

public class LoggingProgressMonitor implements ProgressMonitor {
	private static final Logger logger = Logger.getLogger(LoggingProgressMonitor.class);

	final Lock lock = new ReentrantLock();
	final Condition workerFinished = lock.newCondition();

	private final long workerId;
	private Thread thread;

	private ProgressInfo latestInfo;

	public LoggingProgressMonitor(long workerId) {
		this.workerId = workerId;
	}

	public void setThread(Thread thread) {
		this.thread = thread;
	}

	@Override
	public void reportProgress(ProgressInfo info) {
		this.latestInfo = info;
		logger.debug(String.format("[%d] %d %% - %s", workerId, info.getPercentage(), info.getMessage()));

		// If the worker signals 100% progress we signal the work dispatcher to submit more work
		if (info.getPercentage() == 100) {
			lock.lock();
			workerFinished.signal();
			lock.unlock();
		}
	}

	public ProgressInfo getLatestInfo() {
		return latestInfo;
	}

	public Thread getThread() {
		return thread;
	}

	@Override
	public String toString() {
		return "[" + workerFinished + "] latest = '" + latestInfo == null ? "null" : latestInfo + "'";
	}
}

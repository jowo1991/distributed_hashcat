package de.jowo.pspac;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.log4j.Logger;

import de.jowo.pspac.remote.ProgressMonitor;
import de.jowo.pspac.remote.dto.ProgressInfo;
import de.jowo.pspac.remote.dto.ProgressStatus;

public class LoggingProgressMonitor implements ProgressMonitor {
	private static final Logger logger = Logger.getLogger(LoggingProgressMonitor.class);

	private final Lock lock = new ReentrantLock();
	private final Condition workerFinished = lock.newCondition();

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

		String msg = "Execution on worker failed";
		switch (info.getStatus()) {
			case EXCEPTION:
				logger.error(msg, (Exception) info.getMessage());
				signalWorkFinished();
				break;
			case ERROR:
				logger.error(msg + ": " + info.getMessage());
				signalWorkFinished();
				break;
			case FINISHED:
				signalWorkFinished();
				break;
			default:
			case ACTIVE:
				logger.debug(String.format("[%d] %d %% - %s", workerId, info.getPercentage(), info.getMessage()));
				break;
		}
	}

	private void signalWorkFinished() {
		lock.lock();
		workerFinished.signal();
		lock.unlock();
	}

	/**
	 * Block until the job finished.
	 *
	 * @return Last progress reported by the worker. <br>
	 *         The {@link ProgressInfo#getStatus()} is {@link ProgressStatus#FINISHED} unless an error occurred.
	 * @throws InterruptedException the interrupted exception
	 */
	public ProgressInfo waitForWorker() throws InterruptedException {
		lock.lock();
		workerFinished.await();
		lock.unlock();

		return latestInfo;
	}

	public long getWorkerId() {
		return workerId;
	}

	public ProgressInfo getLatestInfo() {
		return latestInfo;
	}

	public Thread getThread() {
		return thread;
	}

	@Override
	public String toString() {
		return String.format("[%d] latest = '%s'", workerId, latestInfo == null ? "null" : latestInfo.toString());
	}
}

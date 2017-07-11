package de.jowo.pspac.factories;

import java.util.Queue;

import org.apache.log4j.Logger;

import de.jowo.pspac.AbstractFactory;
import de.jowo.pspac.jobs.HashcatJob;
import de.jowo.pspac.jobs.JobInterface;
import de.jowo.pspac.remote.ProgressMonitor;
import de.jowo.pspac.remote.ProgressReporter;

public class SingleMaskFactory extends AbstractFactory {
	private static final Logger logger = Logger.getLogger(SingleMaskFactory.class);

	private String hashcatArguments;
	private String hash;

	public SingleMaskFactory() {
		hashcatArguments = System.getProperty("hashcatargs");
		hash = System.getProperty("hash");

		logger.info("hashcatArguments = '" + hashcatArguments + "'");
		logger.info("hash = '" + hash + "'");

		if (hashcatArguments == null) {
			throw new IllegalStateException("'hashcatargs' is mandatory.");
		}

		if (hash == null) {
			throw new IllegalStateException("'hash' is mandatory.");
		}

		HashcatJob.validateArgsOrThrow(hashcatArguments);
	}

	@Override
	public JobInterface createJob(Queue<String> maskQueue) {
		return new HashcatJob(hash, maskQueue.poll(), hashcatArguments);
	}

	@Override
	public ProgressMonitor createMonitor(long workerId) {
		return getMonitorReporter(workerId);
	}

	@Override
	public ProgressReporter createReporter(long workerId) {
		return getMonitorReporter(workerId);
	}

}

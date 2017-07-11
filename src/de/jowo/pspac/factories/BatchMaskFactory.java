package de.jowo.pspac.factories;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

import org.apache.log4j.Logger;

import de.jowo.pspac.AbstractFactory;
import de.jowo.pspac.jobs.BatchHashcatJob;
import de.jowo.pspac.jobs.JobInterface;
import de.jowo.pspac.remote.ProgressMonitor;
import de.jowo.pspac.remote.ProgressReporter;

public class BatchMaskFactory extends AbstractFactory {
	private static final Logger logger = Logger.getLogger(BatchMaskFactory.class);

	private final String hashcatArguments;
	private final String hash;
	private final int batchSize;

	public BatchMaskFactory() {
		hashcatArguments = System.getProperty("hashcatargs");
		hash = System.getProperty("hash");
		batchSize = Integer.parseInt(System.getProperty("batchsize", "10"));

		logger.info("hashcatArguments = '" + hashcatArguments + "'");
		logger.info("hash = '" + hash + "'");
		logger.info("batchSize = '" + batchSize + "'");

		if (hashcatArguments == null) {
			throw new IllegalStateException("'hashcatargs' is mandatory.");
		}

		if (hash == null) {
			throw new IllegalStateException("'hash' is mandatory.");
		}

		BatchHashcatJob.validateArgsOrThrow(hashcatArguments);
	}

	@Override
	public JobInterface createJob(Queue<String> maskQueue) {
		List<String> masks = new ArrayList<>(batchSize);
		for (int i = 0; i < batchSize; i++) {
			if (maskQueue.isEmpty()) {
				break;
			}
			masks.add(maskQueue.poll());
		}

		return new BatchHashcatJob(hash, masks, hashcatArguments);
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

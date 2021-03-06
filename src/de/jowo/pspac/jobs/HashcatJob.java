package de.jowo.pspac.jobs;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.Arrays;

import org.apache.log4j.Logger;

import de.jowo.pspac.remote.ProgressReporter;
import de.jowo.pspac.remote.dto.ProgressInfo;

public class HashcatJob implements JobInterface {
	private static final long serialVersionUID = 8726661157219926871L;
	private static final Logger logger = Logger.getLogger(HashcatJob.class);

	final String hash;
	final String mask;
	final String args;

	/**
	 * Instantiates a new hashcat job.
	 *
	 * @param hash the hash
	 * @param mask the mask, e.g. ?l?l?l
	 * @param args the args that <b>must</b> contain the placeholders '{mask}' and '{hash}'.
	 * @throws IllegalArgumentException the illegal argument exception
	 */
	public HashcatJob(String hash, String mask, String args) throws IllegalArgumentException {
		this.hash = hash;
		this.mask = mask;
		this.args = validateArgsOrThrow(args);
	}

	public static String validateArgsOrThrow(String args) {
		if (!args.contains("{mask}") || !args.contains("{hash}")) {
			throw new IllegalArgumentException("Invalid arguments. Must contain {mask} and {hash}");
		}

		return args;
	}

	private String getCommand() {
		String arguments = args.replace("{mask}", "\"" + mask + "\"").replace("{hash}", "\"" + hash + "\"");

		String command = "{cmd} {arguments} -a 3 --status --status-timer 2 --potfile-disable";
		command = command.replace("{cmd}", System.getProperty("hashcat", "hashcat64"));
		command = command.replace("{arguments}", arguments);

		return command;
	}

	@Override
	public String toString() {
		return "HashcatJob [hash=" + hash + ", mask=" + mask + ", args=" + args + "]";
	}

	@Override
	public Serializable call(ProgressReporter reporter) throws Exception {
		logger.info("Executing: '" + getCommand() + "'");
		final Process process = Runtime.getRuntime().exec(getCommand());
		final BufferedReader stdOut = new BufferedReader(new InputStreamReader(process.getInputStream()));

		final HashcatParser parser = new HashcatParser(hash);

		try {
			// read the output from the command
			HashcatJobStatus res;
			while ((res = parser.parse(stdOut)) != null) {
				logger.debug("Status: " + res);
				reporter.reportProgress(ProgressInfo.active(res.getProgressPercentage(), res.toString()));
			}

			int code = process.waitFor();
			logger.info("Process terminated: " + code);

			if (code < 0 || code > 1) {
				throw new IllegalStateException("Illegal exit code: " + code);
			}

			if (code == 0 && parser.getResult() == null) {
				return HASH_CRACKED_BUT_NOT_CAPTURED;
			}

			return parser.getResult();
		} finally {
			/**
			 * No matter how we leave this method, the process HAS to be terminated if it still runs!
			 */
			if (process != null && process.isAlive()) {
				logger.warn("Forcibly stopping hashcat process");
				process.destroyForcibly();
			}
		}
	}

	@Override
	public Iterable<String> getMasks() {
		return Arrays.asList(mask);
	}
}

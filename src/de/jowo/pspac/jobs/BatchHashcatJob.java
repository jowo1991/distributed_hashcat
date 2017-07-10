package de.jowo.pspac.jobs;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;

import org.apache.log4j.Logger;

import de.jowo.pspac.LoggingProgressMonitor;
import de.jowo.pspac.remote.ProgressReporter;
import de.jowo.pspac.remote.dto.ProgressInfo;

public class BatchHashcatJob implements JobInterface {
	private static final long serialVersionUID = 3488356811632132272L;
	private static final Logger logger = Logger.getLogger(BatchHashcatJob.class);

	private static final String MASK_FILENAME = "maskfile";
	private static final Path TMP_PATH = Paths.get(System.getProperty("TMP", System.getProperty("java.io.tmpdir")));

	final String hash;
	final Iterable<String> masks;
	final String args;

	/**
	 * Instantiates a new batch hashcat job.
	 *
	 * @param hash the hash
	 * @param masks the masks
	 * @param args the optional arguments
	 * @throws IllegalArgumentException the illegal argument exception
	 */
	public BatchHashcatJob(String hash, Iterable<String> masks, String args) throws IllegalArgumentException {
		this.hash = hash;
		this.masks = masks;
		this.args = validateArgsOrThrow(args);
	}

	public static String validateArgsOrThrow(String args) {
		if (!args.contains("-m")) {
			throw new IllegalArgumentException("Please provide the hash type using '-m <hash>'");
		}

		return args;
	}

	public static void main(String[] args) throws Exception {
		// Logger.getLogger(HashcatParser.class).setLevel(Level.ALL);

		String arg = "-m 0";
		// String hash = "04cf6ab42833951e9f86598d1213ef3e"; // Sstupid
		String hash = "098f6bcd4621d373cade4e832627b4f6"; // test
		// String hash = "74db53da6e2e7d75be37fdd2a7d27828"; // testttttt

		Iterable<String> masks = Arrays.asList("?d", "?d?d", "?l", "?d?d?d?d", "?d?d?d?d?d?d", "?d?d?d?d?d", "?l?l", "?d?d?d", "?u", "?s", "?l?l?l?l");

		BatchHashcatJob hashcatJob = new BatchHashcatJob(hash, masks, arg);
		Object result = hashcatJob.call(new LoggingProgressMonitor(1L));

		logger.info("Final result = " + String.valueOf(result));
	}

	private String getCommand(Path maskfile) {
		String command = "{cmd} {hash} {maskfile} {args} -a 3 --status --status-timer 2 --potfile-disable";
		command = command.replace("{cmd}", System.getProperty("hashcat", "hashcat64"));
		command = command.replace("{hash}", hash);
		command = command.replace("{maskfile}", maskfile.toString());
		command = command.replace("{args}", args);

		return command;
	}

	@Override
	public String toString() {
		return "BatchHashcatJob [hash=" + hash + ", masks=" + masks + ", optArgs=" + args + "]";
	}

	private Path writeMaskfile() throws IOException {
		Path maskfile = TMP_PATH.resolve(MASK_FILENAME);
		if (maskfile.toFile().exists()) {
			maskfile.toFile().delete();
		}

		Files.write(maskfile, masks, Charset.defaultCharset(), StandardOpenOption.CREATE_NEW);

		return maskfile;
	}

	@Override
	public Serializable call(ProgressReporter reporter) throws Exception {
		Path maskfile = writeMaskfile();

		logger.info("Executing: '" + getCommand(maskfile) + "'");
		final Process process = Runtime.getRuntime().exec(getCommand(maskfile));
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
		return masks;
	}
}
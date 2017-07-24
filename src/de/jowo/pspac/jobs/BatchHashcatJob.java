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

import de.jowo.pspac.remote.ProgressReporter;
import de.jowo.pspac.remote.dto.ProgressInfo;

public class BatchHashcatJob implements JobInterface {
	private static final long serialVersionUID = 3488356811632132272L;
	private static final Logger logger = Logger.getLogger(BatchHashcatJob.class);

	private static final String MASK_FILENAME = "maskfile";
	private static final String HASH_FILENAME = "hashfile";
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
			throw new IllegalArgumentException("Please provide the hash type using '-m <hashtype>'");
		}

		return args;
	}

	private String getCommand(Path maskfile, Path hashfile) {
		String command = "{cmd} {hashfile} {maskfile} {args} -a 3 --status --status-timer 2 --potfile-disable";
		command = command.replace("{cmd}", System.getProperty("hashcat", "hashcat64"));
		command = command.replace("{hashfile}", hashfile.toString());
		command = command.replace("{maskfile}", maskfile.toString());
		command = command.replace("{args}", args);

		return command;
	}

	@Override
	public String toString() {
		return "BatchHashcatJob [hash=" + hash + ", masks=" + masks + ", optArgs=" + args + "]";
	}

	private Path writeContentToFile(String filename, Iterable<String> lines) throws IOException {
		Path file = TMP_PATH.resolve(filename);
		if (file.toFile().exists()) {
			file.toFile().delete();
		}
		Files.write(file, lines, Charset.defaultCharset(), StandardOpenOption.CREATE_NEW);

		return file;
	}

	@Override
	public Serializable call(ProgressReporter reporter) throws Exception {
		Path maskfile = writeContentToFile(MASK_FILENAME, masks);
		Path hashfile = writeContentToFile(HASH_FILENAME, Arrays.asList(hash));

		logger.debug(String.format("%s = %s: ", maskfile, Files.readAllLines(maskfile)));
		logger.debug(String.format("%s = %s: ", hashfile, Files.readAllLines(hashfile)));

		String command = getCommand(maskfile, hashfile);
		logger.info("Executing: '" + command + "'");

		final Process process = Runtime.getRuntime().exec(command);
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
		return masks;
	}
}

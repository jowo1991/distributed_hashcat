package de.jowo.pspac.jobs;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

public class HashcatParser {
	private static final Logger logger = Logger.getLogger(HashcatParser.class);

	static final Pattern PARSING_START = Pattern.compile("Session\\.*: .*");
	static final Pattern PROGRESS_PERCENTAGE = Pattern.compile("Progress\\.*: \\d*/\\d* \\((\\d*)");
	static final Pattern PROGRESS = Pattern.compile("Progress\\.*: (.*)");
	static final Pattern TIME_STARTED = Pattern.compile("Time.Started\\.*: (.*)");
	static final Pattern TIME_ESTIMATED = Pattern.compile("Time.Estimated\\.*: (.*)");
	static final Pattern SPEED_DEV = Pattern.compile("Speed.Dev.#.\\.*: *(\\d+.*)");
	static final Pattern GUESS_MASK = Pattern.compile("Guess.Mask\\.*: (.*)");
	static final Pattern GUESS_QUEUE = Pattern.compile("Guess.Queue\\.*: (.*)");

	private final String hash;

	private String result;

	public HashcatParser(String hash) {
		this.hash = hash;
	}

	/**
	 * Parses the following text to provide {@link HashcatJobStatus}s for the master.
	 * 
	 * <pre>
	 * 	Session..........: hashcat
	 * 	Status...........: Quit
	 * 	Hash.Type........: MD5
	 * 	Hash.Target......: 04cf6ab42833951e9f86598d1213ef3e
	 * 	Time.Started.....: Tue Jul 04 15:29:18 2017 (1 min, 10 secs)
	 * 	Time.Estimated...: Tue Jul 04 15:34:26 2017 (3 mins, 58 secs)
	 *  Guess.Mask.......: ?l?l?l?l?d?d?d [7]
	 *  Guess.Queue......: 4/4 (100.00%)
	 * 	Speed.Dev.#3.....:   676.7 MH/s (11.14ms)
	 * 	Recovered........: 0/1 (0.00%) Digests, 0/1 (0.00%) Salts
	 * 	Progress.........: 47681863680/208827064576 (22.83%)
	 * 	Rejected.........: 0/47681863680 (0.00%)
	 * 	Restore.Point....: 2703360/11881376 (22.75%)
	 * 	Candidates.#3....: XFFDAZGU -> KWINYQWW
	 * 	HWMon.Dev.#3.....: Temp: 69c
	 * </pre>
	 * 
	 * Also, whenever the {@link #hash} is seen in the stdout we expect to have broken the hash and set the {@link #result} appropriately.
	 *
	 * @param stdOut reader to access the stdout of the process
	 * @return the hashcat job result or {@code null} when EOF was reached.
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public HashcatJobStatus parse(BufferedReader stdOut) throws IOException {
		HashcatJobStatus res = new HashcatJobStatus();
		String s;
		Matcher matcher;
		boolean isParsing = false;

		while ((s = stdOut.readLine()) != null) {
			logger.trace(s);

			if (PARSING_START.matcher(s).find()) {
				isParsing = true;
			}
			// Detect empty line that terminates values
			else if (res.getProgress() != null && s.equals("")) {
				isParsing = false;
				return res;
			}

			if (s.contains(hash + ":")) {
				result = s;
				return null;
			}

			if (isParsing) {
				matcher = PROGRESS.matcher(s);
				if (matcher.find()) {
					res.setProgress(matcher.group(1));
				}

				matcher = TIME_STARTED.matcher(s);
				if (matcher.find()) {
					res.setTimeStarted(matcher.group(1));
				}

				matcher = TIME_ESTIMATED.matcher(s);
				if (matcher.find()) {
					res.setTimeEstimated(matcher.group(1));
				}

				matcher = SPEED_DEV.matcher(s);
				if (matcher.find()) {
					res.setSpeedDev(matcher.group(1));
				}

				matcher = PROGRESS_PERCENTAGE.matcher(s);
				if (matcher.find()) {
					res.setProgressPercentage(Integer.parseInt(matcher.group(1)));
				}

				matcher = GUESS_MASK.matcher(s);
				if (matcher.find()) {
					res.setGuessMask(matcher.group(1));
				}

				matcher = GUESS_QUEUE.matcher(s);
				if (matcher.find()) {
					res.setGuessQueue(matcher.group(1));
				}
			}
		}

		return null;
	}

	/**
	 * Gets the result or 'null' if no result was found.
	 *
	 * @return the result
	 */
	public String getResult() {
		return result;
	}
}

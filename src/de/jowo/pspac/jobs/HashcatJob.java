package de.jowo.pspac.jobs;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import de.jowo.pspac.LoggingProgressMonitor;
import de.jowo.pspac.remote.ProgressMonitor;
import de.jowo.pspac.remote.dto.ProgressInfo;

public class HashcatJob implements JobInterface {
	private static final long serialVersionUID = 8726661157219926871L;
	private static final Logger logger = Logger.getLogger(HashcatJob.class);

	final String hash;
	final String mask;
	final String args;

	String result;

	public HashcatJob(String hash, String mask, String args) {
		this.hash = hash;
		this.mask = mask;
		this.args = args;
	}

	private String getCommand() {
		String command = "{cmd} {arguments}";
		command = command.replace("{cmd}", "G:/Studium_Master/Sem_2_SS17/PS-PAC/Project/hashcat-3.5.0/hashcat64.exe");
		command = command.replace("{arguments}", args.replace("{mask}", mask).replace("{hash}", hash));

		return command;
	}

	@Override
	public String toString() {
		return "HashcatJob [hash=" + hash + ", mask=" + mask + ", args=" + args + "]";
	}

	public static void main(String[] args) throws Exception {
		String arg = "-m 0 -a 3 {hash} {mask} -D 2 --status-timer 2 --potfile-disable --status";
		// String hash = "04cf6ab42833951e9f86598d1213ef3e"; // Sstupid
		String hash = "098f6bcd4621d373cade4e832627b4f6"; // test
		// String hash = "74db53da6e2e7d75be37fdd2a7d27828"; // testttttt

		HashcatJob hashcatJob = new HashcatJob(hash, "?l?l?l?l?l?l?l", arg);
		Object result = hashcatJob.call(new LoggingProgressMonitor(2L));

		logger.info("Final result = " + String.valueOf(result));
	}

	static final Pattern PARSING_START = Pattern.compile("Session\\.*: .*");
	static final Pattern PROGRESS_PERCENTAGE = Pattern.compile("Progress\\.*: \\d*/\\d* \\((\\d*)");
	static final Pattern PROGRESS = Pattern.compile("Progress\\.*: (.*)");
	static final Pattern TIME_STARTED = Pattern.compile("Time.Started\\.*: (.*)");
	static final Pattern TIME_ESTIMATED = Pattern.compile("Time.Estimated\\.*: (.*)");
	static final Pattern SPEED_DEV = Pattern.compile("Speed.Dev.#.\\.*: *(\\d+.*)");

	/**
	 * <pre>
	 * 	Session..........: hashcat
	 * 	Status...........: Quit
	 * 	Hash.Type........: MD5
	 * 	Hash.Target......: 04cf6ab42833951e9f86598d1213ef3e
	 * 	Time.Started.....: Tue Jul 04 15:29:18 2017 (1 min, 10 secs)
	 * 	Time.Estimated...: Tue Jul 04 15:34:26 2017 (3 mins, 58 secs)
	 * 	Guess.Mask.......: ?u?u?u?u?u?u?u?u [8]
	 * 	Guess.Queue......: 1/1 (100.00%)
	 * 	Speed.Dev.#3.....:   676.7 MH/s (11.14ms)
	 * 	Recovered........: 0/1 (0.00%) Digests, 0/1 (0.00%) Salts
	 * 	Progress.........: 47681863680/208827064576 (22.83%)
	 * 	Rejected.........: 0/47681863680 (0.00%)
	 * 	Restore.Point....: 2703360/11881376 (22.75%)
	 * 	Candidates.#3....: XFFDAZGU -> KWINYQWW
	 * 	HWMon.Dev.#3.....: Temp: 69c
	 * </pre>
	 *
	 * @param stdOut reader to access the stdout of the process
	 * @return the hashcat job result
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private HashcatJobResult parse(BufferedReader stdOut) throws IOException {
		HashcatJobResult res = new HashcatJobResult();
		String s;
		Matcher matcher;
		boolean isParsing = false;

		Pattern resultPattern = Pattern.compile(hash + ":(.*)");

		while ((s = stdOut.readLine()) != null) {
			// System.out.println(s);

			if (PARSING_START.matcher(s).find()) {
				isParsing = true;
			}
			// Detect empty line that terminates values
			else if (res.getProgress() != null && s.equals("")) {
				isParsing = false;
				return res;
			}

			matcher = resultPattern.matcher(s);
			if (matcher.find()) {
				result = matcher.group();
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
			}
		}

		return null;
	}

	@Override
	public Object call(ProgressMonitor monitor) throws Exception {
		logger.info("Executing: '" + getCommand() + "'");
		final Process process = Runtime.getRuntime().exec(getCommand());
		final BufferedReader stdOut = new BufferedReader(new InputStreamReader(process.getInputStream()));

		try {
			// read the output from the command
			HashcatJobResult res;
			while ((res = parse(stdOut)) != null) {
				logger.debug("Status: " + res);
				monitor.reportProgress(ProgressInfo.active(res.getProgressPercentage(), res.toString()));
			}

			logger.info("Process terminated: " + process.waitFor());

			return result;
		} finally {
			/**
			 * No matter how we leave this method, the process HAS to be terminated if it still runs!
			 */
			if (process != null && process.isAlive()) {
				logger.warn("Forcibly stopping process");
				process.destroyForcibly();
			}
		}
	}
}

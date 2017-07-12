package de.jowo.pspac.jobs;

import java.io.Serializable;

import de.jowo.pspac.remote.ProgressReporter;

/**
 * Interface for all Jobs that will be executed on the <b>worker nodes</b>.
 */
public interface JobInterface extends Serializable {
	public static final String HASH_CRACKED_BUT_NOT_CAPTURED = "Hash cracked but not captured. Please read logs! This only happens when using '--username'";

	/**
	 * Executes the job; can take a long while.<br>
	 * The progress is reported to the <b>master</b> using the given {@code reporter}.
	 * @param reporter
	 * @return {@code null} to indicate the final result was <b>not</b> found, {@code true} / {@code Object} otherwise.
	 * @throws Exception
	 */
	public Serializable call(ProgressReporter reporter) throws Exception;

	/**
	 * Gets the masks the job is working on.
	 *
	 * @return the masks
	 */
	public Iterable<String> getMasks();
}

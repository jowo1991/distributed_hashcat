package de.jowo.pspac.jobs;

import java.io.Serializable;

import de.jowo.pspac.remote.ProgressMonitor;

/**
 * Marker interface for Jobs.
 */
public interface JobInterface extends Serializable {
	/**
	 * Executes the job. Can take a long while.
	 * @param monitor
	 * @return {@code null} to indicate the final result was <b>not</b> found, {@code true} / {@code Object} otherwise.
	 * @throws Exception
	 */
	public Serializable call(ProgressMonitor monitor) throws Exception;
}

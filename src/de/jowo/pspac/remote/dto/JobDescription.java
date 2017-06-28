package de.jowo.pspac.remote.dto;

import java.io.Serializable;
import java.util.Map;

import de.jowo.pspac.remote.ProgressMonitor;

/**
 * States what kind of job should be executed. Could for example be a:
 * <ul>
 * <li>hashcat job</li>
 * <li>java hash generation job</li>
 * <li>...</li>
 * </ul>
 */
public interface JobDescription extends Serializable {

	/**
	 * Gets the name for this job
	 *
	 * @return the name
	 */
	public String getName();

	/**
	 * Gets the parameters for this job.
	 *
	 * @return the parameters
	 */
	public Map<String, Object> getParameters();

	/**
	 * Executes the job.
	 */
	public void execute(ProgressMonitor monitor);
}

package de.jowo.pspac.jobs;

import java.io.Serializable;

import de.jowo.pspac.remote.ProgressMonitor;

/**
 * Marker interface for Jobs.
 */
public interface JobInterface extends Serializable {
	public Object call(ProgressMonitor monitor) throws Exception;
}

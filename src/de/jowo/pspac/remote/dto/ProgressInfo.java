package de.jowo.pspac.remote.dto;

import java.io.Serializable;

public interface ProgressInfo extends Serializable {
	/**
	 * Gets the percentage of the progress.
	 *
	 * @return the percentage
	 */
	public int getPercentage();

	/**
	 * Gets the message.
	 *
	 * @return the message
	 */
	public String getMessage();
}

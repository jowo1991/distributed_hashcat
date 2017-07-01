package de.jowo.pspac.remote.dto;

import java.io.Serializable;

public class ProgressInfo implements Serializable {
	private static final long serialVersionUID = -4713928045060738775L;

	private int percentage;
	private String message;

	public ProgressInfo(int percentage, String message) {
		this.percentage = percentage;
		this.message = message;
	}

	/**
	 * Gets the percentage.
	 *
	 * @return the percentage
	 */
	public int getPercentage() {
		return percentage;
	}

	/**
	 * Gets the message.
	 *
	 * @return the message
	 */
	public String getMessage() {
		return message;
	}
}

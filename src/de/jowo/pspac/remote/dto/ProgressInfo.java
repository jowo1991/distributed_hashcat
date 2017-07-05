package de.jowo.pspac.remote.dto;

import java.io.Serializable;

public class ProgressInfo implements Serializable {
	private static final long serialVersionUID = -4713928045060738775L;

	private final ProgressStatus status;
	private final int percentage;
	private final Serializable message;

	public static ProgressInfo active(int percentage, String message) {
		return new ProgressInfo(ProgressStatus.ACTIVE, percentage, message);
	}

	public static ProgressInfo error(Serializable message) {
		return new ProgressInfo(ProgressStatus.ERROR, -1, message);
	}

	public static ProgressInfo exception(Exception ex) {
		return new ProgressInfo(ProgressStatus.EXCEPTION, -1, ex);
	}

	public static ProgressInfo finished(Serializable result) {
		return new ProgressInfo(ProgressStatus.FINISHED, -1, result);
	}

	private ProgressInfo(ProgressStatus status, int percentage, Serializable message) {
		this.percentage = percentage;
		this.message = message;
		this.status = status;
	}

	public ProgressStatus getStatus() {
		return status;
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
	public Serializable getMessage() {
		return message;
	}

	@Override
	public String toString() {
		return String.format("[%d %%] %s - %s", percentage, status, message);
	}
}

package de.jowo.pspac.jobs;

public class HashcatJobStatus {
	private String progress;
	private String timeStarted;
	private String timeEstimated;
	private String speedDev;
	private int progressPercentage;
	private String guessMask;
	private String guessQueue;

	public String getTimeStarted() {
		return timeStarted;
	}

	public void setTimeStarted(String timeStarted) {
		this.timeStarted = timeStarted;
	}

	public String getTimeEstimated() {
		return timeEstimated;
	}

	public void setTimeEstimated(String timeEstimated) {
		this.timeEstimated = timeEstimated;
	}

	public String getProgress() {
		return progress;
	}

	public void setProgress(String progress) {
		this.progress = progress;
	}

	public String getSpeedDev() {
		return speedDev;
	}

	public void setSpeedDev(String speedDev) {
		this.speedDev = speedDev;
	}

	public int getProgressPercentage() {
		return progressPercentage;
	}

	public void setProgressPercentage(int progressPercentage) {
		this.progressPercentage = progressPercentage;
	}

	public String getGuessMask() {
		return guessMask;
	}

	public void setGuessMask(String guessMask) {
		this.guessMask = guessMask;
	}

	public String getGuessQueue() {
		return guessQueue;
	}

	public void setGuessQueue(String guessQueue) {
		this.guessQueue = guessQueue;
	}

	@Override
	public String toString() {
		return "HashcatJobStatus [" + "guessMask=" + guessMask + ", guessQueue=" + guessQueue + ", progress=" + progress + ", timeStarted=" + timeStarted
				+ ", timeEstimated=" + timeEstimated + ", speedDev=" + speedDev + ", progressPercentage=" + progressPercentage + "]";
	}

}

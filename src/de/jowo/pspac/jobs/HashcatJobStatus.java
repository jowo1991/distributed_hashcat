package de.jowo.pspac.jobs;

public class HashcatJobStatus {
	private String progress;
	private String timeStarted;
	private String timeEstimated;
	private String speedDev;
	private int progressPercentage;

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

	@Override
	public String toString() {
		return "HashcatJobResult [progress=" + progress + ", timeStarted=" + timeStarted + ", timeEstimated=" + timeEstimated + ", speedDev=" + speedDev
				+ ", progressPercentage=" + progressPercentage + "]";
	}
}

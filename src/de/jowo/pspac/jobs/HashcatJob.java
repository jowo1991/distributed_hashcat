package de.jowo.pspac.jobs;

public class HashcatJob implements JobInterface {
	private static final long serialVersionUID = 8726661157219926871L;

	final String mask;

	public HashcatJob(String mask) {
		this.mask = mask;
	}

	@Override
	public String toString() {
		if (mask != null) {
			return "HashcatJob [" + mask + "]";
		} else {
			return "HashcatJob [null]";
		}
	}
}

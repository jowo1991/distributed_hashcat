package de.jowo.pspac.remote;

import java.rmi.Remote;

public interface JobControl extends Remote {
	/**
	 * Cancels the execution of the current Job.
	 */
	public void cancel();
}

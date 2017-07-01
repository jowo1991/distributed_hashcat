package de.jowo.pspac.remote;

import java.rmi.Remote;
import java.rmi.RemoteException;

import de.jowo.pspac.remote.dto.ProgressInfo;

public interface ProgressMonitor extends Remote {
	/**
	 * Reports the progress to the <b>master node</b>.
	 *
	 * @param progressInfo the progress info
	 */
	public void reportProgress(ProgressInfo progressInfo) throws RemoteException;
}

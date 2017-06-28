package de.jowo.pspac;

import java.rmi.RemoteException;

import de.jowo.pspac.exceptions.NodeBusyException;
import de.jowo.pspac.remote.JobControl;
import de.jowo.pspac.remote.ProgressMonitor;
import de.jowo.pspac.remote.WorkerInterface;

public class Worker implements WorkerInterface {

	@Override
	public JobControl submitJob(ProgressMonitor monitor) throws RemoteException, NodeBusyException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void terminate() throws RemoteException {
		// TODO Auto-generated method stub

	}
}

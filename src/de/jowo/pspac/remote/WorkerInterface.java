package de.jowo.pspac.remote;

import java.rmi.Remote;
import java.rmi.RemoteException;

import de.jowo.pspac.exceptions.NodeBusyException;

/**
 * The Interface implemented by the <b>worker nodes</b>.<br>
 * The <b>master node</b> uses this interface to:
 * <ul>
 * <li>Submit work</li>
 * <li>Monitor progress</li>
 * <li>Terminate nodes (when the overall calculation finished)</li>
 * </ul>
 */
public interface WorkerInterface extends Remote {

	/**
	 * Submits the given job for execution on the <b>worker node</b>.<br>
	 * Each worker node can execute only exactly one job.
	 *
	 * @param monitor the monitor used to monitor the progress of the execution, typically the <b>master node</b>
	 * @return the job control
	 * @throws RemoteException the remote exception
	 * @throws NodeBusyException the node is busy executing a job that hasn't finished yet
	 */
	public JobControl submitJob(ProgressMonitor monitor) throws RemoteException, NodeBusyException;

	/**
	 * Terminates the worker node.
	 *
	 * @throws RemoteException the remote exception
	 */
	public void terminate() throws RemoteException;
}

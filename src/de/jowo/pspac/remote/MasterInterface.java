package de.jowo.pspac.remote;

import java.rmi.Remote;
import java.rmi.RemoteException;

import de.jowo.pspac.exceptions.RegistrationFailedException;

/**
 * The Interface implemented by the <b>master node</b>. <br>
 * Worker nodes can register themselves with the master node that will then submit work on them and monitor the progress.
 */
public interface MasterInterface extends Remote {

	/**
	 * Registers a worker with the master node.
	 *
	 * @param worker the worker
	 * @return unique id that the master assigned for this worker
	 * @throws RegistrationFailedException the registration failed exception
	 * @throws RemoteException the remote exception
	 */
	public long register(WorkerInterface worker) throws RegistrationFailedException, RemoteException;
}

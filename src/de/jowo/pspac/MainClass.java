/**
 * 
 */
package de.jowo.pspac;

import java.net.InetAddress;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import org.apache.log4j.Logger;

import de.jowo.pspac.exceptions.RegistrationFailedException;
import de.jowo.pspac.remote.MasterInterface;

/**
 * Entry point for the computation.<br>
 * The following properties are used depending on the mode:
 * <ul>
 * <li>mode - Either 'master' or 'worker' (DEFAULT = worker)</li>
 * <li>masterhost - Master to register the worker with. Only relevant if mode = 'worker'</li>
 * <li>masterport - The port to connect to the master or setup the master depending on the mode (DEFAULT = 2000)</li>
 * </ul>
 * 
 * @author Jo
 */
public class MainClass {
	private static final Logger logger = Logger.getLogger(MainClass.class);

	public static final String MASTER_DEFAULT_PORT = "2000";
	public static final String MASTER_LOOKUP_ALIAS = "MASTER";

	public static final String PROP_MASTER_HOST = "masterhost";
	public static final String PROP_MASTER_PORT = "masterport";
	public static final String PROP_MODE = "mode";

	public static final String PROP_MIN_WORKERS = "minWorkers";

	private static enum NodeMode {
		MASTER, WORKER
	}

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		String nodeMode = System.getProperty(PROP_MODE, "worker").toUpperCase();
		NodeMode mode = nodeMode.equals("MASTER") ? NodeMode.MASTER : NodeMode.WORKER;
		switch (mode) {
			case MASTER:
				runMaster();
				break;

			case WORKER:
				runWorker();
				break;

			default:
				IllegalArgumentException ex = new IllegalArgumentException("Invalid mode = " + mode);
				logger.fatal("Failed to start node", ex);
				throw ex;
		}
	}

	public static void runMaster() throws Exception {
		Master master = new Master();
		UnicastRemoteObject.exportObject(master, 0);

		int port = Integer.parseInt(System.getProperty(PROP_MASTER_PORT, MASTER_DEFAULT_PORT));
		String host = InetAddress.getLocalHost().getHostName();

		Registry registry = LocateRegistry.createRegistry(port);
		registry.bind(MASTER_LOOKUP_ALIAS, master);

		logger.info(String.format("Master ready at %s:%d", host, port));
	}

	public static void runWorker() throws RemoteException {
		String masterhost = System.getProperty(PROP_MASTER_HOST);
		if (masterhost == null || masterhost.equals("")) {
			logger.fatal("Can't start worker with no '" + PROP_MASTER_HOST + "' configuration");
			return;
		}

		int masterport = Integer.parseInt(System.getProperty(PROP_MASTER_PORT, MASTER_DEFAULT_PORT));

		Registry registry = LocateRegistry.getRegistry(masterhost, masterport);
		MasterInterface master = null;
		try {
			master = (MasterInterface) registry.lookup(MASTER_LOOKUP_ALIAS);
		} catch (NotBoundException e) {
			logger.fatal("Failed to lookup master node. Terminating the worker NOW.", e);
			return;
		}

		Worker worker = new Worker();
		UnicastRemoteObject.exportObject(worker, 0);
		long workerId;
		try {
			workerId = master.register(worker);
			logger.info(String.format("Successfully registerd worker = %d with the master (%s)", workerId, master));
		} catch (RegistrationFailedException e) {
			logger.fatal("Failed to register node with master", e);
		}
	}
}

/**
 * 
 */
package de.jowo.pspac;

import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

import de.jowo.pspac.exceptions.RegistrationFailedException;
import de.jowo.pspac.factories.BatchMaskFactory;
import de.jowo.pspac.factories.DemoFactory;
import de.jowo.pspac.factories.SingleMaskFactory;
import de.jowo.pspac.remote.MasterInterface;

/**
 * Entry point for the computation.<br>
 * 
 * <h1>Properties for the worker:</h1>
 * <ul>
 * <li>mode - The mode to be used, here: 'worker' (DEFAULT = worker) - optional</li>
 * <li>masterhost - Hostname of the 'master' to register the worker with, e.g. 'faui003' - <b>mandatory</b></li>
 * <li>masterport - The port to connect to the master or setup the master depending on the mode (DEFAULT = 2000) - optional</li>
 * <li>LOG_DIR - The path for all log files, e.g. "/tmp/pspac_logs" (DEFAULT = java.io.tmpdir) - optional</li>
 * </ul>
 * 
 * <h1>Properties for the master:</h1>
 * 
 * <h2>Independent of the factory:</h2>
 * <ul>
 * <li>mode - The mode to be used, here: 'master' - <b>mandatory</b></li>
 * <li>maskfile - The path to the maskfile to be used for hash breaking - <b>mandatory</b></li>
 * <li>LOG_DIR - The path for all log files, e.g. "/tmp/pspac_logs" (DEFAULT = java.io.tmpdir)</li>
 * </ul>
 * 
 * <h2>Using {@link SingleMaskFactory}</h2>
 * <ul>
 * <li>factory = SingleMaskFactory</li>
 * <li>hash - The hash to be broken, e.g. "098f6bcd4621d373cade4e832627b4f6" - <b>mandatory</b></li>
 * <li>hashcatargs - The arguments to pass to hashcat, e.g. "-m 0 {hash} {mask} -D 2" - <b>mandatory</b></li>
 * </ul>
 * 
 * <h2>Using {@link BatchMaskFactory}</h2>
 * <ul>
 * <li>factory = BatchMaskFactory</li>
 * <li>hash - The hash to be broken, e.g. "098f6bcd4621d373cade4e832627b4f6" - <b>mandatory</b></li>
 * <li>hashcatargs - The hash function to be used, e.g. "-m 0" - <b>mandatory</b></li>
 * <li>batchsize - The batch size - optional (DEFAULT = 10)</li>
 * </ul>
 * 
 * <h2>Using {@link DemoFactory}</h2>
 * <ul>
 * <li>delayMillis - The delay between each simulated mask - optional(DEFAULT = 100)</li>
 * <li>batchsize - The batch size - optional (DEFAULT = 10)</li>
 * </ul>
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

	private static final String LOG_DIR = System.getProperty("LOG_DIR", System.getProperty("java.io.tmpdir"));

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

	private static void initLogger(Path filePath) {
		if (System.getProperty("log4j.configuration") != null) {
			return;
		}

		if (filePath.toFile().exists()) {
			filePath.toFile().delete();
		}

		FileAppender fa = new FileAppender();
		fa.setName("FileLogger");
		fa.setFile(filePath.toString());
		fa.setLayout(new PatternLayout("%d %-5p %c{1}:%L - %m%n"));
		fa.setThreshold(Level.DEBUG);
		fa.setAppend(true);
		fa.activateOptions();

		Logger.getRootLogger().addAppender(fa);
		System.out.println("Appending logger " + fa.getFile());
	}

	public static void runMaster() throws Exception {
		initLogger(Paths.get(LOG_DIR, "pspac_master.log"));

		Master master = new Master();
		UnicastRemoteObject.exportObject(master, 0);

		int port = Integer.parseInt(System.getProperty(PROP_MASTER_PORT, MASTER_DEFAULT_PORT));
		String host = InetAddress.getLocalHost().getHostName();

		// Setup RMI
		Registry registry = LocateRegistry.createRegistry(port);
		registry.bind(MASTER_LOOKUP_ALIAS, master);

		// Setup JMX
		MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
		ObjectName name = new ObjectName("de.jowo.pspac:type=MasterControl");
		mbs.registerMBean(master, name);

		logger.info(String.format("Master ready at %s:%d", host, port));
	}

	public static void runWorker() throws RemoteException, UnknownHostException {
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
			worker.setWorkerId(workerId);
			logger.info(String.format("Successfully registerd worker = %d with the master (%s)", workerId, master));

			initLogger(Paths.get(LOG_DIR, String.format("pspac_%d.log", workerId)));

			logger.info(String.format("Worker started up on host '%s'", InetAddress.getLocalHost().getHostName()));
		} catch (RegistrationFailedException e) {
			logger.fatal("Failed to register node with master", e);
		}
	}
}

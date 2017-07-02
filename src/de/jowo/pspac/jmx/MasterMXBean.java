package de.jowo.pspac.jmx;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.List;

import de.jowo.pspac.Master;

/**
 * JMX monitoring interface for the {@link Master}.
 */
public interface MasterMXBean {
	public List<String> getMonitors();

	public List<String> getMaskRows();

	public String getProgress();

	public double getProgressPercentage();

	public List<String> getWorkers();

	public int getWorkerCount();

	public void runJob() throws IOException, RemoteException;
}

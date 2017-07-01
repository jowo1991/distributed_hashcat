package de.jowo.pspac.jmx;

import java.util.List;
import java.util.Queue;

import de.jowo.pspac.LoggingProgressMonitor;
import de.jowo.pspac.Master;

/**
 * JMX monitoring interface for the {@link Master}.
 */
public interface MasterMXBean {
	public List<LoggingProgressMonitor> getMonitors();

	public Queue<String> getMaskRows();
}

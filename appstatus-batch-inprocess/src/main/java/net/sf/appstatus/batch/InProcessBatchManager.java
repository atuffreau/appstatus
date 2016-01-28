package net.sf.appstatus.batch;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.Vector;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.appstatus.core.batch.IBatch;
import net.sf.appstatus.core.batch.IBatchManager;
import net.sf.appstatus.core.batch.IBatchProgressMonitor;

/**
 * This implementation stores batch history and status in memory.
 * <p>
 * Can be used when batch is run within the same JVM as the console.
 * <p>
 * History is lost on restart.
 *
 * @author Nicolas Richeton
 *
 */
public class InProcessBatchManager implements IBatchManager {

	private static Logger logger = LoggerFactory.getLogger(InProcessBatchManager.class);

	private List<IBatch> errorBatches = new Vector<IBatch>();

	List<IBatch> finishedBatches = new Vector<IBatch>();
	private int logInterval = 1000;
	private long maxSize = 25;
	List<IBatch> runningBatches = new Vector<IBatch>();

	private int zombieInterval;

	public IBatch addBatch(String name, String group, String uuid) {

		// Add batch
		IBatch b = new Batch(uuid, name, group);
		((Batch) b).setZombieInterval(this.zombieInterval);

		int currentPosition = runningBatches.indexOf(b);
		if (currentPosition >= 0) {
			// Reuse existing object (and keep monitor).
			b = runningBatches.get(currentPosition);
		} else {
			// Add new batch

			// initMonitor
			getMonitor(b);

			// runningBatches is not limited in size.
			runningBatches.add(b);
		}

		return b;
	}

	protected void addTo(List<IBatch> l, IBatch b) {
		// Ensure batch list does not exceed defined size
		if (l.size() >= maxSize) {
			l.remove(0);
		}

		l.add(b);
	}

	public void batchEnd(Batch batch) {
		runningBatches.remove(batch);
		addTo(finishedBatches, batch);

		if (!batch.isSuccess()) {
			addTo(errorBatches, batch);
		}
	}

	public List<IBatch> getBatches(String group, String name) {
		List<IBatch> result = new ArrayList<IBatch>();

		for (IBatch b : runningBatches) {
			if (StringUtils.equals(group, b.getGroup()) && StringUtils.equals(name, b.getName())) {
				result.add(b);
			}
		}

		for (IBatch b : finishedBatches) {
			if (StringUtils.equals(group, b.getGroup()) && StringUtils.equals(name, b.getName())) {
				result.add(b);
			}
		}

		return result;
	}

	public Properties getConfiguration() {
		return null;
	}

	public List<IBatch> getErrorBatches() {
		return errorBatches;
	}

	public List<IBatch> getFinishedBatches() {
		return finishedBatches;
	}

	public IBatchProgressMonitor getMonitor(IBatch batch) {
		Batch b = (Batch) batch;
		// If batch has no progress monitor, create one.
		if (b.getProgressMonitor() == null) {
			// Calling this method automatically call
			// IBatch#setProgressMonitor()
			new InProcessBatchProgressMonitor(batch.getUuid(), batch, this);
		}

		// Return current monitor.
		return b.getProgressMonitor();
	}

	public List<IBatch> getRunningBatches() {
		return runningBatches;
	}

	public void init() {
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see net.sf.appstatus.core.batch.IBatchManager#removeAllBatches(int)
	 */
	public void removeAllBatches(int scope) {

		if (finishedBatches == null || finishedBatches.size() == 0) {
			// Nothing can be removed using scoped.
			return;
		}

		ArrayList<IBatch> toRemove = new ArrayList<IBatch>();
		switch (scope) {
		case REMOVE_SUCCESS:
			for (IBatch b : finishedBatches) {
				if (b.isSuccess() && (b.getRejectedItemsId() == null || b.getRejectedItemsId().size() == 0)) {
					toRemove.add(b);
				}
			}
			break;

		case REMOVE_OLD:
			// Create reference date.
			Calendar c = Calendar.getInstance();
			c.add(Calendar.MONTH, -6);
			Date thisIsOld = c.getTime();

			for (IBatch b : finishedBatches) {
				if (b.getLastUpdate().before(thisIsOld)) {
					toRemove.add(b);
				}
			}
			break;
		}

		// Remove all batches marked for deletion.
		for (IBatch b : toRemove) {
			removeBatch(b);
		}

	}

	private void removeBatch(IBatch b) {

		// Remove only finished jobs
		if (!runningBatches.contains(b)) {

			// remove from errorBatches
			if (errorBatches.contains(b)) {
				errorBatches.remove(b);
			}

			// Remove from finished batches.
			if (finishedBatches.contains(b)) {
				finishedBatches.remove(b);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * net.sf.appstatus.core.batch.IBatchManager#removeBatch(net.sf.appstatus
	 * .core.batch.IBatch)
	 */
	public void removeBatch(String uuid) {
		Batch b = new Batch(uuid);
		removeBatch(b);
	}

	public void setConfiguration(Properties configuration) {
		try {

			logInterval = Integer.parseInt(configuration.getProperty("batch.logInterval"));
		} catch (NumberFormatException e) {
			logInterval = 1000;
		}
		logger.info("Batch log interval: {}ms", logInterval);

		try {
			maxSize = Integer.parseInt(configuration.getProperty("batch.maxHistory"));
		} catch (NumberFormatException e) {
			maxSize = 25;
		}
		logger.info("Batch history size: {}", maxSize);

		try {
			zombieInterval = Integer.parseInt(configuration.getProperty("batch.zombieInterval"));
		} catch (NumberFormatException e) {
			zombieInterval = 1000 * 60 * 10;
		}
		logger.info("Zombie interval: {}", zombieInterval);

	}
}

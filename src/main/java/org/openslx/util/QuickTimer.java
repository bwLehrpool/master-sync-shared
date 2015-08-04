package org.openslx.util;

import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.Logger;

/**
 * This is a global, static {@link Timer} you can use anywhere for repeating
 * tasks that will <b>not take a significant amount of time to execute</b>. This
 * means they should not run any complex data base queries (better yet, none at
 * all) or do heavy file I/O, etc..
 * The main reason for this class is to prevent having {@link Timer} threads
 * everywhere in the server for trivial tasks.
 */
public class QuickTimer {
	
	private static final Logger LOGGER = Logger.getLogger(QuickTimer.class);

	private static final Timer timer = new Timer("QuickTimer", true);

	public static void scheduleAtFixedDelay(Task task, long delay, long period) {
		timer.schedule(task, delay, period);
	}

	public static void scheduleAtFixedRate(Task task, long delay, long period) {
		timer.scheduleAtFixedRate(task, delay, period);
	}

	public static void scheduleOnce(Task task, long delay) {
		timer.schedule(task, delay);
	}

	public static void scheduleOnce(Task timerTask) {
		scheduleOnce(timerTask, 1);
	}

	/**
	 * Cancel this timer. Should only be called when the server is shutting
	 * down.
	 */
	public static void cancel() {
		timer.cancel();
	}
	
	public static abstract class Task extends TimerTask {

		@Override
		public final void run() {
			try {
				fire();
			} catch (Throwable t) {
				LOGGER.warn("A Task threw an exception!", t);
			}
		}
		
		public abstract void fire();
		
	}

}

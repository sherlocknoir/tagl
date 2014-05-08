/*
 * cron4j - A pure Java cron-like scheduler
 * 
 * Copyright (C) 2007-2009 Carlo Pelliccia (www.sauronsoftware.it)
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License version
 * 2.1, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License 2.1 for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License version 2.1 along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package it.sauronsoftware.cron4j.bundle;

import it.sauronsoftware.cron4j.InvalidPatternException;
import it.sauronsoftware.cron4j.Scheduler;
import it.sauronsoftware.cron4j.Task;
import it.sauronsoftware.cron4j.service.ICronCmd;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.ServiceProperty;
import org.apache.felix.ipojo.annotations.Validate;
import org.apache.felix.ipojo.whiteboard.Wbp;
import org.apache.felix.service.command.CommandSession;
import org.apache.felix.service.command.Descriptor;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.service.log.LogService;

/**
 * This iPOJO component provides a cron deamon for cron4j. It uses the
 * whiteboard pattern handler.
 * 
 * @author Didier Donsez
 * @TODO add logging with the LogService
 * @TODO add a command to list the current scheduler tasks
 */
@Component(name = "TransientCronDeamon", architecture = true)
@Provides()
@Wbp(filter = "(&(&(objectClass=java.lang.Runnable)(cron4j.pattern=*))(!(service.pid=*)))", onArrival = "onArrival", onDeparture = "onDeparture", onModification = "onModification")
public class TransientCronDeamon implements ICronCmd {

	// for future usage
	class StatRunnable implements Runnable {
		public StatRunnable(Runnable runnable) {this.runnable=runnable;}
		private String taskId=null;
		private Runnable runnable;
		private int counter=0;
		private long lastInvocationTime=0;
		private long cumulatedInvocationTime=0;
		
		public void run() {
			lastInvocationTime=System.currentTimeMillis();
			runnable.run();
			counter++;
			cumulatedInvocationTime+=System.currentTimeMillis()-lastInvocationTime;
		}
		public int getCounter() {
			return counter;
		}
		public long getLastInvocationTime() {
			return lastInvocationTime;
		}
		public long getCumulatedInvocationTime() {
			return cumulatedInvocationTime;
		}
		public String getTaskId() {
			return taskId;
		}
		public void setTaskId(String taskId) {
			this.taskId = taskId;
		}
	}
	
	
	private long startTime;

	private BundleContext bundleContext;

	private Scheduler scheduler;

	private Map<Long, String> tasks = new HashMap<Long, String>();
//	private Map<Long, StatRunnable> tasks = new HashMap<Long, StatRunnable>();

	// @Requires(optional=false)
	// @Requires(optional=true)
	@org.apache.felix.ipojo.handler.temporal.Requires
	// @org.apache.felix.ipojo.handler.temporal.Temporal if version >= 1.7.0
	private LogService logService;

	public TransientCronDeamon(BundleContext bundleContext) {
		this.bundleContext = bundleContext;
		scheduler = new Scheduler();
	}

	@Validate
	public void starting() {
		System.out.println("Cron4J: validate");
		startTime = System.currentTimeMillis();
		scheduler.start();
	}

	@Invalidate
	public void stopping() {
		System.out.println("Cron4J: invalidate");
		scheduler.stop();
		scheduler = null;
	}

	/*
	 *  whiteboard pattern methods
	 */
	public void onArrival(ServiceReference ref) {
		System.out.println("Cron4J: onArrival");
		Runnable runnable = (Runnable) bundleContext.getService(ref);
		String pattern = (String) ref
				.getProperty(it.sauronsoftware.cron4j.service.Constants.PATTERN_PROPERTYNAME);
		Long serviceId = (Long) ref.getProperty(Constants.SERVICE_ID);
		if (runnable != null) {
			try {
				
				//String taskId = scheduler.schedule(pattern, new StatRunnable(runnable));
				String taskId = scheduler.schedule(pattern, runnable);
				tasks.put(serviceId, taskId);
				if (logService != null)
					logService.log(LogService.LOG_INFO, "schedule #"
							+ serviceId + " with " + pattern + " (taskId="
							+ taskId + ")");
			} catch (InvalidPatternException e) {
				if (logService != null)
					logService.log(LogService.LOG_ERROR, "deschedule #"
							+ serviceId + " Reason is: " + e.getMessage());
				bundleContext.ungetService(ref);
			}
		}
	}

	public void onDeparture(ServiceReference ref) {
		System.out.println("Cron4J: onDeparture");
		Long serviceId = (Long) ref.getProperty(Constants.SERVICE_ID);
		String taskId = tasks.remove(serviceId);
		if (taskId != null) {
			scheduler.deschedule(taskId);
			bundleContext.ungetService(ref);
			if (logService != null)
				logService.log(LogService.LOG_INFO, "deschedule #" + serviceId
						+ " (taskId=" + taskId + ")");
		}
	}

	public void onModification(ServiceReference ref) {
		System.out.println("Cron4J: onModification");
		String pattern = (String) ref
				.getProperty(it.sauronsoftware.cron4j.service.Constants.PATTERN_PROPERTYNAME);
		Long serviceId = (Long) ref.getProperty(Constants.SERVICE_ID);
		String taskId = tasks.get(serviceId);
		try {
			if (taskId != null) {
				scheduler.reschedule(taskId, pattern);
				if (logService != null)
					logService.log(LogService.LOG_INFO, "reschedule #"
							+ serviceId + " with " + pattern + " (taskId="
							+ taskId + ")");
			}
		} catch (InvalidPatternException e) {
			scheduler.deschedule(taskId);
			tasks.remove(serviceId);
			bundleContext.ungetService(ref);
			if (logService != null)
				logService.log(LogService.LOG_ERROR, "deschedule #" + serviceId
						+ " (taskId=" + taskId + ") Reason is: "
						+ e.getMessage());
		}
	}

	/*
	 * Defines the gogo shell commmands
	 */

	@ServiceProperty(name = "osgi.command.scope", value = "cron")
	String m_scope;

	@ServiceProperty(name = "osgi.command.function", value = "{}")
	String[] m_function = new String[] { "schedules", "stats" };


	@Descriptor("list of schedules")
	public void schedules() {
		System.out.println("Cron4J: list of schedules");
		for (Iterator iterator = tasks.entrySet().iterator(); iterator
				.hasNext();) {
			Map.Entry<Long, String> entry = (Map.Entry<Long, String>) iterator
					.next();
			long serviceId=entry.getKey();
			String taskId=entry.getValue();
			Task task=scheduler.getTask(taskId);
			
			System.out.println("service #" +serviceId+ " is scheduled with (" + scheduler.getSchedulingPattern(taskId)
					+ ") by task " + taskId
					+(task.supportsCompletenessTracking()?" supportsCompletenessTracking":" ")
					+(task.supportsStatusTracking()?" supportsStatusTracking":" ")
			);
			
		}
	}

	@Descriptor("statistics")
	public void stats() {
		System.out.println("Cron4J statistics");
		System.out.println("uptime : "
				+ (System.currentTimeMillis() - startTime) / 1000 + " seconds");
	}
	
	@Descriptor("pause of schedule")
	public boolean pause(long serviceId) throws Exception  {
		// TODO
	}

	@Descriptor("resume of schedule")
	public boolean resume(long serviceId) throws Exception {
		// TODO
	}
}

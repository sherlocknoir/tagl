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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.apache.felix.ipojo.whiteboard.Wbp;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.service.log.LogService;

/**
 * This iPOJO component provides a cron deamon for cron4j. It uses the whiteboard pattern handler.
 * @author Didier Donsez
 * @TODO add logging with the LogService
 * @TODO add a command to list the task id
 * @TODO enables to tolerate services unbinding if the service has a service.pid
 * @TODO make persistent the schedules for services with service.pid
 */
@Component(name = "PersistentCronDeamon", architecture = true)
@Wbp(filter="(&(objectClass=java.lang.Runnable)(cron4j.pattern=*)(service.pid=*))", 
            onArrival="onArrival", 
            onDeparture="onDeparture",
            onModification="onModification")
public class PersistentCronDeamon {
	
	public final String TASKS_FILEPATH="./tasks.ser";
	
	private BundleContext bundleContext;

    private Scheduler scheduler;
	
	private Map<String, PersistentScheduledRunnable> persistentTasks;

	@Requires(optional=true)
	private LogService logService;
	

	public PersistentCronDeamon(BundleContext bundleContext) {
		this.bundleContext=bundleContext;
		loadPersistentTask();
	}
	
    @Validate
    public void starting() {
    	System.out.println("starting");
		scheduler.start();
    }

    @Invalidate
    public void stopping() {
       	System.out.println("stopping");
		scheduler.stop();
		scheduler=null;
    }

	private void savePersistentTask(){
		try {
			ObjectOutputStream oos=new ObjectOutputStream(new FileOutputStream(bundleContext.getDataFile(TASKS_FILEPATH)));
			oos.writeObject(persistentTasks);
			oos.close();
		} catch(IOException ioe) {
			ioe.printStackTrace(System.err);
		}
	}
	
	private void loadPersistentTask(){
		File ser=bundleContext.getDataFile(TASKS_FILEPATH);
		if(ser.exists()){
			try {
				ObjectInputStream ois=new ObjectInputStream(new FileInputStream(ser));
				persistentTasks=(Map<String, PersistentScheduledRunnable>)ois.readObject();
				ois.close();
			} catch(IOException ioe) {
				ioe.printStackTrace(System.err);
				persistentTasks=new HashMap<String, PersistentScheduledRunnable>();				
			} catch(ClassNotFoundException cnfe) {
				// could append if update
				cnfe.printStackTrace(System.err);
				persistentTasks=new HashMap<String, PersistentScheduledRunnable>();				
			}
		} else {
			persistentTasks=new HashMap<String, PersistentScheduledRunnable>();
		}
    	scheduler=new Scheduler();
    	Iterator<PersistentScheduledRunnable> it=persistentTasks.values().iterator();
    	while(it.hasNext()) {
    		PersistentScheduledRunnable persistentScheduledRunnable=it.next();
    		scheduler.schedule(persistentScheduledRunnable.getPattern(), persistentScheduledRunnable);
    	}   	
	}
    
    // whiteboard pattern methods
    
    public void onArrival(ServiceReference ref) {
        // do something
    	System.out.println("onArrival");
    	Runnable runnable=(Runnable)bundleContext.getService(ref);
    	if(runnable!=null) {
    		try {
    			String servicePid=(String)ref.getProperty(Constants.SERVICE_PID);    			
    			String pattern=(String)ref.getProperty(it.sauronsoftware.cron4j.service.Constants.PATTERN_PROPERTYNAME);
    			PersistentScheduledRunnable persistentScheduledRunnable=new PersistentScheduledRunnable(servicePid, runnable, pattern);
    			String id=scheduler.schedule(pattern, persistentScheduledRunnable);
    			persistentScheduledRunnable.setTaskId(id);
	        	persistentTasks.put((String)ref.getProperty(Constants.SERVICE_PID), persistentScheduledRunnable);
    			savePersistentTask();
    		} catch(InvalidPatternException e) {
    	    	System.err.println(e);
    	    	bundleContext.ungetService(ref);
    		}
    	}
    }
    
    public void onDeparture(ServiceReference ref) {
        // do something
    	System.out.println("onDeparture");
    	PersistentScheduledRunnable persistentScheduledRunnable=(PersistentScheduledRunnable)persistentTasks.get(ref.getProperty(Constants.SERVICE_PID));
    	if(persistentScheduledRunnable!=null) {
    		persistentScheduledRunnable.setRunnable(null);
			savePersistentTask();
    	}
    }
    
    public void onModification(ServiceReference ref) {
        // do something
    	System.out.println("onModification");
    	String servicePid=(String)ref.getProperty(Constants.SERVICE_PID);
    	PersistentScheduledRunnable persistentScheduledRunnable=persistentTasks.get(servicePid);
		try {
			String pattern=(String)ref.getProperty(it.sauronsoftware.cron4j.service.Constants.PATTERN_PROPERTYNAME);
    		if(persistentScheduledRunnable!=null) {
    			scheduler.reschedule(persistentScheduledRunnable.getTaskId(),pattern);
    			persistentScheduledRunnable.setPattern(pattern);
    			savePersistentTask();
    		}
		} catch(InvalidPatternException e) {
	    	System.err.println(e);
			scheduler.deschedule(servicePid);
	    	persistentTasks.remove((String)ref.getProperty(Constants.SERVICE_PID));
	    	bundleContext.ungetService(ref);
		}
    }
    
    class PersistentScheduledRunnable implements Runnable, Serializable {
    	
    	private transient String taskId;
		private String servicePid;
		private String pattern;
		private transient Runnable runnable;

    	public PersistentScheduledRunnable() { }
    	
    	public PersistentScheduledRunnable(String servicePid, Runnable runnable, String pattern) {
    		this.runnable=runnable;
    		this.servicePid=servicePid;
    		this.pattern=pattern;
    	}

    	public void setRunnable(Runnable runnable){
    		this.runnable=runnable;    		
    	}

    	public String getTaskId() {
			return taskId;
		}

		public void setTaskId(String taskId) {
			this.taskId = taskId;
		}
		
    	public String getPattern() {
			return pattern;
		}

		public void setPattern(String pattern) {
			this.pattern = pattern;
		}
    	
    	public void run() {
    		if(runnable==null) {
    			if(logService!=null) logService.log(LogService.LOG_WARNING, "No runnable with service.pid="+servicePid+" for taskId="+taskId);
    		} else {
    			if(logService!=null) logService.log(LogService.LOG_INFO, "Start to run service.pid="+servicePid+" for taskId="+taskId);
    			long startTime=System.currentTimeMillis();
    			runnable.run();
    			long duration=System.currentTimeMillis()-startTime;
    			if(logService!=null) logService.log(LogService.LOG_INFO, "Running completed for service.pid="+servicePid+" for taskId="+taskId+ "in " + duration + "millisceconds");
    		}
    	}
    }
}


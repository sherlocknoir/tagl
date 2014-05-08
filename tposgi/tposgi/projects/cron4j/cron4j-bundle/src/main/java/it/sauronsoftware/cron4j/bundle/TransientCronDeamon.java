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

import java.util.HashMap;
import java.util.Map;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.apache.felix.service.command;
import org.apache.felix.ipojo.whiteboard.Wbp;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.service.log.LogService;

/**
 * This iPOJO component provides a cron deamon for cron4j. It uses the whiteboard pattern handler.
 * @author Didier Donsez
 * @TODO add logging with the LogService
 * @TODO add a command to list the current scheduler tasks
 */
@Component(name = "TransientCronDeamon", architecture = true)
@Wbp(filter="(&(&(objectClass=java.lang.Runnable)(cron4j.pattern=*))(!(service.pid=*)))", 
            onArrival="onArrival", 
            onDeparture="onDeparture",
            onModification="onModification")
public class TransientCronDeamon implements Command
//implement Command // for a Felix shell command "cron4j" listing the active schedules. 
{
	
	private BundleContext bundleContext;

  private Scheduler scheduler;
	
	private Map<Long, String> tasks=new HashMap<Long, String>();
	
	
  @Requires(mandatory=false)
	//@TemporalRequires(mandatory=true)
	private LogService logService
	
	  /**
     * Defines the command scope (cron).
     */
    @ServiceProperty(name = "osgi.command.scope", value = "cron")
    String m_scope;
    
    /**
     * Defines the functions (commands). 
     */
    @ServiceProperty(name = "osgi.command.function", value = "{}")
    String[] m_function = new String[] {
        "list"
    };

	//private LogService logService;
	
	
	public TransientCronDeamon(BundleContext bundleContext) {
		this.bundleContext=bundleContext;
	}
	
    @Validate
    public void starting() {
    scheduler=new Scheduler();
		scheduler.start();
    }

    @Invalidate
    public void stopping() {
		scheduler.stop();
		scheduler=null;
    }
        
    // whiteboard pattern methods
    
    public void onArrival(ServiceReference ref) {
    	System.out.println("onArrival");
    	Runnable runnable=(Runnable)bundleContext.getService(ref);
    	String pattern=(String)ref.getProperty(it.sauronsoftware.cron4j.service.Constants.PATTERN_PROPERTYNAME);
    	Long serviceId=(Long)ref.getProperty(Constants.SERVICE_ID);
    	if(runnable!=null) {
    		// To be completed
    	}
    }
    
    public void onDeparture(ServiceReference ref) {
    	System.out.println("onDeparture");
    	Long serviceId=(Long)ref.getProperty(Constants.SERVICE_ID);
    	String taskId=tasks.remove(serviceId);
    	if(taskId!=null) {
    		// To be completed
    		bundleContext.ungetService(ref);
    	}
    }
    
    public void onModification(ServiceReference ref) {
    	System.out.println("onModification");
    	String pattern=(String)ref.getProperty(it.sauronsoftware.cron4j.service.Constants.PATTERN_PROPERTYNAME);
    	Long serviceId=(Long)ref.getProperty(Constants.SERVICE_ID);
    	String taskId=tasks.get(serviceId);
		try {
    		// To be completed
		} catch(InvalidPatternException e) {
    		// To be completed
		}
    }    
    
    @Descriptor("Display iPOJO factories")
    public void list() {
      System.out.println("not implemented !");
    }

    
}


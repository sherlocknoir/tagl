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
package it.sauronsoftware.cron4j.bundle.example;

import java.util.Date;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.ServiceProperty;
import org.apache.felix.ipojo.annotations.Validate;

/**
 * Component implementing the Runnable service in order to be scheduled by cron4j deamon bundle.
 * This class used annotations to describe the component type. 
 * @author Didier Donsez
 */
@Component
@Provides
public class ScheduleTask implements Runnable {
    
	private static int staticCpt=0;
	private int instanceCpt=0;
	
	@ServiceProperty(mandatory=true)
	private String message;
		
	@ServiceProperty(name="cron4j.pattern", mandatory=true)
	private String pattern;
	
	public void run() {
	    System.out.println((new Date()).toString()+ " : " + message + "(cpt="+(++instanceCpt)+",staticCpt="+(++staticCpt)+")");
	}
	
    @Validate
    public void starting() {
    	System.out.println("starting "+message);
    }

    @Invalidate
    public void stopping() {
       	System.out.println("stopping "+message);
    }
}

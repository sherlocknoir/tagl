package it.sauronsoftware.cron4j.bundle;


import it.sauronsoftware.cron4j.bundle.example.ScheduleTask;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.ServiceProperty;
import org.apache.felix.ipojo.annotations.Validate;

public class ScheduleTaskRunnable{

	
	public static void main(String[] args){
		ScheduleTask st = new ScheduleTask();
		Schedule sch = new Schedule();
		sched.schedule(new SchedulingPattern("* * * * *"), st);
		sched.start();
		try {
				Thread.sleep(5L * 60L * 1000L);
			} catch (InterruptedException e) {
				;
			}
			// Stops the scheduler.
		sched.stop();
	}
}
package it.sauronsoftware.cron4j;

import it.sauronsoftware.cron4j.RunnableTask;
import it.sauronsoftware.cron4j.Scheduler;
import it.sauronsoftware.cron4j.Task;
import it.sauronsoftware.cron4j.TaskExecutionContext;
import it.sauronsoftware.cron4j.TaskExecutor;

/** 
 * @author Heda 
 * @version 4 mai 2014 22:52:51 
 * 
 */
public class AspectjTask implements Runnable{

	public void run() {
		// TODO Auto-generated method stub
		System.out.println("Je suis dans AspectjTask.run ^^");
	}

}

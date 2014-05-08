package it.sauronsoftware;

import it.sauronsoftware.cron4j.AspectjTask;
import it.sauronsoftware.cron4j.RunnableTask;
import it.sauronsoftware.cron4j.Scheduler;
import it.sauronsoftware.cron4j.SchedulingPattern;
import it.sauronsoftware.cron4j.InvalidPatternException;
import it.sauronsoftware.cron4j.Task;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;


public class AspectjTest {

  @BeforeClass
  public static void testSetup() {
  }

  @AfterClass
  public static void testCleanup() {
    // Teardown for data used by the unit tests
  }

  @Test(expected = InvalidPatternException.class)
  public void testExceptionIsThrown() {
    SchedulingPattern sp = new SchedulingPattern("0 5 * *");
  }

  @Test
  public void testAspectj(){
	  
	  Scheduler sched = new Scheduler();
	  RunnableTask rt = new RunnableTask(new AspectjTask());
	  String taskID = sched.schedule(new SchedulingPattern("* * * * *"), rt);
	  sched.start();
	  try {
			Thread.sleep(5L * 60L * 1000L);
		} catch (InterruptedException e) {
			;
		}
		// Stops the scheduler.
		sched.stop();
	 
  }
 
  @Test
  public void testPattern() {
    String pattern;
    pattern="0 5 * * *|8 10 * * *|22 17 * * *";
    assertTrue(pattern + "is correct", SchedulingPattern.validate(pattern));
    pattern="0 5 * * *";
    assertTrue(pattern + "is correct", SchedulingPattern.validate(pattern));
  
  }
}
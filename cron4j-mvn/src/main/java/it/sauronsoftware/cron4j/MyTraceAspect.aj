package it.sauronsoftware.cron4j;
import Task;
import TaskExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
/**
 * créer une classe aspect MyTraceAspect
 * @author Heda
 * @version 21 mars 2014 14:52:43
 *
 */
public aspect MyTraceAspect{

    public static final Logger LOGGER = LoggerFactory.getLogger(MyTraceAspect.class);
    private Map<Task, AtomicLong> counters = new HashMap<Task, AtomicLong>();
    private ThreadLocal<AtomicLong> local = new ThreadLocal<AtomicLong>();
	//declarer un pointcut
    pointcut run(TaskExecutionContext context) : execution(RunnableTask.execute(TaskExecutionContext context));
	
	//advice before
    before(TaskExecutionContext context) : run() && args(context){
           Task task = context.getTaskExecutor().getTask();

        // Code using the global map
        AtomicLong count = counters.get(task);
        if (count == null) {
            count = new AtomicLong(0);
            counters.put(task, count);
        }
        long n1 = count.incrementAndGet();

        // Code using the thread local
        count = local.get();
        if (count == null) {
            count = new AtomicLong(0);
            local.set(count);
        }
        long n2 = count.incrementAndGet();
        LOGGER.info("Calling {} - execution #{},{} - thread[{}]", task, n1, n2, Thread.currentThread().getId());
    }
	//advice after
    after(TaskExecutionContext context) : run() && args(context){
        Task task = context.getTaskExecutor().getTask();
        // Cannot be null, it was necessarily set in `before`.
        long n1 = counters.get(task).get();
        long n2 = local.get().get();
        LOGGER.info("End of {}#{},{}", task, n1, n2);
    }
}
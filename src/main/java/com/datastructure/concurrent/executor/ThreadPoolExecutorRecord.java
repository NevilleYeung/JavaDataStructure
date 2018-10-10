package com.datastructure.concurrent.executor;

import java.util.HashSet;
import java.util.concurrent.*;

/**
 * ThreadPoolExecutor代码走读记录
 *
 * JDK版本：1.8.0_172
 *
 */
public class ThreadPoolExecutorRecord
{
    public static void main(String[] args)
    {
        System.out.println("begin");

        /*
        如果把线程池比作一个公司。公司会有正式员工处理正常业务，如果工作量大的话，会雇佣外包人员来工作。
        闲时就可以释放外包人员以减少公司管理开销。一个公司因为成本关系，雇佣的人员始终是有最大数。
        如果这时候还有任务处理不过来，就走需求池排任务。
        ——来自网络。

        线程池ThreadPoolExecutor类有4个构造函数，分别有5、6、6、7个参数。
        各参数如下：
         */
        // 线程池中核心线程数最大值。可以类比正式员工数量，常驻线程数量。
        int corePoolSize = 1;
        // 线程池中线程总数最大值。即公司最多雇佣员工数量，常驻+临时线程数量。
        int maximumPoolSize = 1;
        // 该线程池中非核心线程闲置超时时长。外包人员等待这么久后，如果还没有活干，就解雇了。
        long keepAliveTime = 0L;
        // keepAliveTime的单位
        TimeUnit unit = TimeUnit.MILLISECONDS;
        // 多余任务的等待队列，维护着等待执行的Runnable对象。任务多到公司员工处理不完，则在这里等待。
        // 常见的workQueue类型：SynchronousQueue、LinkedBlockingQueue、ArrayBlockingQueue、DelayQueue
        BlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<Runnable>();
        // 创建线程的工厂,在这个可以统一处理创建的线程的属性。
        ThreadFactory threadFactory;
        // 线程池拒绝策略。
        // 当任务实在是太多，人也不够，需求池也排满了。若还有任务，则不处理，抛出异常告诉任务提交者。
        RejectedExecutionHandler handler;

        // 新建个单线程池
        new ThreadPoolExecutor(corePoolSize,  // 1
                maximumPoolSize,              // 1
                keepAliveTime,                // 0L
                unit,                         // TimeUnit.MILLISECONDS
                workQueue);                   // LinkedBlockingQueue

        /*
        TODO
        执行execute(Runnable command)方法，添加线程的逻辑：

        public void execute(Runnable command) {
            if (command == null)
                throw new NullPointerException();

            int c = ctl.get();
          1.workerCountOf方法根据ctl的低29位，得到线程池的当前线程数；
            if (workerCountOf(c) < corePoolSize) {
          2.线程数量未达到corePoolSize，则执行addWorker方法，新建一个线程(核心线程)执行任务。
                if (addWorker(command, true))
                1)添加成功，则直接返回。
                    return;
                2)添加失败，则获取线程状态，继续下面的逻辑。
                c = ctl.get();
            }

          3.线程数量达到了corePoolSize;
          判断线程池是否在运行，如果在，执行workQueue.offer尝试插入任务队列。
            if (isRunning(c) && workQueue.offer(command)) {
                int recheck = ctl.get();

          4.插入成功；
          再次验证线程池是否运行;如果不在运行，则移除插入的任务，然后抛出拒绝策略。
                if (! isRunning(recheck) && remove(command))
                    reject(command);
          5.如果线程池在运行；
          且没有线程了，就执行addWorker方法，添加一个非核心线程
                else if (workerCountOf(recheck) == 0)
                    addWorker(null, false);
            }
          6.步骤3的判断不满足；如果添加非核心线程失败，就直接拒绝
            else if (!addWorker(command, false))
                reject(command);
        }

         */

        /*
        常见的线程池有4种。
        可缓存线程池 CachedThreadPool
        定长线程池 FixedThreadPool
        单线程池 SingleThreadPool
         ScheduledThreadPool
         */
        
        // 可缓存线程池
        ExecutorService cached1 = Executors.newCachedThreadPool();
        ExecutorService cached2 = new ThreadPoolExecutor(0, Integer.MAX_VALUE, 60L, TimeUnit.SECONDS, new SynchronousQueue<Runnable>());

        // 单线程池
        ExecutorService single1 = Executors.newSingleThreadExecutor();
        //
//        ExecutorService single2 = new ThreadPoolExecutor(1, 1,
//                0L, TimeUnit.MILLISECONDS,
//                new LinkedBlockingQueue<Runnable>());
    }
}

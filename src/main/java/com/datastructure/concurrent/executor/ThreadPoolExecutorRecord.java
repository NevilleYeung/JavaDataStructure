package com.datastructure.concurrent.executor;

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
        System.out.println("aaaa");

        /*
        线程池ThreadPoolExecutor类有4个构造函数，分别有5、6、6、7个参数。
        各参数如下：
         */
        // 线程池中核心线程数最大值
        int corePoolSize = 1;
        // 线程池中线程总数最大值
        int maximumPoolSize = 1;
        // 该线程池中非核心线程闲置超时时长
        long keepAliveTime = 0L;
        // keepAliveTime的单位
        TimeUnit unit = TimeUnit.MILLISECONDS;
        // 该线程池中的任务队列，维护着等待执行的Runnable对象。
        // 常见的workQueue类型：SynchronousQueue、LinkedBlockingQueue、ArrayBlockingQueue、DelayQueue
        BlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<Runnable>();
        // TODO 创建线程的方式，这是一个接口
        ThreadFactory threadFactory;
        // TODO 抛出异常专用
        RejectedExecutionHandler handler;

        // 新建个单线程池
        new ThreadPoolExecutor(corePoolSize,  // 1
                maximumPoolSize,              // 1
                keepAliveTime,                // 0L
                unit,                         // TimeUnit.MILLISECONDS
                workQueue);                   // LinkedBlockingQueue

        /*
        TODO
        1.线程数量未达到corePoolSize，则新建一个线程(核心线程)执行任务
        2.线程数量达到了corePools，则将任务移入队列等待
        3.队列已满，新建线程(非核心线程)执行任务
        4.队列已满，总线程数又达到了maximumPoolSize，就会由(RejectedExecutionHandler)抛出异常
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

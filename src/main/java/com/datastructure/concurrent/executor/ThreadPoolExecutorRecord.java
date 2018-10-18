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
        TODO
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
        ThreadPoolExecutor类代码分析：
        TODO
         */

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
        TODO
        addWorker(Runnable firstTask, boolean core)方法:

        private boolean addWorker(Runnable firstTask, boolean core) {
        retry:
        for (;;) {
            int c = ctl.get();
            int rs = runStateOf(c);

            1.判断线程池的状态，如果线程池的状态值大于或等SHUTDOWN，则直接返回false。
            // Check if queue empty only if necessary.
            if (rs >= SHUTDOWN &&
                ! (rs == SHUTDOWN &&
                   firstTask == null &&
                   ! workQueue.isEmpty()))
                return false;

            2.自旋，更新创建线程数量。
            通过参数core判断当前需要创建的线程是否为核心线程，如果core为true，且当前线程数小于corePoolSize，则break retry跳出循环，开始创建新的线程。
            for (;;) {
                int wc = workerCountOf(c);
                if (wc >= CAPACITY ||
                    wc >= (core ? corePoolSize : maximumPoolSize))
                    return false;
                if (compareAndIncrementWorkerCount(c))
                    break retry;
                c = ctl.get();  // Re-read ctl
                if (runStateOf(c) != rs)
                    continue retry;
                // else CAS failed due to workerCount change; retry inner loop
            }
        }

        boolean workerStarted = false;
        boolean workerAdded = false;
        Worker w = null;
        try {

        3. 获得线程池主锁
        线程池的工作线程是通过Woker类实现的，通过ReentrantLock锁保证线程安全。
            w = new Worker(firstTask);
            final Thread t = w.thread;
            if (t != null) {
                final ReentrantLock mainLock = this.mainLock;
                mainLock.lock();
                try {
                    // Recheck while holding lock.
                    // Back out on ThreadFactory failure or if
                    // shut down before lock acquired.
                    int rs = runStateOf(ctl.get());

        4.添加线程到workers（HashSet）中。
                    if (rs < SHUTDOWN ||
                        (rs == SHUTDOWN && firstTask == null)) {
                        if (t.isAlive()) // precheck that t is startable
                            throw new IllegalThreadStateException();
                        workers.add(w);
                        int s = workers.size();
                        if (s > largestPoolSize)
                            largestPoolSize = s;
                        workerAdded = true;
                    }
                } finally {
                    mainLock.unlock();
                }

        5.启动新线程
                if (workerAdded) {
                    t.start();
                    workerStarted = true;
                }
            }
        } finally {
            if (! workerStarted)
                addWorkerFailed(w);
        }
        return workerStarted;
    }

         */


        /*
        TODO
        Worker类run方法的实现方法是runWorker(Worker w)，如下：

        final void runWorker(Worker w) {
        Thread wt = Thread.currentThread();
        Runnable task = w.firstTask;
        w.firstTask = null;
        w.unlock(); // allow interrupts
        boolean completedAbruptly = true;
        try {

        1.判断是否是第一次执行任务，或者从队列中可以获取到任务
            while (task != null || (task = getTask()) != null) {
                w.lock();
                // If pool is stopping, ensure thread is interrupted;
                // if not, ensure thread is not interrupted.  This
                // requires a recheck in second case to deal with
                // shutdownNow race while clearing interrupt
                if ((runStateAtLeast(ctl.get(), STOP) ||
                     (Thread.interrupted() &&
                      runStateAtLeast(ctl.get(), STOP))) &&
                    !wt.isInterrupted())
                    wt.interrupt();
                try {
        2.获取到任务后，执行任务开始前操作钩子beforeExecute
        该方法是protected类型，可以继承修改。
                    beforeExecute(wt, task);
                    Throwable thrown = null;
                    try {
        3.执行任务
                        task.run();
                    } catch (RuntimeException x) {
                        thrown = x; throw x;
                    } catch (Error x) {
                        thrown = x; throw x;
                    } catch (Throwable x) {
                        thrown = x; throw new Error(x);
                    } finally {
        4.执行任务后钩子afterExecute，与beforeExecute类似
                        afterExecute(task, thrown);
                    }
                } finally {
                    task = null;
                    w.completedTasks++;
                    w.unlock();
                }
            }
            completedAbruptly = false;
        } finally {
            processWorkerExit(w, completedAbruptly);
        }
    }

         */


        /*
        TODO
        所谓线程池本质是一个hashSet。多余的任务会放在阻塞队列中。
        只有当阻塞队列满了后，才会触发非核心线程的创建。所以非核心线程只是临时过来打杂的。直到空闲了，然后自己关闭了。
        线程池提供了两个钩子（beforeExecute，afterExecute）给我们，我们继承线程池，在执行任务前后做一些事情。

        线程池原理关键技术：锁（lock,cas）、阻塞队列、hashSet（资源池）
         */




        /*
        TODO
        常见的线程池有4种。
        可缓存线程池 CachedThreadPool
        指定工作线程数量的线程池 FixedThreadPool
        单线程池 SingleThreadPool
        定长的线程池ScheduledThreadPool（支持定时及周期性任务执行）
         */


        // TODO 可缓存线程池
        ExecutorService cached1 = Executors.newCachedThreadPool();
        ExecutorService cached2 = new ThreadPoolExecutor(0, Integer.MAX_VALUE, 60L, TimeUnit.SECONDS, new SynchronousQueue<Runnable>());

        /*
        根据前面的分析，从CachedThreadPool传给ThreadPoolExecutor的参数就能判断该线程池的特性。
        1.corePoolSize=0
        若线程空闲就会被清理，不会保留任何空闲线程。
        2.maximumPoolSize=Integer.MAX_VALUE
        最大线程数为int类型最大值，几乎可以认为是线程数无上限。
        3.keepAliveTime=60
        线程空闲60s后就会被回收。
         */


        // TODO 指定工作线程数量的线程池
        int nThreads = 3;
        ExecutorService fixed1 = Executors.newFixedThreadPool(nThreads);
        ExecutorService fixed2 = new ThreadPoolExecutor(nThreads, nThreads, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());

        /*
        FixedThreadPool特性：
        1.corePoolSize=指定数量nThreads
        核心线程数为nThreads，线程池中最多会保留nThreads个线程，且核心线程会一直存活。
        2.maximumPoolSize=指定数量nThreads
        与corePoolSize相同，则不会创建临时线程。
        3.keepAliveTime=0
        无需解释了。
         */

        // TODO 单线程池
        ExecutorService single1 = Executors.newSingleThreadExecutor();
        // 原代码如下，为简化说明，先去掉FinalizableDelegatedExecutorService这层
//        new Executors.FinalizableDelegatedExecutorService(new ThreadPoolExecutor(1, 1,
//                0L, TimeUnit.MILLISECONDS,
//                new LinkedBlockingQueue<Runnable>()));
        ExecutorService single2 = new ThreadPoolExecutor(1, 1,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>());

        /*
        SingleThreadExecutor特性：
        1.corePoolSize=1
        线程池中最多只能有一个线程，且创建后会一直保留。
        2.maximumPoolSize=1
        与corePoolSize相同，则不会创建临时线程。
        3.keepAliveTime=0
        无需解释了。
         */


        // TODO 定长的线程池ScheduledThreadPool（支持定时及周期性任务执行）
        nThreads = 5;
        // ScheduledThreadPoolExecutorl类后续再研究
//        ThreadPoolExecutor(corePoolSize, Integer.MAX_VALUE, 0, NANOSECONDS,
//                new DelayedWorkQueue());
        ScheduledExecutorService scheduledThreadPool = Executors.newScheduledThreadPool(nThreads);

        /*
        ScheduledThreadPool特性：
        1.corePoolSize=指定数量nThreads
        可自由指定核心线程数。
        2.maximumPoolSize=Integer.MAX_VALUE
        与CachedThreadPool类似，可无上限地创建临时线程。
         */

    }
}

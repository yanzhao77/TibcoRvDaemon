package com.chot.utils;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 线程池
 */
public class CustomThreadPoolExecutor {
    private ThreadPoolExecutor pool = null;
    long total;//已用内存

    /**
     * 线程池初始化方法
     * <p>
     * corePoolSize 核心线程池大小----3
     * maximumPoolSize 最大线程池大小----5
     * keepAliveTime 线程池中超过corePoolSize数目的空闲线程最大存活时间----30+单位TimeUnit
     * TimeUnit keepAliveTime时间单位----TimeUnit.MINUTES
     * workQueue 阻塞队列----new ArrayBlockingQueue<Runnable>(10000)====10000容量的阻塞队列
     * threadFactory 新建线程工厂----new CustomThreadFactory()====定制的线程工厂
     * rejectedExecutionHandler 当提交任务数超过maxmumPoolSize+workQueue之和时,
     * 当提交第一万个任务时(前面线程都没有执行完,此测试方法中用sleep(100)),
     * 任务会交给RejectedExecutionHandler来处理
     */

    public void init() {
        pool = new ThreadPoolExecutor(3, 5, 30,
                TimeUnit.MINUTES, new ArrayBlockingQueue<Runnable>(10000), new CustomThreadFactory(), new CustomRejectedExecutionHandler());

        Thread daemonThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    printlnThreadValue();
                }
            }
        });
        daemonThread.setDaemon(true);
        daemonThread.start();
    }

    /**
     * 输出打印线程信息 和AP信息
     */
    public void printlnThreadValue() {
        Runtime run = Runtime.getRuntime();
        long max = run.maxMemory();//最大内存
        long free = run.freeMemory();//已分配内存中的剩余空间
        total = run.totalMemory();//已用内存

        System.out.println("可用内存 = " + XStreamUtil.getNetFileSizeDescription(max - total + free));
        System.out.println("项目已用内存 = " + XStreamUtil.getNetFileSizeDescription(total));

        System.out.println("最大线程数：" + pool.getMaximumPoolSize());
        System.out.println("核心线程数：" + pool.getCorePoolSize());
        System.out.println("当前执行线程数：" + pool.getActiveCount());
        System.out.println("剩余线程数：" + (pool.getMaximumPoolSize() - pool.getActiveCount()));
        System.out.println();
    }

    /**
     * 销毁线程池
     */
    public void destory() {
        if (pool != null) {
            pool.shutdownNow();
        }
    }


    public ExecutorService getExecutor() {
        return this.pool;
    }

    /**
     * 执行线程
     *
     * @param runnable
     */
    public void execute(Runnable runnable) {
        if (total < 314572800) {//当内存超过300M时，不得再起新的线程
            pool.execute(runnable);
        } else {
            System.out.println("内存已超出300M！禁止再添加任务");
        }
    }


    private class CustomRejectedExecutionHandler implements RejectedExecutionHandler {
        @Override
        public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
            //记录异常
            System.out.println("error...................");
        }
    }

    private class CustomThreadFactory implements ThreadFactory {

        private AtomicInteger count = new AtomicInteger(0);

        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r);
//            String threadName = "Thread" + count.addAndGet(1);
//            t.setName(threadName);
            return t;
        }
    }

    public static void main(String[] args) {
        CustomThreadPoolExecutor exec = new CustomThreadPoolExecutor();
        //1. 初始化
        exec.init();
        ExecutorService pool = exec.getExecutor();

        for (int i = 1; i < 100; i++) {
            System.out.println("提交第" + i + "个任务");
            int finalI = i;
            pool.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        System.out.println(">>>task is running========" + finalI);
                        Thread.sleep(1000);
                        boolean terminated = pool.isTerminated();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            });
        }

    }
}
 
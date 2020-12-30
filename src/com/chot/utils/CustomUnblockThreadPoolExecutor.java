package com.chot.utils;

import org.apache.log4j.Logger;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Author:Zach
 * @Description: 阻塞线程池
 * @Date:Created in 15:26 2018/8/14
 * @Modified By:
 */
public class CustomUnblockThreadPoolExecutor {
    private ThreadPoolExecutor pool = null;
    Logger logger;

    public CustomUnblockThreadPoolExecutor() {
        logger = LoggerUtil.getLogger();
    }

    /**
     * 线程池初始化方法
     * <p>
     * corePoolSize 核心线程池大小----1
     * maximumPoolSize 最大线程池大小----3
     * keepAliveTime 线程池中超过corePoolSize数目的空闲线程最大存活时间----30+单位TimeUnit
     * TimeUnit keepAliveTime时间单位----TimeUnit.MINUTES
     * workQueue 阻塞队列----new ArrayBlockingQueue<Runnable>(5)==== 5容量的阻塞队列
     * threadFactory 新建线程工厂----new CustomThreadFactory()====定制的线程工厂
     * rejectedExecutionHandler 当提交任务数超过maxmumPoolSize+workQueue之和时,
     * 即当提交第9个任务时(前面线程都没有执行完,此测试方法中用sleep(100)),
     * 任务会交给RejectedExecutionHandler来处理
     */

    public void init() {

        pool = new ThreadPoolExecutor(1, 3, 30,
                TimeUnit.MINUTES, new ArrayBlockingQueue<Runnable>(5), new CustomThreadFactory(), new CustomRejectedExecutionHandler());
        logger.debug("start  ThreadPoolExecutor");
    }

    public void destory() {
        if (pool != null) {
            pool.shutdownNow();
        }
    }

    public ExecutorService getCustomThreadPoolExecutor() {
        return this.pool;
    }


    private class CustomRejectedExecutionHandler implements RejectedExecutionHandler {
        @Override
        public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
            //核心改造点,由blockingqueue的offer改成put阻塞方法
            try {
                executor.getQueue().put(r);
            } catch (InterruptedException e) {
                e.printStackTrace();
                logger.error(e.getLocalizedMessage(), e.getCause());
            }
        }
    }

    private class CustomThreadFactory implements ThreadFactory {

        private AtomicInteger count = new AtomicInteger(0);

        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r);
            String threadName = CustomThreadPoolExecutor.class.getSimpleName() + count.addAndGet(1);
            t.setName(threadName);
            return t;
        }
    }

    public static void main(String[] args) {
        CustomUnblockThreadPoolExecutor exec = new CustomUnblockThreadPoolExecutor();

        //1. 初始化
        exec.init();

        ExecutorService pool = exec.getCustomThreadPoolExecutor();

        for (int i = 1; i < 100; i++) {
            System.out.println("提交第" + i + "个任务");
            pool.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        System.out.println(">>>task is running========");
                        TimeUnit.SECONDS.sleep(10);
                        boolean terminated = pool.isTerminated();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            });
        }

    }
}
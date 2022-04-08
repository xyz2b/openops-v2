package com.openops.cocurrent;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * 不带回调的任务执行器，普通任务执行器
 * */
public class FutureTaskScheduler {
    static ThreadPoolExecutor mixPool = null;

    static {
        mixPool = ThreadPoolFactory.getMixedTargetThreadPool();
    }

    private FutureTaskScheduler()
    {

    }

    /**
     * 添加任务
     *
     * @param executeTask 普通任务
     */
    public static void add(Runnable executeTask) {
        mixPool.submit(()->{ executeTask.run(); });
    }
}

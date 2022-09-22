package com.example.demo;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;

/**
 * <pre>
 * 程序目的：演示 AtomicInteger、AtomicLong 在高并发下性能不好
 * 在16个线程下使用AtomicLong。
 * 每次值发生变化时，都会刷新回主内存，竞争激烈时，这样的 flush 和 refresh 操作耗费了很多资源，而且 CAS 也会经常失败
 * </pre>
 * created at 2020/8/11 06:11
 * @author lerry
 */
public class AtomicLongDemo {
    /**
     * 线程池内线程数
     */
    final static int POOL_SIZE = 1000;

    public static void main(String[] args) throws InterruptedException {
        long start = System.currentTimeMillis();

        AtomicLong counter = new AtomicLong(0);
        ExecutorService service = Executors.newFixedThreadPool(POOL_SIZE);

        ArrayList<Future> futures = new ArrayList<>(POOL_SIZE);
        for (int i = 0; i < POOL_SIZE * 1000; i++) {
            futures.add(service.submit(new Task(counter)));
        }

        // 等待所有线程执行完
        for (Future future : futures) {
            try {
                future.get();
            }
            catch (ExecutionException e) {
                e.printStackTrace();
            }
        }

        NumberFormat numberFormat = NumberFormat.getInstance();
        System.out.printf("统计结果为：[%s]\n", numberFormat.format(counter.get()));
        System.out.printf("耗时：[%d]毫秒", (System.currentTimeMillis() - start));
        // 关闭线程池
        service.shutdown();
    }

    /**
     * 有一个 AtomicLong 成员变量，每次执行N次+1操作
     */
    static class Task implements Runnable {

        private final AtomicLong counter;

        public Task(AtomicLong counter) {
            this.counter = counter;
        }

        /**
         * 每个线程执行N次+1操作
         */
        @Override
        public void run() {
            for (int i = 0; i < 100; i++) {
                counter.get();
                counter.addAndGet(1);
            }
        }// end run
    }// end class
}


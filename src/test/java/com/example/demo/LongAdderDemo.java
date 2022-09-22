package com.example.demo;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.LongAdder;

/**
 * <pre>
 * 程序目的：和 AtomicLong 进行性能对比
 * </pre>
 * created at 2020/8/11 06:25
 * @author lerry
 */
public class LongAdderDemo {
    /**
     * 线程池内线程数
     */
    final static int POOL_SIZE = 1000;

    public static void main(String[] args) throws InterruptedException {
        long start = System.currentTimeMillis();

        LongAdder counter = new LongAdder();
        ExecutorService service = Executors.newFixedThreadPool(POOL_SIZE);

        ArrayList<Future> futures = new ArrayList<>(POOL_SIZE);
        for (int i = 0; i < POOL_SIZE * 1000; i++) {
            futures.add(service.submit(new LongAdderDemo.Task(counter)));
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
        System.out.printf("统计结果为：[%s]\n", numberFormat.format(counter.sum()));
        System.out.printf("耗时：[%d]毫秒", (System.currentTimeMillis() - start));
        // 关闭线程池
        service.shutdown();
    }

    /**
     * 有一个 LongAdder 成员变量，每次执行N次+1操作
     */
    static class Task implements Runnable {

        private final LongAdder counter;

        public Task(LongAdder counter) {
            this.counter = counter;
        }

        /**
         * 每个线程执行N次+1操作
         */
        @Override
        public void run() {
            for (int i = 0; i < 100; i++) {
                counter.sum();
                counter.add(1);
                counter.sum();
            }
        }// end run
    }// end class
}


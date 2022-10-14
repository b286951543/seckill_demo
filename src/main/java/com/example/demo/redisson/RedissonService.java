package com.example.demo.redisson;

import org.redisson.api.RBlockingQueue;
import org.redisson.api.RDelayedQueue;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class RedissonService {

    @Autowired
    private RedissonClient redissonClient;

    public void addDelay() {
        RBlockingQueue<CallDTO> blockingFairQueue = redissonClient.getBlockingQueue("delay_queue_call");
        RDelayedQueue<CallDTO> delayedQueue = redissonClient.getDelayedQueue(blockingFairQueue);
        delayedQueue.offer(new CallDTO(), 5, TimeUnit.SECONDS);
        // 不要调用下面的方法,否者会导致消费不及时
//        delayedQueue.destroy();
    }

}


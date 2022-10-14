package com.example.demo.redisson;

import com.alibaba.fastjson2.JSON;
import lombok.extern.log4j.Log4j2;
import org.redisson.api.RBlockingQueue;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * spring-boot项目启动完成运行，监听 redis 的 延迟队列
 */
@Log4j2
@Component
public class AppStartupRunner implements CommandLineRunner {

    @Autowired
    private RedissonClient redissonClient;

    @Override
    public void run(String... args) {
        new Thread(()->{
            RBlockingQueue<CallDTO> blockingFairQueue = redissonClient.getBlockingQueue("delay_queue_call");
            // 开启客户端监听（必须调用），否者系统重启时拿不到已过期数据，要等到系统第一次调用getDelayedQueue方法时才能开启监听
            redissonClient.getDelayedQueue(blockingFairQueue);
            while (true){
                CallDTO dto = null;
                try {
                    dto = blockingFairQueue.take();
                } catch (Exception e) {
                    continue;
                }
                if (Objects.isNull(dto)) {
                    continue;
                }
                System.out.println(String.format("receive1=======dto:%s", JSON.toJSONString(dto)));
            }
        }).start();
    }

}


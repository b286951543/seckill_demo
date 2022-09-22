package com.example.demo.controller;

import com.example.demo.model.ProductInventory;
import com.example.demo.util.ProductConstant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.io.*;
import java.util.Arrays;
import java.util.Set;

@RestController
@RequestMapping("/")
public class SecKillController {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final String redisScript =   " local result = redis.call('HMGET', KEYS[1], 'total_num', 'sole_num') " +
                                    " if tonumber(result[1]) > tonumber(result[2]) then " +
                                    "   return redis.call('HINCRBY', KEYS[1], 'sole_num', 1) " +
                                    " end " +
                                    " return 0 ";

    private OutputStream output = new FileOutputStream("/Users/jiangzx/github-workspace/seckill_demo/aaaa/test.txt");

    @Resource(name = "redisTemplate1")
    RedisTemplate redisTemplate1;

    public SecKillController() throws IOException {
    }

    // http://localhost:10013/addOrder/1
    @GetMapping("/addOrder/{productId}")
    public String addOrder(@PathVariable String productId) throws IOException {
        if (delLocalInventory(productId) && delRedisInventory(productId)){
            // 异步发送 创建订单/扣除库存 的消息
            // 过期订单处理（需要恢复本地库存与redis库存）
            sendLog();
            return "success";
        }else {
            sendLog();
            return "false";
        }
    }

    private void sendLog() throws IOException {
        Set<String> keySet = ProductConstant.productInventory.keySet();
        for (String key: keySet){
            ProductInventory productInventory = ProductConstant.productInventory.get(key);

            if ("1".equals(key)){
                String msg = "商品id：" + key + "。库存：" + productInventory.getInventory() + "。销售量：" + productInventory.getSaleNum() + "\n";
                byte data[] = msg.getBytes();
                output.write(data);
            }
//            logger.info("----------------------------");
//            logger.info("商品id " + key);
//            logger.info("库存" + productInventory.getInventory());
//            logger.info("销售量" + productInventory.getSaleNum());
//            logger.info("----------------------------");
        }
    }

    // 扣除本地库存
    private boolean delLocalInventory(String productId){
        ProductInventory productInventory = ProductConstant.productInventory.get(productId);
        if (productInventory.getInventory() > productInventory.getSaleNum().get()){
            long newSaleNum = productInventory.getSaleNum().addAndGet(1);
            return productInventory.getInventory() >= newSaleNum;
        }else {
            return false;
        }
    }

    // 扣除redis 库存
    private boolean delRedisInventory(String productId){
        long result = (long)redisTemplate1.execute(
                new DefaultRedisScript<>(redisScript, Long.class),
                Arrays.asList(productId));

//        DefaultRedisScript<Long> script = new DefaultRedisScript<>(stringBuffer.toString(), Long.class);
//        String[] strings = {"total_num", "sole_num"};
//        Long result = RedisUtil.execute(redisTemplate1, script, Arrays.asList(productId + ""), strings);
        return result != 0;
    }
}

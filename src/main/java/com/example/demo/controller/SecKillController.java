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
            // todo 异步发送 kafka 创建订单/扣除库存 的消息
            // todo 过期订单处理（需要恢复本地库存，redis库存，数据库库存），可参考 《订单过期逻辑.md》
            // todo 本地库存通过 kafka 加，redis库存直接加，数据库库存也是直接加，例如：update aa_test set value = value + #{count} where value >= #{count}
            // todo 库存修改，需要计算出每台服务器应新增的库存（假设原本库存为10，现在改为了20，总库存新增了10，有2台服务器，那么每台服务器新增5库存+2buff库存），然后通过kafka发到每台服务器上
            // todo 要注意数据库与redis 的数据一致性。当把数据库的销售量数据同步到redis时，需要等kafka里的消息消费完了才能同步！！
            // todo 同步步骤：1.暂停下单功能，2.等kafka里的消息消费完，3.把数据库的销售量数据同步到redis
            // todo 数据库也分两个字段：商品总数和销售量，库存通过两者相减得出
            // todo 商品总数可以实时同步，因为只有后台会修改，但是销售量必须按照商品的方法同步，否则会数据不一致，导致超卖
            // todo redis 可使用单机或集群（多主零从的方式）来部署。但不要使用主从的方式部署。因为如果有从节点，主节点把命令同步到从节点时，会有延迟。
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
            // 还有库存则预售量+1
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

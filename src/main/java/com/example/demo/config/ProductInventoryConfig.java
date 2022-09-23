package com.example.demo.config;

import com.example.demo.mock.ProductMock;
import com.example.demo.model.Product;
import com.example.demo.model.ProductInventory;
import com.example.demo.util.ProductConstant;
import com.example.demo.util.RedisUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 商品库存初始化
 */
@Configuration
public class ProductInventoryConfig {

    @Resource(name = "redisTemplate1")
    private RedisTemplate redisTemplate1;

    @Value("${server.num}")
    private int serviceNum;

    @Value("${product.buffer-inventory.proportion}")
    private double bufferProportion;

    // 初始化库存
    public void intInventory(){
        // 查询所有的商品id及库存
        List<Product> list = ProductMock.getAllProduct();
        for (Product product: list){
            // 商品总库存
            Long inventory = product.getInventory();
            String id = product.getId();

            long localInventory = inventory/serviceNum; // 该服务器的本地库存
            long newLocalInventory = (long) (localInventory*bufferProportion); // 该服务器的本地库存2(加上了buffer库存)
            AtomicLong saleNum = new AtomicLong(0); // 该服务器的销售数量
            ProductInventory bean = ProductInventory.builder().inventory(newLocalInventory).saleNum(saleNum).build();
            ProductConstant.productInventory.put(id, bean);

            // 设置商品总库存与销售量
            HashMap<String, Object> map = new HashMap<>();
            map.put("total_num", inventory);
            map.put("sole_num", 0);
            RedisUtil.HSet(redisTemplate1, id, map);
        }
    }
}

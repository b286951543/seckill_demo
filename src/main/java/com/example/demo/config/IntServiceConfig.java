package com.example.demo.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

/**
 * 项目启动时执行的初始化任务
 */
@Configuration
public class IntServiceConfig {
    @Autowired
    private ProductInventoryConfig productInventoryConfig;

    @PostConstruct
    private void run(){
        // 项目启动时的初始化设置
        productInventoryConfig.intInventory();
    }
}

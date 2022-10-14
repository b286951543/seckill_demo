package com.example.demo.controller;

import com.example.demo.redisson.RedissonService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/redisson")
public class RedissonController {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private RedissonService redissonService;

    // http://localhost:10013/redisson/addDelay
    @GetMapping("/addDelay")
    public String addDelay() throws IOException {
        redissonService.addDelay();
        return "success";
    }

}

package com.pingo.yuapi;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
@MapperScan("com.pingo")
public class YuApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(YuApiApplication.class, args);
    }

}
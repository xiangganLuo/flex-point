package com.flexpoint.example.springboot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Spring Boot示例应用启动类
 * 
 * 演示Spring Boot环境下的Flex Point用法：
 * 1. 自动扫描和注册扩展点
 * 2. 自动注册选择器
 * 3. Web API接口
 *
 * @author xiangganluo
 * @version 1.0.0
 */
@SpringBootApplication
public class SpringBootExampleApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(SpringBootExampleApplication.class, args);
        System.out.println("Spring Boot示例应用启动成功！");
        System.out.println("访问地址: http://localhost:8080");
        System.out.println("测试接口: http://localhost:8080/api/v1/orders/process");
    }

}
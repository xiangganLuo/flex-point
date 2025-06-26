package com.flexpoint.example.springboot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.CommandLineRunner;
import org.springframework.beans.factory.annotation.Autowired;

@SpringBootApplication
public class ExampleApplication implements CommandLineRunner {
    @Autowired
    private DemoService demoService;

    public static void main(String[] args) {
        SpringApplication.run(ExampleApplication.class, args);
    }

    @Override
    public void run(String... args) {
        demoService.checkPermission();
    }
} 
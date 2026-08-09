package com.smartshop.erp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SmartshopErpApplication {
    public static void main(String[] args) {
        SpringApplication.run(SmartshopErpApplication.class, args);
    }
}

package com.carshop;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CarShopApplication {

    public static void main(String[] args) {
        SpringApplication.run(CarShopApplication.class, args);
    }
}

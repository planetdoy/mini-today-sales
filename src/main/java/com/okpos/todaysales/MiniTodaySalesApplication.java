package com.okpos.todaysales;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class MiniTodaySalesApplication {

    public static void main(String[] args) {
        SpringApplication.run(MiniTodaySalesApplication.class, args);
    }

}
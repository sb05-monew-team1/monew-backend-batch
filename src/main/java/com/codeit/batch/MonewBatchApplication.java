package com.codeit.batch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MonewBatchApplication {

    public static void main(String[] args) {
        SpringApplication.run(MonewBatchApplication.class, args);
    }

}

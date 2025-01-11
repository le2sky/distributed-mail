package com.aengdulab.distributedmail;

import java.util.concurrent.CountDownLatch;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class DistributedMailApplication {

    public static void main(String[] args) {
        SpringApplication.run(DistributedMailApplication.class, args);
    }

    @Bean
    public GlobalLatch globalLatch() {
        CountDownLatch latch = new CountDownLatch(1);

        return new GlobalLatch(latch);
    }
}

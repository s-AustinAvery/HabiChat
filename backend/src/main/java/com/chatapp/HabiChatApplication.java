package com.chatapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class HabiChatApplication {
    public static void main(String[] args) {
        SpringApplication.run(HabiChatApplication.class, args);
    }
}

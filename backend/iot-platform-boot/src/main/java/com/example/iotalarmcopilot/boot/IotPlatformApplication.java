package com.example.iotalarmcopilot.boot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.example.iotalarmcopilot")
public class IotPlatformApplication {

    public static void main(String[] args) {
        SpringApplication.run(IotPlatformApplication.class, args);
    }
}

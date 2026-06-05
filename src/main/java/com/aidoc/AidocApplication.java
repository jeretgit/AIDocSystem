package com.aidoc;

import com.aidoc.config.FileStorageProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
@EnableConfigurationProperties(FileStorageProperties.class)
public class AidocApplication {

    public static void main(String[] args) {
        SpringApplication.run(AidocApplication.class, args);
    }
}

package com.b1;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class B1Application {

    public static void main(String[] args) {
        SpringApplication.run(B1Application.class, args);
    }
}

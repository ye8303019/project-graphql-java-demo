package com.chris.graphql;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(
        scanBasePackages = {"com.chris.graphql"}
)
public class ApplicationDemo {

    public static void main(String[] args) throws Exception {
        SpringApplication.run(ApplicationDemo.class, args);
    }
}

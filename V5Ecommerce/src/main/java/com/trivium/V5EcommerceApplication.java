package com.trivium;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@SpringBootApplication
@Configuration
@EnableAutoConfiguration
@ComponentScan(basePackages = "com.trivium")

public class V5EcommerceApplication {

	public static void main(String[] args) {
		SpringApplication.run(V5EcommerceApplication.class, args);
		System.out.println("started");
	}

}

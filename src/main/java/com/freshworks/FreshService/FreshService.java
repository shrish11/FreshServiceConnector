package com.freshworks.FreshService;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = { "com.netflix.conductor", "com.freshworks" })
public class FreshService {

	public static void main(String[] args) {
		SpringApplication.run(FreshService.class, args);
	}

}

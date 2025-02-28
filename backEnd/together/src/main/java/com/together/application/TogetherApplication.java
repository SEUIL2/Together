package com.together.application;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@ComponentScan(basePackages = "com.together")
@EntityScan(basePackages = "com.together")
@EnableJpaRepositories(basePackages = "com.together")
@SpringBootApplication
public class TogetherApplication {

	public static void main(String[] args) {
		SpringApplication.run(TogetherApplication.class, args);
	}

}

package com.acra;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class AcraApplication {

	public static void main(String[] args) {
		SpringApplication.run(AcraApplication.class, args);
	}

}

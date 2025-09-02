package com.example.clubreview;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan("com.example.clubreview")
public class ClubreviewApplication {

	public static void main(String[] args) {
		SpringApplication.run(ClubreviewApplication.class, args);
	}

}

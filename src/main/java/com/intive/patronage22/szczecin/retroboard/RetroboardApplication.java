package com.intive.patronage22.szczecin.retroboard;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class })
public class RetroboardApplication {

	public static void main(String[] args) {
		SpringApplication.run(RetroboardApplication.class, args);
	}

}

package com.touralert;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@org.springframework.web.bind.annotation.CrossOrigin // Ensure CORS settings remain intact
@org.springframework.scheduling.annotation.EnableScheduling

@org.springframework.data.jpa.repository.config.EnableJpaAuditing
@SpringBootApplication
public class TouralertApplication {

	public static void main(String[] args) {
		SpringApplication.run(TouralertApplication.class, args);
	}

}
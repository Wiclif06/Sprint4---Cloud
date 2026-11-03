package br.com.fiap.petjourney;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@ConfigurationPropertiesScan
@EnableCaching
@EnableAsync
public class PetJourneyApplication {

	public static void main(String[] args) {
		SpringApplication.run(PetJourneyApplication.class, args);
	}

}


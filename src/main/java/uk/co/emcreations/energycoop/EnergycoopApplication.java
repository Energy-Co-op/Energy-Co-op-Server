package uk.co.emcreations.energycoop;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients
@SpringBootApplication
public class EnergycoopApplication {

	static void main(String[] args) {
		SpringApplication.run(EnergycoopApplication.class, args);
	}

}

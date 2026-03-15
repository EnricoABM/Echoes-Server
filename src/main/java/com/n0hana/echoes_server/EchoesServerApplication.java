package com.n0hana.echoes_server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;

@SpringBootApplication
public class EchoesServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(EchoesServerApplication.class, args);
	}

}

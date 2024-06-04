package com.bytes;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@EntityScan("com.bytes.model")
@SpringBootApplication
public class BytesApplication {

	public static void main(String[] args) {
		SpringApplication.run(BytesApplication.class, args);
	}
}

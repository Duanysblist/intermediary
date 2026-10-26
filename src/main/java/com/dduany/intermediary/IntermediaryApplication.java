package com.dduany.intermediary;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.time.Clock;

@SpringBootApplication
public class IntermediaryApplication {

	public static void main(String[] args) {
		SpringApplication.run(IntermediaryApplication.class, args);
	}

	/** Injected wherever "now" matters, so tests can substitute a fixed clock. */
	@Bean
	Clock clock() {
		return Clock.systemUTC();
	}

}

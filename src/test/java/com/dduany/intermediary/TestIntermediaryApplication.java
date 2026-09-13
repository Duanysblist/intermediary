package com.dduany.intermediary;

import org.springframework.boot.SpringApplication;

/**
 * Dev launcher: runs the real application against a fresh Testcontainers
 * PostgreSQL instance. Use the "IntermediaryApplication (testcontainers)"
 * run configuration in IntelliJ. The database is discarded on shutdown.
 */
public class TestIntermediaryApplication {

	public static void main(String[] args) {
		SpringApplication.from(IntermediaryApplication::main)
				.with(TestcontainersConfiguration.class)
				.run(args);
	}

}

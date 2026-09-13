package com.dduany.intermediary;

import org.springframework.boot.SpringApplication;

/**
 * Dev launcher: runs the real application against a fresh Testcontainers
 * PostgreSQL instance. Use the "IntermediaryApplication (testcontainers)"
 * run configuration in IntelliJ. The database is discarded on shutdown.
 */
public class TestIntermediaryApplication {

	public static void main(String[] args) {
		// Predictable dev login for the IDE launcher; production reads these from the environment.
		System.setProperty("app.auth.username", System.getProperty("app.auth.username", "dev"));
		System.setProperty("app.auth.password", System.getProperty("app.auth.password", "dev"));
		SpringApplication.from(IntermediaryApplication::main)
				.with(TestcontainersConfiguration.class)
				.run(args);
	}

}

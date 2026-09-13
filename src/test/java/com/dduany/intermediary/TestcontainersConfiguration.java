package com.dduany.intermediary;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * Starts a throwaway PostgreSQL container for tests (and for the
 * {@link TestIntermediaryApplication} dev launcher). Requires Docker to be running.
 *
 * {@code @ServiceConnection} wires the container's JDBC URL, username and password
 * into Spring's DataSource automatically, so no datasource properties are needed.
 */
@TestConfiguration(proxyBeanMethods = false)
class TestcontainersConfiguration {

	@Bean
	@ServiceConnection
	PostgreSQLContainer<?> postgresContainer() {
		// Same major version as docker-compose.yml and production.
		return new PostgreSQLContainer<>(DockerImageName.parse("postgres:17.9"));
	}

}

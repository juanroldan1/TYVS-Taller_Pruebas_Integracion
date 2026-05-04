// src/main/java/edu/unisabana/tyvs/registry/config/RegistryConfig.java
package edu.unisabana.tyvs.registry.config;

import edu.unisabana.tyvs.registry.application.port.out.RegistryRepositoryPort;
import edu.unisabana.tyvs.registry.application.usecase.Registry;
import edu.unisabana.tyvs.registry.infrastructure.persistence.RegistryRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RegistryConfig {

    // Define el bean para el repositorio esto aplica si es la configuracion
    // principal

    @Bean
    public RegistryRepositoryPort registryRepositoryPort() throws Exception {
        // Usa una base de datos puramente en memoria sin persistencia extra
        RegistryRepository repo = new RegistryRepository("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1");
        repo.initSchema();
        return repo;
    }

    @Bean
    public Registry registry(RegistryRepositoryPort repo) {
        return new Registry(repo);
    }
}
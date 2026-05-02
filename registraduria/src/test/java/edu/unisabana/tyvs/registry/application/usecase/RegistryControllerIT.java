package edu.unisabana.tyvs.registry.application.usecase;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.http.*;
import org.springframework.test.context.junit4.SpringRunner;

import edu.unisabana.tyvs.registry.application.port.out.RegistryRepositoryPort;
import edu.unisabana.tyvs.registry.infrastructure.persistence.RegistryRepository;

import static org.junit.Assert.*;

/**
 * Pruebas de sistema (caja negra) para {@link RegistryController}.
 *
 * <p>
 * Validan el comportamiento completo del sistema a través de su
 * interfaz HTTP, sin conocer la implementación interna.
 * </p>
 *
 * <p>
 * Usa {@link TestRestTemplate} para enviar peticiones reales al servidor
 * embebido levantado en un puerto aleatorio.
 * </p>
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class RegistryControllerIT {

    /** Configura beans de prueba: repositorio H2 + caso de uso. */
    @TestConfiguration
    static class TestBeans {

        @Bean
        public RegistryRepositoryPort registryRepositoryPort() throws Exception {
            String jdbc = "jdbc:h2:mem:itdb;DB_CLOSE_DELAY=-1";
            RegistryRepository repo = new RegistryRepository(jdbc);
            repo.initSchema();
            return repo;
        }

        @Bean
        public edu.unisabana.tyvs.registry.application.usecase.Registry registry(
                RegistryRepositoryPort port) {
            return new edu.unisabana.tyvs.registry.application.usecase.Registry(port);
        }
    }

    @Autowired
    private TestRestTemplate rest;

    @Autowired
    private RegistryRepositoryPort repo;

    /** Limpia la BD antes de cada test para garantizar aislamiento. */
    @Before
    public void cleanUp() throws Exception {
        repo.deleteAll();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Utilitario interno
    // ─────────────────────────────────────────────────────────────────────────

    private ResponseEntity<String> post(String jsonBody) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return rest.postForEntity("/register",
                new HttpEntity<>(jsonBody, headers), String.class);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // CASO 1 – Registro exitoso → HTTP 200, body "VALID"
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    public void shouldRegisterValidPerson() {
        // Arrange
        String json = "{\"name\":\"Ana\",\"id\":100,\"age\":30,\"gender\":\"FEMALE\",\"alive\":true}";

        // Act
        ResponseEntity<String> resp = post(json);

        // Assert
        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertEquals("VALID", resp.getBody());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // CASO 2 – Registro duplicado → HTTP 200, body "DUPLICATED"
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    public void shouldReturnDuplicatedOnSecondRegistration() {
        // Arrange – primera petición
        String json = "{\"name\":\"Pedro\",\"id\":200,\"age\":35,\"gender\":\"MALE\",\"alive\":true}";
        post(json); // primer registro (VALID)

        // Act – segunda petición con mismo ID
        ResponseEntity<String> resp = post(json);

        // Assert
        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertEquals("DUPLICATED", resp.getBody());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // CASO 3 – Menor de edad → HTTP 200, body "UNDERAGE"
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    public void shouldReturnUnderageForMinor() {
        // Arrange
        String json = "{\"name\":\"Niño\",\"id\":301,\"age\":15,\"gender\":\"MALE\",\"alive\":true}";

        // Act
        ResponseEntity<String> resp = post(json);

        // Assert
        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertEquals("UNDERAGE", resp.getBody());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // CASO 4 – Persona fallecida → HTTP 200, body "DEAD"
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    public void shouldReturnDeadForDeceasedPerson() {
        // Arrange
        String json = "{\"name\":\"Carlos\",\"id\":401,\"age\":50,\"gender\":\"MALE\",\"alive\":false}";

        // Act
        ResponseEntity<String> resp = post(json);

        // Assert
        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertEquals("DEAD", resp.getBody());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // CASO 5 – Género inválido → HTTP 400 (Defecto 05 corregido)
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    public void shouldReturn400ForInvalidGender() {
        // Arrange – "OTHER" no es un valor del enum Gender
        String json = "{\"name\":\"Laura\",\"id\":500,\"age\":20,\"gender\":\"OTHER\",\"alive\":true}";

        // Act
        ResponseEntity<String> resp = post(json);

        // Assert – debe ser 400, NO 500
        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
        assertNotNull(resp.getBody());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // CASO 6 – JSON incompleto → HTTP 400
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    public void shouldReturn400OrErrorForMalformedJson() {
        // Arrange – JSON sin cerrar
        String malformed = "{\"name\":\"Test\",\"id\":600";

        // Act
        ResponseEntity<String> resp = post(malformed);

        // Assert – no debe ser 200
        assertNotEquals(HttpStatus.OK, resp.getStatusCode());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // CASO 7 – ID inválido (0) → HTTP 200, body "INVALID"
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    public void shouldReturnInvalidForZeroId() {
        // Arrange
        String json = "{\"name\":\"Test\",\"id\":0,\"age\":25,\"gender\":\"MALE\",\"alive\":true}";

        // Act
        ResponseEntity<String> resp = post(json);

        // Assert
        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertEquals("INVALID", resp.getBody());
    }
}

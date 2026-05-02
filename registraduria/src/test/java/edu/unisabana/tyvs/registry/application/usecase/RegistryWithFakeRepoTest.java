package edu.unisabana.tyvs.registry.application.usecase;

import edu.unisabana.tyvs.registry.domain.model.Gender;
import edu.unisabana.tyvs.registry.domain.model.Person;
import edu.unisabana.tyvs.registry.domain.model.RegisterResult;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Pruebas usando {@link FakeRepository} (reto adicional).
 *
 * <p>Demuestra cómo un "Fake" permite verificar el estado interno
 * del repositorio sin usar Mockito ni H2.</p>
 */
public class RegistryWithFakeRepoTest {

    private FakeRepository fakeRepo;
    private Registry registry;

    @Before
    public void setUp() {
        fakeRepo = new FakeRepository();
        registry = new Registry(fakeRepo);
    }

    @Test
    public void shouldSaveValidPersonInFakeRepo() throws Exception {
        // Arrange
        Person p = new Person("Elena", 1001, 28, Gender.FEMALE, true);

        // Act
        RegisterResult result = registry.registerVoter(p);

        // Assert – resultado correcto
        assertEquals(RegisterResult.VALID, result);
        // Assert – verificamos el estado interno del fake
        assertEquals(1, fakeRepo.count());
        assertTrue(fakeRepo.existsById(1001));
        assertEquals("Elena", fakeRepo.findById(1001).get().getName());
    }

    @Test
    public void shouldRejectDuplicateInFakeRepo() throws Exception {
        // Arrange
        Person p1 = new Person("Elena", 1001, 28, Gender.FEMALE, true);
        Person p2 = new Person("Elena2", 1001, 30, Gender.FEMALE, true);

        // Act
        registry.registerVoter(p1);
        RegisterResult result = registry.registerVoter(p2);

        // Assert
        assertEquals(RegisterResult.DUPLICATED, result);
        // Solo debe haber 1 registro en memoria
        assertEquals(1, fakeRepo.count());
    }

    @Test
    public void shouldNotSaveAnythingForUnderageOrDead() throws Exception {
        // Arrange
        Person underage = new Person("Niño", 2001, 10, Gender.MALE, true);
        Person dead     = new Person("Muerto", 2002, 50, Gender.MALE, false);

        // Act
        registry.registerVoter(underage);
        registry.registerVoter(dead);

        // Assert – el repositorio debe estar vacío
        assertEquals(0, fakeRepo.count());
    }
}

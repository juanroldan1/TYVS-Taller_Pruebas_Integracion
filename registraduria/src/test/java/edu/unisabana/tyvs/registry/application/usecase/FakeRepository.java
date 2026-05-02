package edu.unisabana.tyvs.registry.application.usecase;

import edu.unisabana.tyvs.registry.application.port.out.RegistryRepositoryPort;
import edu.unisabana.tyvs.registry.infrastructure.persistence.RegistryRecord;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Implementación falsa (Fake) del repositorio que guarda datos en memoria
 * usando un {@link HashMap}. No usa Mockito ni base de datos real.
 *
 * <p>Útil para pruebas rápidas donde se necesita verificar el estado
 * interno del repositorio sin dependencias externas.</p>
 *
 * <p>Diferencia con el mock:</p>
 * <ul>
 *   <li><b>Mock (Mockito):</b> simula comportamiento, no guarda estado real.</li>
 *   <li><b>Fake:</b> implementación simplificada pero funcionalmente correcta.</li>
 * </ul>
 */
public class FakeRepository implements RegistryRepositoryPort {

    /** Almacenamiento en memoria: clave = ID del votante. */
    private final Map<Integer, RegistryRecord> store = new HashMap<>();

    @Override
    public void initSchema() {
        // No requiere inicialización; el HashMap ya está listo.
    }

    @Override
    public boolean existsById(int id) {
        return store.containsKey(id);
    }

    @Override
    public void save(int id, String name, int age, boolean isAlive) {
        store.put(id, new RegistryRecord(id, name, age, isAlive));
    }

    @Override
    public Optional<RegistryRecord> findById(int id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public void deleteAll() {
        store.clear();
    }

    /** Devuelve la cantidad de registros actuales (útil en pruebas). */
    public int count() {
        return store.size();
    }
}

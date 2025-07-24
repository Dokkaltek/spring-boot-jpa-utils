package io.github.dokkaltek.repository.addon;

import io.github.dokkaltek.TestIntegrationRunner;
import io.github.dokkaltek.helper.EntriesWithSequence;
import io.github.dokkaltek.model.OracleTestEntity;
import io.github.dokkaltek.repository.OracleTestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.EnabledIf;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for {@link BatchRepositoryAddonImpl} class with oracle test database.
 */
@EnabledIf(expression = "#{environment['spring.profiles.active'] == 'oracle'}")
@SpringBootTest
@ContextConfiguration(classes = {TestIntegrationRunner.class})
@Transactional
class OracleBatchRepositoryTest {
    @Autowired
    private OracleTestRepository repository;

    private List<OracleTestEntity> sampleEntities;

    @BeforeEach
    void setUp() {
        sampleEntities = List.of(
                OracleTestEntity.builder()
                        .id(1L)
                        .name("Test 1")
                        .age(28)
                        .build(),
                OracleTestEntity.builder()
                        .id(2L)
                        .name("Test 2")
                        .age(30)
                        .build());
    }

    /**
     * Tests {@link BatchRepositoryAddonImpl#insertAll(Collection)} method.
     */
    @Test
    @DisplayName("Test multiple insert with insertAll")
    void testInsertAll() {
        String name = "InsertAll test";
        sampleEntities.forEach(entity -> entity.setName(name));
        sampleEntities.get(0).setId(10L);
        sampleEntities.get(1).setId(11L);
        repository.insertAll(sampleEntities);
        long count = repository.countByName(name);
        assertEquals(2, count);

        assertDoesNotThrow(() -> repository.insertAll(Collections.emptyList()));
    }

    /**
     * Tests {@link BatchRepositoryAddonImpl#insertAllInBatchWithSequence(Collection)} method.
     */
    @Test
    @DisplayName("Test multiple insert with sequence")
    void testInsertAllInBatchWithSequence() {
        String name = "InsertAll batch with sequence test";
        sampleEntities.forEach(entity -> entity.setName(name));
        List<EntriesWithSequence> sampleEntries = List.of(new EntriesWithSequence(null,
                "id", 1, sampleEntities));
        repository.insertAllInBatchWithSequence(sampleEntries);
        long count = repository.countByName(name);
        assertEquals(2, count);

        assertDoesNotThrow(() -> repository.insertAllInBatchWithSequence(Collections.emptyList()));
    }

    /**
     * Tests {@link BatchRepositoryAddonImpl#oracleInsertAll(Collection...)} method.
     */
    @Test
    @DisplayName("Test oracle multiple insert")
    void testOracleInsertAll() {
        String name = "Oracle insert all test";
        sampleEntities.get(0).setId(12L);
        sampleEntities.get(1).setId(13L);
        sampleEntities.forEach(entity -> entity.setName(name));
        repository.oracleInsertAll(sampleEntities);
        long count = repository.countByName(name);
        assertEquals(2, count);

        assertDoesNotThrow(() -> repository.oracleInsertAll(Collections.emptyList()));
    }

    /**
     * Tests {@link BatchRepositoryAddonImpl#oracleInsertAllWithSequenceId(Collection)} method.
     */
    @Test
    @DisplayName("Test oracle multiple insert with sequence")
    void testOracleInsertAllWithSequenceId() {
        String name = "Oracle insert all with sequence test";
        sampleEntities.forEach(entity -> entity.setName(name));
        List<EntriesWithSequence> sampleEntries = List.of(new EntriesWithSequence(null,
                "id", 1, sampleEntities));
        repository.oracleInsertAllWithSequenceId(sampleEntries);
        long count = repository.countByName(name);
        assertEquals(2, count);

        assertDoesNotThrow(() -> repository.oracleInsertAllWithSequenceId(Collections.emptyList()));
    }

}

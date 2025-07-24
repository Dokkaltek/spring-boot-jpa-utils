package io.github.dokkaltek.repository.addon;

import io.github.dokkaltek.TestIntegrationRunner;
import io.github.dokkaltek.helper.PrimaryKeyEntries;
import io.github.dokkaltek.model.TestEntity;
import io.github.dokkaltek.repository.TestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for {@link BatchRepositoryAddonImpl} class.
 */
@SpringBootTest
@ContextConfiguration(classes = {TestIntegrationRunner.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@TestPropertySource("/application-test.properties")
class BatchRepositoryAddonTest {
    @Autowired
    private TestRepository repository;

    private List<TestEntity> sampleEntities;

    @BeforeEach
    void setUp() {
        sampleEntities = List.of(
                TestEntity.builder()
                        .name("Test 1")
                        .active(true)
                        .age(28)
                        .build(),
                TestEntity.builder()
                        .name("Test 2")
                        .active(false)
                        .age(30)
                        .build());
    }

    /**
     * Tests {@link BatchRepositoryAddonImpl#insertAll(Collection)} method.
     */
    @Test
    @Transactional
    @DisplayName("Test multiple insert with insertAll")
    void testInsertAll() {
        repository.insertAll(sampleEntities);
        assertEquals(2, repository.findAllById(List.of(6L, 7L)).size());

        assertDoesNotThrow(() -> repository.insertAll(Collections.emptyList()));
    }

    /**
     * Tests {@link BatchRepositoryAddonImpl#insertAllInBatch(Collection, Collection...)} method.
     */
    @Test
    @Transactional
    @DisplayName("Test batch inserts")
    void testInsertAllInBatch() {
        sampleEntities.get(0).setId(6L);
        sampleEntities.get(1).setId(7L);
        repository.insertAllInBatch(sampleEntities);
        assertEquals(2, repository.findAllById(List.of(6L, 7L)).size());
        assertDoesNotThrow(() -> repository.insertAllInBatch(Collections.emptyList()));
    }

    /**
     * Tests {@link BatchRepositoryAddonImpl#updateAllInBatch(Collection, Collection...)} method.
     */
    @Test
    @Transactional
    @DisplayName("Test batch updates")
    void testUpdateAllInBatch() {
        sampleEntities.get(0).setId(4L);
        sampleEntities.get(1).setId(5L);
        repository.updateAllInBatch(sampleEntities);
        List<TestEntity> result = repository.findAllById(List.of(4L, 5L));
        assertEquals(2, result.size());
        TestEntity entityFour = result.stream().filter(item -> item.getId() == 4L)
                .findFirst().orElse(new TestEntity());
        TestEntity entityFive = result.stream().filter(item -> item.getId() == 5L)
                .findFirst().orElse(new TestEntity());
        assertEquals(sampleEntities.get(0), entityFour);
        assertEquals(sampleEntities.get(1), entityFive);

        assertDoesNotThrow(() -> repository.updateAllInBatch(Collections.emptyList()));
    }

    /**
     * Tests {@link BatchRepositoryAddonImpl#removeAllByIdInBatch(PrimaryKeyEntries, PrimaryKeyEntries...)} method.
     */
    @Test
    @Transactional
    @DisplayName("Test batch deletes")
    void testRemoveAllByIdInBatch() {
        repository.removeAllByIdInBatch(new PrimaryKeyEntries<>(List.of(4L, 5L), TestEntity.class));
        List<TestEntity> result = repository.findAllById(List.of(4L, 5L));
        assertEquals(0, result.size());

        assertDoesNotThrow(() -> repository.deleteAllInBatch(Collections.emptyList()));
    }
}

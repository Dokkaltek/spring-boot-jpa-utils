package io.github.dokkaltek.repository.addon;

import io.github.dokkaltek.TestIntegrationRunner;
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
@TestPropertySource("/application-rewrite.properties")
class BatchRepositoryAddonWithRewriteTest {
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
     * Tests {@link BatchRepositoryAddonImpl#insertAllInBatch(Collection, Collection...)} method.
     */
    @Test
    @Transactional
    @DisplayName("Test batch inserts with query rewrite")
    void testInsertAllInBatchWithQueryRewrite() {
        // Change properties
        repository.insertAllInBatch(sampleEntities);
        assertEquals(2, repository.findAllById(List.of(6L, 7L)).size());
        assertDoesNotThrow(() -> repository.insertAllInBatch(Collections.emptyList()));
    }
}

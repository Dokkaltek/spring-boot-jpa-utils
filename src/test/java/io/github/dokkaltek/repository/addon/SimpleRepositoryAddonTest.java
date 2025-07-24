package io.github.dokkaltek.repository.addon;

import io.github.dokkaltek.TestIntegrationRunner;
import io.github.dokkaltek.model.TestEntity;
import io.github.dokkaltek.repository.TestRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for {@link SimpleRepositoryAddonImpl} class.
 */
@SpringBootTest
@ContextConfiguration(classes = {TestIntegrationRunner.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@TestPropertySource("/application-test.properties")
@Transactional
class SimpleRepositoryAddonTest {
    @Autowired
    private TestRepository repository;

    private TestEntity sampleEntity;
    private List<TestEntity> sampleEntities;

    @BeforeEach
    void setup() {
        sampleEntity = TestEntity.builder()
                .name("Test")
                .description("Test description")
                .active(true)
                .age(25)
                .lastUsageTime(LocalTime.of(10, 5, 10))
                .createdAt(LocalDateTime.of(2025, 7, 18, 21, 25, 10))
                .build();

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
     * Tests {@link SimpleRepositoryAddonImpl#find(Class, Object)} method.
     */
    @Test
    @DisplayName("Test finding an entity by id")
    void testFind() {
        // Test with result
        Optional<TestEntity> result = repository.find(TestEntity.class, 1L);
        assertTrue(result.isPresent());
        assertEquals("Eduardo", result.get().getName());

        // Test without result
        result = repository.find(TestEntity.class, 0L);
        assertTrue(result.isEmpty());
    }

    /**
     * Tests {@link SimpleRepositoryAddonImpl#persist(Object)} method.
     */
    @Test
    @DisplayName("Test persisting an entity")
    void testPersist() {
        TestEntity result = repository.persist(sampleEntity);

        assertNotNull(result);
        assertEquals(sampleEntity, result);
    }

    /**
     * Tests {@link SimpleRepositoryAddonImpl#persistAndFlush(Object)} method.
     */
    @Test
    @DisplayName("Test persisting an entity and then flushing")
    void testPersistAndFlush() {
        TestEntity result = repository.persistAndFlush(sampleEntity);

        assertNotNull(result);
        assertNotNull(result.getId());
    }

    /**
     * Tests {@link SimpleRepositoryAddonImpl#persistAll(Iterable)} method.
     */
    @Test
    @DisplayName("Test persisting a collection of entities")
    void testPersistAll() {
        List<TestEntity> result = repository.persistAll(List.of(sampleEntity, sampleEntity));

        assertNotNull(result);
        assertEquals(2, result.size());

        // Test empty list
        result = repository.persistAll(Collections.emptyList());
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    /**
     * Tests {@link SimpleRepositoryAddonImpl#persistAll(Iterable)} method.
     */
    @Test
    @DisplayName("Test persisting a collection of entities and flushing")
    void testPersistAllAndFlush() {
        List<TestEntity> result = repository.persistAllAndFlush(sampleEntities);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(6, result.get(0).getId());
        assertEquals(7, result.get(1).getId());

        // Test empty list
        result = repository.persistAllAndFlush(Collections.emptyList());
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    /**
     * Tests {@link SimpleRepositoryAddonImpl#remove(Object)} method.
     */
    @Test
    @DisplayName("Test removing an entity")
    void testRemove() {
        Optional<TestEntity> entity = repository.findById(5L);
        assertTrue(entity.isPresent());
        entity.ifPresent(repository::remove);

        Optional<TestEntity> result = repository.find(TestEntity.class, 5L);
        assertTrue(result.isEmpty());
    }

    /**
     * Tests {@link SimpleRepositoryAddonImpl#removeAll(Iterable)} method.
     */
    @Test
    @DisplayName("Test removing a list of entities")
    void testRemoveAll() {
        List<TestEntity> result = repository.findAllById(List.of(4L, 5L));
        assertEquals(2, result.size());

        // Remove them after finding them
        repository.removeAll(result);
        result = repository.findAllById(List.of(4L, 5L));
        assertEquals(0, result.size());
    }

    /**
     * Tests {@link SimpleRepositoryAddonImpl#deleteById(Object, Class)} method.
     */
    @Test
    @DisplayName("Test deleting an entry by id")
    void testDeleteById() {
        // Remove the item and then search it to make sure it were deleted
        assertTrue(repository.deleteById(5L, TestEntity.class));
        Optional<TestEntity> result = repository.findById(5L);
        assertTrue(result.isEmpty());
    }

    /**
     * Tests {@link SimpleRepositoryAddonImpl#deleteAllById(Iterable, Class)} method.
     */
    @Test
    @DisplayName("Test deleting a list of entities by id")
    void testDeleteAllById() {
        // Remove them and then search them to make sure they were deleted
        assertEquals(2, repository.deleteAllById(List.of(4L, 5L), TestEntity.class));
        List<TestEntity> result = repository.findAllById(List.of(4L, 5L));
        assertEquals(0, result.size());
    }

    /**
     * Tests {@link SimpleRepositoryAddonImpl#merge(Object)} method.
     */
    @Test
    @DisplayName("Test merging an entity")
    void testMerge() {
        Optional<TestEntity> entity = repository.findById(5L);
        assertTrue(entity.isPresent());

        TestEntity result = entity.get();
        result.setDescription("test description");

        result = repository.merge(result);

        assertEquals("test description", result.getDescription());
        assertEquals(5L, result.getId());

        entity = repository.findById(5L);
        assertTrue(entity.isPresent());

        assertEquals(result, entity.get());
    }

    /**
     * Tests {@link SimpleRepositoryAddonImpl#mergeAndFlush(Object)} method.
     */
    @Test
    @DisplayName("Test merging an entity and then flushing")
    void testMergeAndFlush() {
        Optional<TestEntity> entity = repository.findById(5L);
        assertTrue(entity.isPresent());

        TestEntity result = entity.get();
        result.setDescription("test description");

        result = repository.mergeAndFlush(result);

        assertEquals("test description", result.getDescription());
        assertEquals(5L, result.getId());

        entity = repository.findById(5L);
        assertTrue(entity.isPresent());

        assertEquals(result, entity.get());
    }

    /**
     * Tests {@link SimpleRepositoryAddonImpl#mergeAll(Iterable)} method.
     */
    @Test
    @DisplayName("Test merging a list of entities")
    void testMergeAll() {
        Optional<TestEntity> entity = repository.findById(5L);
        assertTrue(entity.isPresent());

        TestEntity result = entity.get();
        result.setDescription("test description");

        List<TestEntity> resultList = repository.mergeAll(List.of(result, sampleEntity));

        assertEquals("test description", resultList.get(0).getDescription());
        assertEquals(5L, resultList.get(0).getId());

        resultList = repository.findAllById(List.of(5L, 6L));
        assertEquals(2, resultList.size());

        assertEquals(resultList.get(0), result);
        assertEquals(6L, resultList.get(1).getId());
    }

    /**
     * Tests {@link SimpleRepositoryAddonImpl#mergeAllAndFlush(Iterable)} method.
     */
    @Test
    @DisplayName("Test merging a list of entities flushing each of them")
    void testMergeAllAndFlush() {
        Optional<TestEntity> entity = repository.findById(5L);
        assertTrue(entity.isPresent());

        TestEntity result = entity.get();
        result.setDescription("test description");

        List<TestEntity> resultList = repository.mergeAllAndFlush(List.of(result, sampleEntity));

        assertEquals("test description", resultList.get(0).getDescription());
        assertEquals(5L, resultList.get(0).getId());

        resultList = repository.findAllById(List.of(5L, 6L));
        assertEquals(2, resultList.size());

        assertEquals(resultList.get(0), result);
        assertEquals(6L, resultList.get(1).getId());
    }

    /**
     * Tests {@link SimpleRepositoryAddonImpl#update(Object)} method.
     */
    @Test
    @DisplayName("Test updating an entity")
    void testUpdate() {
        TestEntity entryToUpdate = TestEntity.builder()
                .id(5L)
                .name("TEST")
                .description("test description")
                .active(true)
                .age(99)
                .lastUsageTime(LocalTime.of(10, 5, 10))
                .createdAt(LocalDateTime.of(2025, 7, 18, 21, 25, 10))
                .build();

        assertTrue(repository.update(entryToUpdate));

        Optional<TestEntity> optionalResult = repository.findById(5L);
        assertTrue(optionalResult.isPresent());
        TestEntity result = optionalResult.get();
        assertEquals(entryToUpdate, result);
    }

    /**
     * Tests {@link SimpleRepositoryAddonImpl#update(Object, Set)} method.
     */
    @Test
    @DisplayName("Test partially updating an entity")
    void testPartialUpdate() {
        TestEntity entryToUpdate = TestEntity.builder()
                .id(4L)
                .name("TEST")
                .build();

        assertTrue(repository.update(entryToUpdate, Set.of("name")));

        Optional<TestEntity> optionalResult = repository.findById(4L);
        assertTrue(optionalResult.isPresent());
        TestEntity result = optionalResult.get();
        assertEquals(entryToUpdate.getName(), result.getName());
        assertNotNull(result.getDescription());
        assertNotNull(result.getAge());
        assertNotNull(result.getCreatedAt());
    }

    /**
     * Tests {@link SimpleRepositoryAddonImpl#updateAll(Iterable)} method.
     */
    @Test
    @DisplayName("Test updating a list of entities")
    void testUpdateAll() {
        sampleEntities.get(0).setId(5L);
        sampleEntities.get(1).setId(4L);

        assertEquals(2, repository.updateAll(sampleEntities));

        List<TestEntity> optionalResult = repository.findAllById(List.of(4L, 5L));
        assertEquals(2, optionalResult.size());
        assertEquals(sampleEntities.get(0), optionalResult.stream()
                .filter(item -> item.getId() == 5L)
                .findFirst().orElse(null));
        assertEquals(sampleEntities.get(1), optionalResult.stream()
                .filter(item -> item.getId() == 4L)
                .findFirst().orElse(null));
    }

    /**
     * Tests {@link SimpleRepositoryAddonImpl#updateAll(Iterable, Set)} method.
     */
    @Test
    @DisplayName("Test updating a list of entities partially")
    void testPartialUpdateAll() {
        sampleEntities.get(0).setId(5L);
        sampleEntities.get(1).setId(4L);

        assertEquals(2, repository.updateAll(sampleEntities, Set.of("name")));

        List<TestEntity> optionalResult = repository.findAllById(List.of(4L, 5L));
        assertEquals(2, optionalResult.size());
        TestEntity entityFive = optionalResult.stream()
                .filter(item -> item.getId() == 5L)
                .findFirst().orElse(new TestEntity());
        assertEquals(sampleEntities.get(0).getName(), entityFive.getName());
        assertNull(entityFive.getAge());
        TestEntity entityFour = optionalResult.stream()
                .filter(item -> item.getId() == 4L)
                .findFirst().orElse(new TestEntity());
        assertEquals(sampleEntities.get(1).getName(), entityFour.getName());
        assertEquals(55, entityFour.getAge());
    }

    /**
     * Tests {@link SimpleRepositoryAddonImpl#detach(Object)} method.
     */
    @Test
    @DisplayName("Test detaching an entity")
    void testDetach() {
        Optional<TestEntity> entryFound = repository.findById(5L);

        assertTrue(entryFound.isPresent());
        TestEntity entryToUpdate = entryFound.get();
        assertTrue(repository.contains(entryToUpdate));
        assertDoesNotThrow(() -> repository.detach(entryToUpdate));
        assertFalse(repository.contains(entryToUpdate));
    }

    /**
     * Tests {@link SimpleRepositoryAddonImpl#detachAll(Iterable)} method.
     */
    @Test
    @DisplayName("Test detaching a list of entities")
    void testDetachAll() {
        List<TestEntity> entriesFound = repository.findAllById(List.of(4L, 5L));
        assertTrue(repository.containsAll(entriesFound));
        assertDoesNotThrow(() -> repository.detachAll(entriesFound));
        assertFalse(repository.containsAll(entriesFound));
    }

    /**
     * Tests {@link SimpleRepositoryAddonImpl#refresh(Object)} method.
     */
    @Test
    @DisplayName("Test refreshing an entity")
    void testRefresh() {
        Optional<TestEntity> entryFound = repository.findById(4L);
        assertTrue(entryFound.isPresent());
        TestEntity entity = entryFound.get();
        entity.setName("TEST");
        entity.setDescription("TEST");
        repository.refresh(entity);
        assertEquals(4L, entity.getId());
        assertEquals("Juan", entity.getName());
        assertEquals("SOME DESCRIPTION", entity.getDescription());
    }

    /**
     * Tests {@link SimpleRepositoryAddonImpl#refreshAll(Iterable)} method.
     */
    @Test
    @DisplayName("Test refreshing a list of entities")
    void testRefreshAll() {
        List<TestEntity> entriesFound = repository.findAllById(List.of(4L, 5L));

        for (TestEntity entry : entriesFound) {
            entry.setName("TEST");
            entry.setDescription("TEST");
        }

        repository.refreshAll(entriesFound);
        TestEntity entityFour = entriesFound.stream().filter(item -> item.getId() == 4L).findFirst()
                .orElse(new TestEntity());
        TestEntity entityFive = entriesFound.stream().filter(item -> item.getId() == 5L).findFirst()
                .orElse(new TestEntity());
        assertEquals("Juan", entityFour.getName());
        assertEquals("SOME DESCRIPTION", entityFour.getDescription());
        assertEquals("Angel", entityFive.getName());
        assertNull(entityFive.getDescription());
    }

    /**
     * Tests {@link SimpleRepositoryAddonImpl#getEntityManager(Class)} method.
     */
    @Test
    @DisplayName("Test getting the entity manager of an entity")
    void testGetEntityManager() {
        EntityManager entityManager = repository.getEntityManager(TestEntity.class);
        assertNotNull(entityManager);
    }
}

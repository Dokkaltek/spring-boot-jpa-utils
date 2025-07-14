package io.github.dokkaltek.repository;

import io.github.dokkaltek.model.TestEntity;
import io.github.dokkaltek.repository.addon.BatchRepositoryAddon;
import io.github.dokkaltek.repository.addon.SimpleRepositoryAddon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

/**
 * Test repository for database operations.
 */
@Repository
public interface TestRepository extends JpaRepository<TestEntity, Long>, SimpleRepositoryAddon, BatchRepositoryAddon,
        JpaSpecificationExecutor<TestEntity> {
}

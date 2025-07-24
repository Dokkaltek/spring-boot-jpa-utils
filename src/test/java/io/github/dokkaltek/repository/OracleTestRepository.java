package io.github.dokkaltek.repository;

import io.github.dokkaltek.model.OracleTestEntity;
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
public interface OracleTestRepository extends JpaRepository<OracleTestEntity, Long>, SimpleRepositoryAddon, BatchRepositoryAddon,
        JpaSpecificationExecutor<TestEntity> {

    /**
     * Counts records by name.
     *
     * @param name The name to match.
     * @return The count.s
     */
    long countByName(String name);
}

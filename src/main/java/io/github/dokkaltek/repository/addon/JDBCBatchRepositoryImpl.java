package io.github.dokkaltek.repository.addon;

import io.github.dokkaltek.helper.EntriesWithSequence;
import io.github.dokkaltek.helper.QueryData;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.jpa.repository.JpaContext;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.util.Pair;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static io.github.dokkaltek.util.EntityReflectionUtils.getEntitySequenceName;
import static io.github.dokkaltek.util.EntityReflectionUtils.setField;
import static io.github.dokkaltek.util.QueryBuilderUtils.generateMultiInsertStatement;
import static io.github.dokkaltek.util.QueryBuilderUtils.generateOracleInsertAllSQL;
import static io.github.dokkaltek.util.QueryBuilderUtils.generateOracleInsertAllWithSequenceId;
import static io.github.dokkaltek.util.QueryBuilderUtils.getOracleSequencesForEntities;

/**
 * Implementation of {@link JDBCBatchRepository}.
 *
 * @param <T> The entity of the repository.
 */
@Transactional
@RequiredArgsConstructor
public class JDBCBatchRepositoryImpl<T, I> implements JDBCBatchRepository<T, I> {
    private static final String DB_NAME = "DB_NAME";
    private static final String DB_MAJOR_VERSION = "DB_MAJOR_VERSION";
    private final JpaContext jpaContext;

    @Value("${repository.jdbc-batching.batch-size:500}")
    private int defaultBatchSize;

    @Value("${repository.jdbc-batching.rewrite-batch-inserts:false}")
    private boolean shouldRewriteBatchInserts;


    /**
     * {@inheritDoc}
     */
    @Modifying
    @Override
    public <S extends T> void insertAll(Collection<S> entryList) {
        try (EntityManager entityManager = resolveEntityManagerFromLists(entryList)) {
            if (getDbMetadata(entityManager).get(DB_NAME).toLowerCase(Locale.getDefault()).contains("oracle") &&
                    Integer.parseInt(getDbMetadata(entityManager).get(DB_MAJOR_VERSION)) < 23) {
                QueryData oracleInsert = generateOracleInsertAllSQL(entryList);
                executeUpdateQuery(entityManager, oracleInsert.getQuery(), oracleInsert.getPositionBindings());
            } else {
                Pair<String, Map<Integer, Object>> queryPair = generateMultiInsertStatement(entryList);
                executeUpdateQuery(entityManager, queryPair.getFirst(), queryPair.getSecond());
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Modifying
    @Override
    public <S extends T> void insertAllWithSequenceId(Collection<S> entryList, String idParamToGenerate) {
        insertAllWithSequenceId(entryList, idParamToGenerate, null, 1);
    }

    /**
     * {@inheritDoc}
     */
    @Modifying
    @Override
    public <S extends T> void insertAllWithSequenceId(Collection<S> entryList, String idParamToGenerate,
                                                      String sequenceName) {
        insertAllWithSequenceId(entryList, idParamToGenerate, sequenceName, 1);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <S extends T> void insertAllWithSequenceId(Collection<S> entryList, String idParamToGenerate,
                                                      int allocationSize) {
        insertAllWithSequenceId(entryList, idParamToGenerate, null, allocationSize);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <S extends T> void insertAllWithSequenceId(Collection<S> entryList, String idParamToGenerate,
                                                      String sequenceName, int allocationSize) {
        if (sequenceName == null) {
            sequenceName = getEntitySequenceName(entryList.iterator().next(), idParamToGenerate);
        }
        try (EntityManager entityManager = resolveEntityManagerFromLists(entryList)) {
            if (getDbMetadata(entityManager).get(DB_NAME).toLowerCase(Locale.getDefault()).contains("oracle")) {
                QueryData oracleInsert;
                if (allocationSize <= 1) {
                    oracleInsert = generateOracleInsertAllWithSequenceId(entryList, idParamToGenerate, sequenceName);
                    executeUpdateQuery(entityManager, oracleInsert.getQuery(), oracleInsert.getPositionBindings());
                } else {
                    Map<String, List<Long>> sequencesForEntities = getOracleSequencesForEntities(entityManager,
                            Map.of(sequenceName, entryList.size() / allocationSize));

                    List<Long> sequences = sequencesForEntities.get(sequenceName);
                    List<S> updatedEntries = new ArrayList<>(entryList.size());
                    int lastIndex = 0;
                    long sequence = sequences.get(lastIndex);
                    long nextSequence = sequence + allocationSize;
                    for (S entity : entryList) {
                        if (sequence < nextSequence)
                            sequence++;
                        else {
                            lastIndex++;
                            sequence = sequences.get(lastIndex);
                                nextSequence = sequence + allocationSize;
                        }

                        setField(entity, idParamToGenerate, sequence);
                        updatedEntries.add(entity);
                    }
                    insertAll(updatedEntries);
                }
            } else {
                // TODO 
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Modifying
    @Override
    public <S extends T> void insertAllInBatch(Collection<S> entryList, Collection<?>... extraEntries) {
        insertAllInBatchOfSize(defaultBatchSize, entryList, extraEntries);
    }

    /**
     * {@inheritDoc}
     */
    @Modifying
    @Override
    public <S extends T> void insertAllInBatchOfSize(int batchSize, Collection<S> entryList,
                                                     Collection<?>... extraEntries) {
        try (EntityManager entityManger = resolveEntityManagerFromLists(entryList)) {
            Session session = entityManger.unwrap(Session.class);
            session.doWork(connection -> {
                String databaseName = connection.getMetaData().getDatabaseProductName();
                int version = connection.getMetaData().getDatabaseMajorVersion();
                // TODO - Do insert alls
            });
        }
    }

    /**
     * {@inheritDoc}
     */
    @Modifying
    @Override
    public void insertAllInBatchWithSequence(Collection<EntriesWithSequence> entryList) {
        insertAllInBatchOfSizeWithSequence(defaultBatchSize, entryList);
    }

    /**
     * {@inheritDoc}
     */
    @Modifying
    @Override
    public void insertAllInBatchOfSizeWithSequence(int batchSize, Collection<EntriesWithSequence> entryList) {
        // TODO
    }

    /**
     * {@inheritDoc}
     */
    @Modifying
    @Override
    public <S extends T> void oracleInsertAll(Collection<S> entryList, Collection<?>... extraEntities) {
        EntityManager entityManager = resolveEntityManagerFromLists(entryList);
        QueryData oracleInsert = generateOracleInsertAllSQL(entryList, extraEntities);
        executeUpdateQuery(entityManager, oracleInsert.getQuery(), oracleInsert.getPositionBindings());
    }

    /**
     * {@inheritDoc}
     */
    @Modifying
    @Override
    public void oracleInsertAllWithSequenceId(Collection<EntriesWithSequence> entryList) {
        if (entryList == null || entryList.isEmpty())
            return;

        if (entryList.size() == 1) {
            EntriesWithSequence content = entryList.iterator().next();
            oracleInsertAllWithSequence(content.getEntries(), content.getSequenceField(), content.getSequenceName());
        }

        // TODO - Add multiple entities case
    }

    /**
     * {@inheritDoc}
     */
    @Modifying
    @Override
    public <S extends T> void updateAllInBatch(Collection<S> entryList, Collection<?>... extraEntries) {
        updateAllInBatchOfSize(defaultBatchSize, entryList, extraEntries);
    }

    /**
     * {@inheritDoc}
     */
    @Modifying
    @Override
    public <S extends T> void updateAllInBatchOfSize(int batchSize, Collection<S> entryList, Collection<?>... extraEntries) {
        // TODO
    }

    /**
     * {@inheritDoc}
     */
    @Modifying
    @Override
    public void deleteByIdIn(Collection<I>... idList) {
        // TODO
    }

    /**
     * {@inheritDoc}
     */
    @Modifying
    @Override
    public <S extends T> void deleteAllInBatch(Collection<S> entryList, Collection<?>... extraEntities) {
        deleteAllInBatchOfSize(defaultBatchSize, entryList, extraEntities);
    }

    /**
     * {@inheritDoc}
     */
    @Modifying
    @Override
    public <S extends T> void deleteAllInBatchOfSize(int batchSize, Collection<S> entryList, Collection<?>... extraEntities) {
        // TODO
    }

    /**
     * Implementation of the oracle insert all with sequence.
     * @param entryList The entries to insert.
     * @param idParamToGenerate The id parameter to generate with a sequence.
     * @param sequenceName The sequence name.
     */
    private void oracleInsertAllWithSequence(Collection<?> entryList, String idParamToGenerate,
                                                           String sequenceName) {
        if (entryList == null || !entryList.iterator().hasNext()) {
            return;
        }

        EntityManager entityManager = resolveEntityManagerFromLists(entryList);
        QueryData queryData = generateOracleInsertAllWithSequenceId(entryList, idParamToGenerate, sequenceName);
        executeUpdateQuery(entityManager, queryData.getQuery(), queryData.getPositionBindings());
    }

    /**
     * Performs the insert of the elements from the insert all query.
     *
     * @param entityManager The entity manager to manage the persistence.
     * @param insertQuery   The insert query.
     * @param bindings      The bindings to set as values of the query parameters.
     */
    private static void executeUpdateQuery(EntityManager entityManager, String insertQuery,
                                           Map<Integer, Object> bindings) {
        if (entityManager == null || insertQuery.isBlank()) {
            throw new HibernateException("Could not find the entity manager for the batch insert operation.");
        }

        Query query = entityManager.createNativeQuery(insertQuery);
        bindings.forEach(query::setParameter);
        query.executeUpdate();
    }

    /**
     * Resolves the entity manager to use from a group of lists of entries.
     * @param mainList The main list to search for.
     * @param extraLists The extra lists to search for.
     * @return The found {@link EntityManager} or null.
     */
    private EntityManager resolveEntityManagerFromLists(Collection<?> mainList, Collection<?>... extraLists) {
        if (mainList != null && mainList.iterator().hasNext())
            return jpaContext.getEntityManagerByManagedType(mainList.iterator().next().getClass());
        else if (extraLists != null){
            for (Iterable<?> extraList : extraLists) {
                if (extraList != null && extraList.iterator().hasNext())
                    return jpaContext.getEntityManagerByManagedType(extraList.iterator().next().getClass());
            }
        }
        throw new NullPointerException("No entry was found to get the entity manager from.");
    }

    /**
     * Returns the database metadata.
     * @param entityManager The entity manager.
     * @return The database metadata.
     */
    private Map<String, String> getDbMetadata(EntityManager entityManager) {
        Map<String, String> metadata = new HashMap<>(3);
        entityManager.unwrap(Session.class).doWork(connection -> {
            metadata.put(DB_NAME, connection.getMetaData().getDatabaseProductName());
            metadata.put(DB_MAJOR_VERSION, String.valueOf(connection.getMetaData().getDatabaseMajorVersion()));
        });
        return metadata;
    }
}

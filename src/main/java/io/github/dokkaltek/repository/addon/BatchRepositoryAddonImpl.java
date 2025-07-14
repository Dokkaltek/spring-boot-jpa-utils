package io.github.dokkaltek.repository.addon;

import io.github.dokkaltek.exception.BatchOperationException;
import io.github.dokkaltek.helper.BatchData;
import io.github.dokkaltek.helper.EntriesWithSequence;
import io.github.dokkaltek.helper.QueryData;
import io.github.dokkaltek.util.QueryBuilderUtils;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.jpa.repository.JpaContext;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static io.github.dokkaltek.util.EntityReflectionUtils.getEntitySequenceName;
import static io.github.dokkaltek.util.EntityReflectionUtils.setField;
import static io.github.dokkaltek.util.QueryBuilderUtils.clearPositionPlaceholderIndexes;
import static io.github.dokkaltek.util.QueryBuilderUtils.generateBatchInsertsWithSequence;
import static io.github.dokkaltek.util.QueryBuilderUtils.generateMultiInsertStatement;
import static io.github.dokkaltek.util.QueryBuilderUtils.generateMultiInsertWithSequenceId;
import static io.github.dokkaltek.util.QueryBuilderUtils.generateOracleInsertAllStatement;
import static io.github.dokkaltek.util.QueryBuilderUtils.generateOracleInsertAllWithSequenceId;

/**
 * Implementation of {@link BatchRepositoryAddon}.
 */
@Repository
@Transactional
@RequiredArgsConstructor
public class BatchRepositoryAddonImpl implements BatchRepositoryAddon {
    private static final String DB_NAME = "DB_NAME";
    private static final String DB_MAJOR_VERSION = "DB_MAJOR_VERSION";
    private static final String ORACLE_DB_NAME = "oracle";
    private final JpaContext jpaContext;

    @Value("${repository.jdbc-batching.batch-size:500}")
    private int defaultBatchSize;

    @Value("${repository.jdbc-batching.rewrite-batch-inserts:false}")
    private boolean shouldRewriteBatchInserts;

    @Value("${repository.jdbc-batching.rewritten-insert-size:10}")
    private int rewrittenBatchInsertSize;

    /**
     * {@inheritDoc}
     */
    @Modifying
    @Override
    public <S> void insertAll(@NotNull Collection<S> entryList) {
        if (entryList.isEmpty())
            return;

        try (EntityManager entityManager = resolveEntityManagerFromLists(entryList)) {
            if (getDbMetadata(entityManager).get(DB_NAME).toLowerCase(Locale.getDefault()).contains(ORACLE_DB_NAME) &&
                    Integer.parseInt(getDbMetadata(entityManager).get(DB_MAJOR_VERSION)) < 23) {
                QueryData oracleInsert = generateOracleInsertAllStatement(entryList);
                executeUpdateQuery(entityManager, oracleInsert.getQuery(), oracleInsert.getPositionBindings());
            } else {
                QueryData insertQuery = generateMultiInsertStatement(entryList);
                executeUpdateQuery(entityManager, insertQuery.getQuery(), insertQuery.getPositionBindings());
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Modifying
    @Override
    public <S> void insertAllInBatch(@NotNull Collection<S> entryList, Collection<?>... extraEntries) {
        insertAllInBatchOfSize(defaultBatchSize, entryList, extraEntries);
    }

    /**
     * {@inheritDoc}
     */
    @Modifying
    @Override
    public <S> void insertAllInBatchOfSize(int batchSize, @NotNull Collection<S> entryList,
                                                     Collection<?>... extraEntries) {
        if (entryList.isEmpty())
            return;

        try (EntityManager entityManger = resolveEntityManagerFromLists(entryList)) {
            Session session = entityManger.unwrap(Session.class);
            session.doWork(connection -> {
                List<BatchData> batchesData;
                // Merge all lists in an array so that we can iterate over each for a different batch
                Collection<?>[] allLists = new Collection<?>[extraEntries.length + 1];
                allLists[0] = entryList;
                System.arraycopy(extraEntries, 0, allLists, 1, extraEntries.length);

                for (Collection<?> collection : allLists) {
                    if (shouldRewriteBatchInserts) {
                        String databaseName = connection.getMetaData().getDatabaseProductName()
                                .toLowerCase(Locale.getDefault());
                        int version = connection.getMetaData().getDatabaseMajorVersion();
                        boolean useOracleInsert = databaseName.contains(ORACLE_DB_NAME) && version < 23;
                        batchesData = generateRewrittenBatchInserts(EntriesWithSequence.builder()
                                        .entries(collection).build(), batchSize, rewrittenBatchInsertSize,
                                useOracleInsert);
                    } else {
                        batchesData = QueryBuilderUtils.generateBatchInserts(collection, batchSize);
                    }
                    performBatchUpdate(connection, batchesData);
                }
            });
        }
    }

    /**
     * {@inheritDoc}
     */
    @Modifying
    @Override
    public void insertAllInBatchWithSequence(@NotNull Collection<EntriesWithSequence> entryList) {
        insertAllInBatchOfSizeWithSequence(defaultBatchSize, entryList);
    }

    /**
     * {@inheritDoc}
     */
    @Modifying
    @Override
    public void insertAllInBatchOfSizeWithSequence(int batchSize, @NotNull Collection<EntriesWithSequence> entryList) {
        if (entryList.isEmpty())
            return;

        List<EntriesWithSequence> collections = entryList.stream().toList();
        try (EntityManager entityManger = resolveEntityManagerFromLists(collections.get(0).getEntries())) {
            Session session = entityManger.unwrap(Session.class);
            session.doWork(connection -> {
                List<BatchData> batchesData;
                // Merge all lists in an array so that we can iterate over each for a different batch
                for (EntriesWithSequence collection : collections) {
                    if (shouldRewriteBatchInserts) {
                        String databaseName = connection.getMetaData().getDatabaseProductName()
                                .toLowerCase(Locale.getDefault());
                        int version = connection.getMetaData().getDatabaseMajorVersion();
                        boolean useOracleInsert = databaseName.contains(ORACLE_DB_NAME) && version < 23;
                        batchesData = generateRewrittenBatchInserts(collection, batchSize, rewrittenBatchInsertSize,
                                useOracleInsert);
                    } else {
                        batchesData = generateBatchInsertsWithSequence(collection, batchSize);
                    }
                    performBatchUpdate(connection, batchesData);
                }
            });
        }
    }

    /**
     * {@inheritDoc}
     */
    @Modifying
    @Override
    @Transactional
    public void oracleInsertAll(@NotNull Collection<?>... entriesToInsert) {
        EntityManager entityManager = resolveEntityManagerFromLists(entriesToInsert[0]);
        QueryData oracleInsert = generateOracleInsertAllStatement(entriesToInsert);
        executeUpdateQuery(entityManager, oracleInsert.getQuery(), oracleInsert.getPositionBindings());
    }

    /**
     * {@inheritDoc}
     */
    @Modifying
    @Override
    @Transactional
    public void oracleInsertAllWithSequenceId(@NotNull Collection<EntriesWithSequence> entryList) {
        if (entryList.isEmpty())
            return;

        if (entryList.size() == 1) {
            singleCollectionOracleInsertAllWithSequence(entryList.iterator().next());
        } else {
            multiCollectionOracleInsertAllWithSequence(entryList);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Modifying
    @Override
    public <S> void updateAllInBatch(@NotNull Collection<S> entryList, Collection<?>... extraEntries) {
        updateAllInBatchOfSize(defaultBatchSize, entryList, extraEntries);
    }

    /**
     * {@inheritDoc}
     */
    @Modifying
    @Override
    public <S> void updateAllInBatchOfSize(int batchSize, @NotNull Collection<S> entryList,
                                                     Collection<?>... extraEntries) {
        if (entryList.isEmpty())
            return;

        try (EntityManager entityManger = resolveEntityManagerFromLists(entryList)) {
            Session session = entityManger.unwrap(Session.class);
            session.doWork(connection -> {
                List<BatchData> queryData;

                // Merge all lists in an array so that we can iterate over each for a different batch
                Collection<?>[] allLists = new Collection<?>[extraEntries.length + 1];
                allLists[0] = entryList;
                System.arraycopy(extraEntries, 0, allLists, 1, extraEntries.length);

                for (Collection<?> collection : allLists) {
                    queryData = QueryBuilderUtils.generateBatchUpdates(collection, batchSize);
                    performBatchUpdate(connection, queryData);
                }
            });
        }
    }

    /**
     * {@inheritDoc}
     */
    @Modifying
    @Override
    public <S> void deleteAllInBatch(@NotNull Collection<S> entryList, Collection<?>... extraEntities) {
        deleteAllInBatchOfSize(defaultBatchSize, entryList, extraEntities);
    }

    /**
     * {@inheritDoc}
     */
    @Modifying
    @Override
    public <S> void deleteAllInBatchOfSize(int batchSize, @NotNull Collection<S> entryList,
                                                     Collection<?>... extraEntities) {
        if (entryList.isEmpty())
            return;

        try (EntityManager entityManger = resolveEntityManagerFromLists(entryList)) {
            Session session = entityManger.unwrap(Session.class);
            session.doWork(connection -> {
                List<BatchData> queryData;
                // Merge all lists in an array so that we can iterate over each for a different batch
                Collection<?>[] allLists = new Collection<?>[extraEntities.length + 1];
                allLists[0] = entryList;
                System.arraycopy(extraEntities, 0, allLists, 1, extraEntities.length);

                for (Collection<?> collection : allLists) {
                    queryData = QueryBuilderUtils.generateBatchDeletes(collection, batchSize);
                    performBatchUpdate(connection, queryData);
                }
            });
        }
    }

    /**
     * Inserts all entries of a collection in an Oracle database.
     * @param entriesData The data of the entries to insert.
     */
    private void singleCollectionOracleInsertAllWithSequence(EntriesWithSequence entriesData) {
        try (EntityManager entityManager = resolveEntityManagerFromLists(entriesData.getEntries())) {
            QueryData oracleInsert;
            int allocationSize = 1;
            if (entriesData.getAllocationSize() != 0) {
                allocationSize = entriesData.getAllocationSize();
            }

            if (allocationSize <= 1) {
                oracleInsert = generateOracleInsertAllWithSequenceId(entriesData.getEntries(),
                        entriesData.getSequenceField(), entriesData.getSequenceName());
                executeUpdateQuery(entityManager, oracleInsert.getQuery(), oracleInsert.getPositionBindings());
            } else {
                if (entriesData.getSequenceName() == null || entriesData.getSequenceName().isBlank()) {
                    String sequence = getEntitySequenceName(entriesData.getEntries().iterator().next().getClass(),
                            entriesData.getSequenceField());
                    entriesData.setSequenceName(sequence);
                }
                Map<String, List<Long>> sequencesForEntities = getOracleSequencesForEntities(entityManager,
                        Map.of(entriesData.getSequenceName(), entriesData.getEntries().size() / allocationSize));

                oracleInsertAll(updateEntriesWithSequenceCollection(entriesData, sequencesForEntities));
            }
        }
    }

    /**
     * Inserts all entries of a series of collections in an Oracle database.
     * @param entriesData The data of the entries to insert.
     */
    private void multiCollectionOracleInsertAllWithSequence(Collection<EntriesWithSequence> entriesData) {
        Map<String, Integer> sequencesMap = new HashMap<>(entriesData.size());
        EntityManager entityManager =  jpaContext.getEntityManagerByManagedType(entriesData.iterator().next()
                        .getEntries().iterator().next().getClass());
        for (EntriesWithSequence collection : entriesData) {
            if (collection.getSequenceName() == null || collection.getSequenceName().isBlank()) {
                collection.setSequenceName(getEntitySequenceName(collection.getEntries().iterator().next().getClass(),
                        collection.getSequenceField()));
            }

            if (collection.getAllocationSize() == 0) {
                collection.setAllocationSize(1);
            }

            int allocationSize = collection.getAllocationSize();
            sequencesMap.put(collection.getSequenceName(), collection.getEntries().size() / allocationSize);
        }

        Map<String, List<Long>> sequencesForEntities = getOracleSequencesForEntities(entityManager, sequencesMap);

        Collection<?>[] collectionsToInsert = new ArrayList[entriesData.size()];
        int counter = 0;
        for (EntriesWithSequence collection : entriesData) {
            collectionsToInsert[counter] = updateEntriesWithSequenceCollection(collection, sequencesForEntities);
            counter++;
        }

        oracleInsertAll(collectionsToInsert);
    }

    /**
     * Generates the SQL to insert all entries of a collection in a database.
     * @param collection The collection of entries to insert.
     * @param batchSize The size of the batch.
     * @param insertSize The size of the inserts.
     * @return The list of SQL to insert all entries of a collection in a database.
     */
    private static List<BatchData> generateRewrittenBatchInserts(EntriesWithSequence collection,
                                                                 int batchSize,
                                                                 int insertSize,
                                                                 boolean useOracleInsertAll) {
        if (collection == null || collection.getEntries() == null || collection.getEntries().isEmpty()) {
            return Collections.emptyList();
        }

        List<?> collectionEntries = collection.getEntries().stream().toList();

        boolean hasSequenceName = collection.getSequenceName() != null && !collection.getSequenceName().isBlank();
        boolean hasSequenceField = collection.getSequenceField() != null && !collection.getSequenceField().isBlank();
        boolean hasSequence = hasSequenceName && hasSequenceField;

        int batchesSize = (int) Math.ceil((double) collectionEntries.size() / batchSize);
        List<BatchData> queries = new ArrayList<>(batchesSize);

        for (int i = 0; i < batchesSize; i++) {
            BatchData batchData = new BatchData();
            int batchStart = i * batchesSize;
            int batchEnd = Math.min((i + 1) * batchesSize, collectionEntries.size());
            List<?> batchEntries = collectionEntries.subList(batchStart, batchEnd);
            batchData.setQueriesBindings(new ArrayList<>(batchEntries.size()));

            List<QueryData> batchQueries = createRewrittenBatchInserts(collection, batchEntries, insertSize,
                    useOracleInsertAll, hasSequence);

            // Add all batch queries into one
            batchData.getQueriesBindings().addAll(batchQueries.stream().map(QueryData::getPositionBindings).toList());
            queries.add(batchData);
        }

        if (!queries.isEmpty() && !queries.get(0).getQueriesBindings().isEmpty()) {
            String sampleQuery = queries.get(0).getQuery();
            String clearQuery = clearPositionPlaceholderIndexes(sampleQuery);
            queries.forEach(query -> query.setQuery(clearQuery));
        }

        return queries;
    }

    private static List<QueryData> createRewrittenBatchInserts(EntriesWithSequence collection,
                                                               List<?> batchEntries, int insertSize,
                                                               boolean useOracleInsertAll,
                                                               boolean hasSequence) {
        int insertsPerBatch = (int) Math.ceil((double) batchEntries.size() / insertSize);
        List<QueryData> batchQueries = new ArrayList<>(insertsPerBatch);
        for (int j = 0; (j * insertSize) < batchEntries.size(); j++) {
            int start = j * insertsPerBatch;
            int end = Math.min((j + 1) * insertsPerBatch, collection.getEntries().size());
            if (useOracleInsertAll) {
                QueryData insert;
                if (hasSequence) {
                    insert = generateOracleInsertAllWithSequenceId(
                            batchEntries.subList(start, end),
                            collection.getSequenceField(), collection.getSequenceName());
                } else {
                    insert = generateOracleInsertAllStatement(batchEntries.subList(start, end));
                }
                batchQueries.add(insert);
            } else {
                QueryData insert;
                if (hasSequence) {
                    insert = generateMultiInsertWithSequenceId(
                            batchEntries.subList(start, end),
                            collection.getSequenceField(), collection.getSequenceName()
                    );
                } else {
                    insert = generateMultiInsertStatement(
                            batchEntries.subList(start, end));
                }
                batchQueries.add(QueryData.builder()
                        .query(insert.getQuery())
                        .positionBindings(insert.getPositionBindings()).build());
            }
        }
        return batchQueries;
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

    /**
     * Updates the entries with the sequence map.
     * @param collection The collection to update.
     * @param sequencesForEntities The sequences map.
     * @return The updated collection.
     */
    private Collection<?> updateEntriesWithSequenceCollection(EntriesWithSequence collection,
                                                              Map<String, List<Long>> sequencesForEntities) {
        List<Long> sequences = sequencesForEntities.get(collection.getSequenceName());
        List<Object> updatedEntries = new ArrayList<>(collection.getEntries().size());
        int lastIndex = 0;
        long sequence = sequences.get(lastIndex);
        long nextSequence = sequence + collection.getAllocationSize();
        for (Object entity : collection.getEntries()) {
            if (sequence < nextSequence)
                sequence++;
            else {
                lastIndex++;
                sequence = sequences.get(lastIndex);
                nextSequence = sequence + collection.getAllocationSize();
            }

            setField(entity, collection.getSequenceField(), sequence);
            updatedEntries.add(entity);
        }
        return updatedEntries;
    }

    /**
     * Performs a JDBC batch insert or update.
     * @param connection The connection to use.
     * @param queries The queries to execute.
     */
    private void performBatchUpdate(Connection connection, List<BatchData> queries) {
        String query = queries.get(0).getQuery();
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            for (BatchData batchData : queries) {
                for (int i = 0; i < batchData.getQueriesBindings().size(); i++) {
                    statement.clearParameters();
                    Map<Integer, Object> bindings = batchData.getQueriesBindings().get(i);
                    setStatementValues(statement, bindings);
                    statement.addBatch();
                    if ((i + 1) == batchData.getQueriesBindings().size()) {
                        statement.executeBatch();
                        statement.clearBatch();
                    }
                }
            }
        } catch (SQLException e) {
            throw new BatchOperationException(e);
        }
    }

    /**
     * Generates a map of sequences for each entity.
     * @param entityManager The entity manager.
     * @param sequenceQuantityMap A map with the name of the sequence and the number of entities to generate.
     * @return A map with the number sequences for each entity.
     */
    private static Map<String, List<Long>> getOracleSequencesForEntities(@NotNull EntityManager entityManager,
                                                                        @NotNull @NotEmpty Map<String, Integer> sequenceQuantityMap) {
        StringBuilder columns = new StringBuilder();
        int maxSequenceNum = 0;
        for (Map.Entry<String, Integer> entry : sequenceQuantityMap.entrySet()) {
            if (!columns.isEmpty())
                columns.append(", ");

            columns.append("(CASE WHEN rownum <= ").append(entry.getValue()).append(" THEN ").append(entry.getKey())
                    .append(".nextval ELSE null END) as SEQUENCE_").append(entry.getKey());

            if (entry.getValue() > maxSequenceNum)
                maxSequenceNum = entry.getValue();
        }

        String query = "SELECT " + columns + " FROM (SELECT level FROM dual CONNECT BY level <= " + maxSequenceNum +
                ")";
        Query sequenceQuery = entityManager.createNativeQuery(query);

        List<Object[]> sequences = sequenceQuery.getResultList();
        List<String> sequenceNames = sequenceQuantityMap.keySet().stream().toList();
        Map<String, List<Long>> sequencesByEntity = new HashMap<>(sequenceQuantityMap.size());
        for (Object[] sequence : sequences) {
            for (int i = 0; i < sequence.length; i++) {
                String sequenceName = sequenceNames.get(i);
                List<Long> sequenceList = sequencesByEntity.getOrDefault(sequenceName,
                        new ArrayList<>(sequenceQuantityMap.get(sequenceName)));
                if (sequence[i] != null) {
                    sequenceList.add(((Number) sequence[i]).longValue());
                }
                sequencesByEntity.put(sequenceName, sequenceList);
            }
        }

        return sequencesByEntity;
    }

    /**
     * Sets the values of a prepared statement from a map.
     * @param statement The statement to set the values.
     * @param bindings The bindings to set.
     * @throws SQLException If the statement could not be set.
     */
    private void setStatementValues(PreparedStatement statement, Map<Integer, Object> bindings) throws SQLException {
        for(Map.Entry<Integer, Object> entry : bindings.entrySet()) {
            Object paramValue = entry.getValue();
            if (paramValue == null) {
                statement.setNull(entry.getKey(), Types.NULL);
            } else if (paramValue instanceof String string) {
                statement.setString(entry.getKey(), string);
            } else if (paramValue instanceof Integer integer) {
                statement.setInt(entry.getKey(), integer);
            } else if (paramValue instanceof Boolean bool) {
                statement.setBoolean(entry.getKey(), bool);
            } else if (paramValue instanceof Long longNum) {
                statement.setLong(entry.getKey(), longNum);
            } else if (paramValue instanceof Time time) {
                statement.setTime(entry.getKey(), time);
            } else if (paramValue instanceof BigDecimal bigDecimal) {
                statement.setBigDecimal(entry.getKey(), bigDecimal);
            } else if (paramValue instanceof Float floatNumber) {
                statement.setFloat(entry.getKey(), floatNumber);
            } else if (paramValue instanceof Double doubleNumber) {
                statement.setDouble(entry.getKey(), doubleNumber);
            } else if (paramValue instanceof byte[] bytes) {
                statement.setBytes(entry.getKey(), bytes);
            } else if (paramValue instanceof Blob blob) {
                statement.setBlob(entry.getKey(), blob);
            } else if (paramValue instanceof Clob clob) {
                statement.setClob(entry.getKey(), clob);
            } else {
                statement.setObject(entry.getKey(), paramValue);
            }
        }
    }
}

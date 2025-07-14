package io.github.dokkaltek.util;

import io.github.dokkaltek.helper.BatchData;
import io.github.dokkaltek.helper.EntriesWithSequence;
import io.github.dokkaltek.helper.QueryData;
import io.github.dokkaltek.samples.SampleEmbeddedId;
import io.github.dokkaltek.samples.SampleEntity;
import io.github.dokkaltek.samples.SampleIdClassEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static io.github.dokkaltek.util.QueryBuilderUtils.getMultipleEntityColumnsIntoValuesWithSequence;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for {@link QueryBuilderUtils} class.
 */
class QueryBuilderUtilsTest {
    private SampleEntity sampleEntity;
    private SampleIdClassEntity sampleIdClassEntity;

    @BeforeEach
    void setup() {
        sampleEntity = SampleEntity.builder()
                .embeddedId(new SampleEmbeddedId(1L, "0012"))
                .name("name")
                .description("description")
                .build();
        sampleIdClassEntity = SampleIdClassEntity.builder()
                .id(1L)
                .entity("0012")
                .name("name")
                .description("description")
                .build();
    }

    /**
     * Tests {@link QueryBuilderUtils#getEntityColumnsIntoValues} method.
     */
    @Test
    @DisplayName("Test building the entity into values query part")
    void testGetEntityColumnsIntoValues() {
        Map<Integer, Object> bindings = new HashMap<>();
        String query = QueryBuilderUtils.getEntityColumnsIntoValues(sampleEntity, bindings).toString();

        assertTrue(query.contains("VALUES (?1, ?2, ?3, ?4)"));
        assertTrue(query.contains("name"));
        assertTrue(query.contains("desc"));
        assertTrue(query.contains("id"));
        assertTrue(query.contains("entity2"));
        assertEquals(4, bindings.size());
    }

    /**
     * Tests {@link QueryBuilderUtils#getEntityColumnsIntoValuesWithSequence} method.
     */
    @Test
    @DisplayName("Test building the entity into values query part")
    void testGetEntityColumnsIntoValuesWithSequence() {
        Map<Integer, Object> bindings = new HashMap<>();
        String query = QueryBuilderUtils.getEntityColumnsIntoValuesWithSequence(sampleEntity, bindings, "id",
                "sample_seq").toString();

        assertTrue(query.contains("nextval('sample_seq')"));
        assertTrue(query.contains("name"));
        assertTrue(query.contains("desc"));
        assertTrue(query.contains("id"));
        assertTrue(query.contains("entity2"));
        assertEquals(3, bindings.size());
    }

    /**
     * Tests {@link QueryBuilderUtils#getMultipleEntityColumnsIntoValues} method.
     */
    @Test
    @DisplayName("Test building the columns into multiple values query part")
    void testGetMultipleEntityColumnsIntoValues() {
        Map<Integer, Object> bindings = new HashMap<>();
        String query = QueryBuilderUtils.getMultipleEntityColumnsIntoValues(List.of(sampleEntity, sampleEntity),
                bindings).toString();

        assertEquals(" (name, desc, id, entity2) VALUES (?1, ?2, ?3, ?4), (?5, ?6, ?7, ?8)", query);
        assertEquals(8, bindings.size());
    }

    /**
     * Tests {@link QueryBuilderUtils#getMultipleEntityColumnsIntoValuesWithSequence} method.
     */
    @Test
    @DisplayName("Test building the columns into multiple values with sequence query part")
    void testGetMultipleEntityColumnsIntoValuesWithSequence() {
        Map<Integer, Object> bindings = new HashMap<>();
        String query = getMultipleEntityColumnsIntoValuesWithSequence(List.of(sampleEntity, sampleEntity),
                bindings, "embeddedId.id", null).toString();

        assertTrue(query.contains("nextval('sample_seq')"));
        assertTrue(query.contains("(name, desc, id, entity2) VALUES"));
        assertEquals(6, bindings.size());
    }

    /**
     * Tests {@link QueryBuilderUtils#getEntityColumnsWithIdFirst} method.
     */
    @Test
    @DisplayName("Test building the entity columns with id first query part")
    void testGetEntityColumnsWithIdFirst() {
        String query = QueryBuilderUtils.getEntityColumnsWithIdFirst(sampleEntity, "id").toString();
        assertEquals("id, name, desc, entity2", query);
    }

    /**
     * Tests {@link QueryBuilderUtils#getJPQLWhereClauseFilteringByPrimaryKeys} method.
     */
    @Test
    @DisplayName("Test generating a JPQL where clause filtering by primary keys")
    void testGetJPQLWhereClauseFilteringByPrimaryKeys() {
        SampleEmbeddedId embeddedId = new SampleEmbeddedId(1L, "0012");
        SampleEmbeddedId embeddedId2 = new SampleEmbeddedId(2L, "0012");
        List<SampleEmbeddedId> idsList = List.of(embeddedId, embeddedId2);

        // Try with multiple ids
        QueryData resultPair = QueryBuilderUtils.getJPQLWhereClauseFilteringByPrimaryKeys(
                idsList, SampleEntity.class, "se");
        assertNotNull(resultPair);
        assertEquals(" WHERE (se.embeddedId.id = ?1 AND se.embeddedId.entity = ?2) OR " +
                        "(se.embeddedId.id = ?3 AND se.embeddedId.entity = ?4)", resultPair.getQuery());
        assertEquals(4, resultPair.getPositionBindings().size());

        // Try with single id
        resultPair = QueryBuilderUtils.getJPQLWhereClauseFilteringByPrimaryKeys(
                List.of(embeddedId), SampleEntity.class, "se");
        assertNotNull(resultPair);
        assertEquals(" WHERE (se.embeddedId.id = ?1 AND se.embeddedId.entity = ?2)", resultPair.getQuery());
        assertEquals(2, resultPair.getPositionBindings().size());
    }

    /**
     * Tests {@link QueryBuilderUtils#getNativeWhereClauseFilteringByPrimaryKeys} method.
     */
    @Test
    @DisplayName("Test generating a native where clause filtering by primary keys")
    void testGetNativeWhereClauseFilteringByPrimaryKeys() {
        SampleEmbeddedId embeddedId = new SampleEmbeddedId(1L, "0012");
        SampleEmbeddedId embeddedId2 = new SampleEmbeddedId(2L, "0012");
        List<SampleEmbeddedId> idsList = List.of(embeddedId, embeddedId2);

        // Try with multiple ids
        QueryData resultPair = QueryBuilderUtils.getNativeWhereClauseFilteringByPrimaryKeys(
                idsList, SampleEntity.class, "se");
        assertNotNull(resultPair);
        assertEquals(" WHERE (se.id = ?1 AND se.entity2 = ?2) OR " +
                "(se.id = ?3 AND se.entity2 = ?4)", resultPair.getQuery());
        assertEquals(4, resultPair.getPositionBindings().size());

        // Try with single id
        resultPair = QueryBuilderUtils.getNativeWhereClauseFilteringByPrimaryKeys(
                List.of(embeddedId), SampleEntity.class, "se");
        assertNotNull(resultPair);
        assertEquals(" WHERE (se.id = ?1 AND se.entity2 = ?2)", resultPair.getQuery());
        assertEquals(2, resultPair.getPositionBindings().size());
    }

    /**
     * Tests {@link QueryBuilderUtils#generateIntoStatement} method.
     */
    @Test
    @DisplayName("Test generating the into statement for an insert")
    void testGenerateIntoStatement() {
        Map<Integer, Object> bindings = new HashMap<>();

        // Test with embedded id class
        String query = QueryBuilderUtils.generateIntoStatement(sampleEntity, bindings).toString();
        assertNotNull(query);
        assertEquals("INTO sample_entity (name, desc, id, entity2) VALUES (?1, ?2, ?3, ?4)", query);
        assertEquals(4, bindings.size());

        bindings.clear();

        // Test with IdClass
        query = QueryBuilderUtils.generateIntoStatement(sampleIdClassEntity, bindings).toString();
        assertEquals("INTO sample_entity (id, entity2, name, desc) VALUES (?1, ?2, ?3, ?4)", query);
        assertEquals(4, bindings.size());
    }

    /**
     * Tests {@link QueryBuilderUtils#generateInsertStatement(Object)} method.
     */
    @Test
    @DisplayName("Test generating an insert statement")
    void testGenerateInsertStatement() {
        QueryData queryData = QueryBuilderUtils.generateInsertStatement(sampleEntity);
        assertNotNull(queryData);
        assertEquals("INSERT INTO sample_entity (name, desc, id, entity2) VALUES (?1, ?2, ?3, ?4)", queryData.getQuery());
        assertEquals(4, queryData.getPositionBindings().size());
    }

    /**
     * Tests {@link QueryBuilderUtils#generateUpdateStatement(Object)} method.
     */
    @Test
    @DisplayName("Test generating an update statement")
    void testGenerateUpdateStatement() {

        // Test with embedded id class
        QueryData query = QueryBuilderUtils.generateUpdateStatement(sampleEntity);
        assertNotNull(query);
        assertEquals("UPDATE sample_entity SET (name = ?1, desc = ?2) WHERE id = ?3 AND entity2 = ?4",
                query.getQuery());
        assertEquals(4, query.getPositionBindings().size());

        // Test with IdClass
        query = QueryBuilderUtils.generateUpdateStatement(sampleIdClassEntity);
        assertEquals("UPDATE sample_entity SET (name = ?3, desc = ?4) WHERE id = ?1 AND entity2 = ?2",
                query.getQuery());
        assertEquals(4, query.getPositionBindings().size());
    }

    /**
     * Tests {@link QueryBuilderUtils#generateUpdateStatement(Object, Set)} method.
     */
    @Test
    @DisplayName("Test generating a partial update statement")
    void testGeneratePartialUpdateStatement() {
        Set<String> fieldsToUpdate = Set.of("name");

        // Test with embedded id class
        QueryData query = QueryBuilderUtils.generateUpdateStatement(sampleEntity, fieldsToUpdate);
        assertNotNull(query);
        assertEquals("UPDATE sample_entity SET (name = ?1) WHERE id = ?2 AND entity2 = ?3", query.getQuery());
        assertEquals(3, query.getPositionBindings().size());

        // Test with IdClass
        query = QueryBuilderUtils.generateUpdateStatement(sampleIdClassEntity, fieldsToUpdate);
        assertEquals("UPDATE sample_entity SET (name = ?3) WHERE id = ?1 AND entity2 = ?2", query.getQuery());
        assertEquals(3, query.getPositionBindings().size());
    }

    /**
     * Tests {@link QueryBuilderUtils#generateUpdateStatement(Object, Set)} method with errors.
     */
    @Test
    @DisplayName("Test generating a partial update statement with invalid fields to update")
    void testGeneratePartialUpdateStatementErrors() {
        final Set<String> fieldsToUpdate = Collections.emptySet();

        // Test with empty fields
        assertThrows(IllegalArgumentException.class, () ->
                QueryBuilderUtils.generateUpdateStatement(sampleEntity, fieldsToUpdate));

        // Test with only id fields
        final Set<String> fieldsToUpdate2 = Set.of("id", "entity2");
        assertThrows(IllegalArgumentException.class, () ->
                QueryBuilderUtils.generateUpdateStatement(sampleEntity, fieldsToUpdate2));
    }

    /**
     * Tests {@link QueryBuilderUtils#generateDeleteStatement} method.
     */
    @Test
    @DisplayName("Test generating a delete statement")
    void testGenerateDeleteStatement() {
        QueryData queryData = QueryBuilderUtils.generateDeleteStatement(
                new SampleEmbeddedId(sampleEntity.getEmbeddedId().getId(), sampleEntity.getEmbeddedId().getEntity()),
                SampleEntity.class);
        assertNotNull(queryData);
        assertEquals("DELETE FROM sample_entity WHERE (id = ?1 AND entity2 = ?2)", queryData.getQuery());
        assertEquals(2, queryData.getPositionBindings().size());
    }

    /**
     * Tests {@link QueryBuilderUtils#generateMultiInsertStatement} method.
     */
    @Test
    @DisplayName("Test generating a multi-insert statement")
    void testGenerateMultiInsertStatement() {
        // Test with embedded id class
        QueryData resultPair =
                QueryBuilderUtils.generateMultiInsertStatement(List.of(sampleEntity, sampleEntity));

        assertNotNull(resultPair);
        assertEquals("INSERT INTO sample_entity (name, desc, id, entity2) VALUES (?1, ?2, ?3, ?4), " +
                "(?5, ?6, ?7, ?8)", resultPair.getQuery());
        assertEquals(8, resultPair.getPositionBindings().size());

        // Test with idClass
        resultPair = QueryBuilderUtils.generateMultiInsertStatement(List.of(sampleIdClassEntity, sampleIdClassEntity));

        assertNotNull(resultPair);
        assertEquals("INSERT INTO sample_entity (id, entity2, name, desc) VALUES (?1, ?2, ?3, ?4), " +
                "(?5, ?6, ?7, ?8)", resultPair.getQuery());
        assertEquals(8, resultPair.getPositionBindings().size());

    }

    /**
     * Tests {@link QueryBuilderUtils#generateMultiInsertWithSequenceId} method.
     */
    @Test
    @DisplayName("Test generating a multi-insert statement with sequence id")
    void testGenerateMultiInsertWithSequenceId() {
        // Test with embedded id class
        QueryData resultPair =
                QueryBuilderUtils.generateMultiInsertWithSequenceId(List.of(sampleEntity, sampleEntity),
                        "embeddedId.id", null);

        assertNotNull(resultPair);
        assertEquals("INSERT INTO sample_entity (name, desc, id, entity2) VALUES " +
                "(?1, ?2, nextval('sample_seq'), ?3), " +
                "(?4, ?5, nextval('sample_seq'), ?6)", resultPair.getQuery());
        assertEquals(6, resultPair.getPositionBindings().size());

        // Test with idClass
        resultPair = QueryBuilderUtils.generateMultiInsertWithSequenceId(List.of(sampleIdClassEntity,
                sampleIdClassEntity), "id", null);

        assertNotNull(resultPair);
        assertEquals("INSERT INTO sample_entity (id, entity2, name, desc) VALUES " +
                "(nextval('sample_seq'), ?1, ?2, ?3), " +
                "(nextval('sample_seq'), ?4, ?5, ?6)", resultPair.getQuery());
        assertEquals(6, resultPair.getPositionBindings().size());
    }

    /**
     * Tests {@link QueryBuilderUtils#generateOracleInsertAllStatement} method.
     */
    @Test
    @DisplayName("Test insert all statement for oracle database")
    void testGenerateOracleInsertAllStatement() {
        QueryData queryData = QueryBuilderUtils.generateOracleInsertAllStatement(List.of(sampleEntity, sampleEntity),
                List.of(sampleIdClassEntity, sampleIdClassEntity));

        assertNotNull(queryData);
        assertEquals("""
                        INSERT ALL
                        INTO sample_entity (name, desc, id, entity2) VALUES (?1, ?2, ?3, ?4)
                        INTO sample_entity (name, desc, id, entity2) VALUES (?5, ?6, ?7, ?8)
                        INTO sample_entity (id, entity2, name, desc) VALUES (?9, ?10, ?11, ?12)
                        INTO sample_entity (id, entity2, name, desc) VALUES (?13, ?14, ?15, ?16)
                        SELECT * FROM dual""",
                queryData.getQuery());
        assertEquals(16, queryData.getPositionBindings().size());

        assertThrows(IllegalArgumentException.class, QueryBuilderUtils::generateOracleInsertAllStatement);
    }

    /**
     * Tests {@link QueryBuilderUtils#generateOracleInsertAllWithSequenceId} method.
     */
    @Test
    @DisplayName("Test insert all statement for oracle database")
    void testGenerateOracleInsertAllWithSequenceId() {
        QueryData queryData = QueryBuilderUtils.generateOracleInsertAllWithSequenceId(
                List.of(sampleEntity, sampleEntity), "id", null);

        assertNotNull(queryData);
        assertEquals("INSERT INTO sample_entity (id, name, desc, entity2) " +
                        "SELECT sample_seq.nextval, mt.* FROM(SELECT (?1) as name, (?2) as desc, (?3) as entity2 " +
                        "FROM DUAL UNION SELECT (?4) as name, (?5) as desc, (?6) as entity2 FROM DUAL) mt",
                queryData.getQuery());
        assertEquals(6, queryData.getPositionBindings().size());

        List<SampleEntity> list = Collections.emptyList();
        assertThrows(IllegalArgumentException.class, () -> QueryBuilderUtils.generateOracleInsertAllWithSequenceId(list,
                "id", null));
    }

    /**
     * Tests {@link QueryBuilderUtils#clearPositionPlaceholderIndexes} method.
     */
    @Test
    @DisplayName("Test clearing placeholder indexes")
    void testClearPositionPlaceholderIndexes() {
        String query = QueryBuilderUtils.clearPositionPlaceholderIndexes(
                "SELECT * FROM sample_entity WHERE id = ?1 AND entity2 = ?2");
        assertEquals("SELECT * FROM sample_entity WHERE id = ? AND entity2 = ?", query);
    }

    /**
     * Tests {@link QueryBuilderUtils#generateBatchInserts} method.
     */
    @Test
    @DisplayName("Test generating batch inserts")
    void testGenerateBatchInserts() {
        List<BatchData> batchData = QueryBuilderUtils.generateBatchInserts(List.of(sampleEntity, sampleEntity),
                1);
        assertNotNull(batchData);
        assertEquals(2, batchData.size());
        assertEquals("INSERT INTO sample_entity (name, desc, id, entity2) VALUES (?, ?, ?, ?)",
                batchData.get(0).getQuery());
        assertEquals(1, batchData.get(0).getQueriesBindings().size());
        assertEquals(4, batchData.get(0).getQueriesBindings().get(0).size());
    }

    /**
     * Tests {@link QueryBuilderUtils#generateBatchUpdates} method.
     */
    @Test
    @DisplayName("Test generating batch updates")
    void testGenerateBatchUpdates() {
        List<BatchData> batchData = QueryBuilderUtils.generateBatchUpdates(List.of(sampleEntity, sampleEntity),
                1);
        assertNotNull(batchData);
        assertEquals(2, batchData.size());
        assertEquals("UPDATE sample_entity SET (name = ?, desc = ?) WHERE id = ? AND entity2 = ?",
                batchData.get(0).getQuery());
        assertEquals(1, batchData.get(0).getQueriesBindings().size());
        assertEquals(4, batchData.get(0).getQueriesBindings().get(0).size());
    }

    /**
     * Tests {@link QueryBuilderUtils#generateBatchDeletes} method.
     */
    @Test
    @DisplayName("Test generating batch deletes")
    void testGenerateBatchDeletes() {
        // Test with embedded id
        List<BatchData> batchData = QueryBuilderUtils.generateBatchDeletes(List.of(sampleEntity, sampleEntity),
                1);
        assertNotNull(batchData);
        assertEquals(2, batchData.size());
        assertEquals("DELETE FROM sample_entity WHERE id = ? AND entity2 = ?",
                batchData.get(0).getQuery());
        assertEquals(1, batchData.get(0).getQueriesBindings().size());
        assertEquals(2, batchData.get(0).getQueriesBindings().get(0).size());

        // Test with idClass annotation
        batchData = QueryBuilderUtils.generateBatchDeletes(List.of(sampleIdClassEntity, sampleIdClassEntity),
                1);
        assertNotNull(batchData);
        assertEquals(2, batchData.size());
        assertEquals("DELETE FROM sample_entity WHERE id = ? AND entity2 = ?",
                batchData.get(0).getQuery());
        assertEquals(1, batchData.get(0).getQueriesBindings().size());
        assertEquals(2, batchData.get(0).getQueriesBindings().get(0).size());
    }

    /**
     * Tests {@link QueryBuilderUtils#generateBatchInsertsWithSequence} method.
     */
    @Test
    @DisplayName("Test generating batch inserts with sequence id")
    void testGenerateBatchInsertsWithSequence() {
        EntriesWithSequence entriesWithSequence = EntriesWithSequence.builder()
                .entries(List.of(sampleEntity, sampleEntity))
                .sequenceField("id")
                .build();
        List<BatchData> batchData = QueryBuilderUtils.generateBatchInsertsWithSequence(entriesWithSequence,
                1);
        assertNotNull(batchData);
        assertEquals(2, batchData.size());
        assertEquals("INSERT INTO sample_entity (name, desc, id, entity2) VALUES (?, ?, nextval('sample_seq'), ?)",
                batchData.get(0).getQuery());
        assertEquals(1, batchData.get(0).getQueriesBindings().size());
        assertEquals(3, batchData.get(0).getQueriesBindings().get(0).size());
    }
}

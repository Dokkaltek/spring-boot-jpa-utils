package io.github.dokkaltek.util;

import io.github.dokkaltek.exception.EntityReflectionException;
import io.github.dokkaltek.helper.EntityField;
import io.github.dokkaltek.helper.EntriesWithSequence;
import io.github.dokkaltek.helper.QueryData;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.data.util.Pair;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static io.github.dokkaltek.util.EntityReflectionUtils.getEntitySequenceName;
import static io.github.dokkaltek.util.EntityReflectionUtils.getEntityTable;

/**
 * Utility class to build query strings operations.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class QueryBuilderUtils {
    private static final String INSERT_INTO = "INSERT INTO ";

    /**
     * Generate the '(columns) VALUES (values)' sql part of the insert into query and updates the bindings map.
     *
     * @param entity   The entity to get the columns as a {@link StringBuilder} representation of.
     * @param bindings The parameter bindings.
     * @return The generated query fragment.
     * @param <S> The entity type.
     */
    public static <S> StringBuilder getEntityColumnsIntoValues(S entity, Map<Integer, Object> bindings) {
        StringBuilder allColumns = new StringBuilder();
        StringBuilder columnValues = new StringBuilder();

        List<EntityField> columns = EntityReflectionUtils.getEntityColumns(entity);
        for (EntityField column : columns) {

            if (!allColumns.isEmpty()) {
                allColumns.append(", ");
                columnValues.append(", ");
            }

            allColumns.append(column.getColumnName());

            Object columnValue = column.getValue();
            bindings.put(bindings.size() + 1, columnValue);
            columnValues.append("?").append(bindings.size());
        }
        return new StringBuilder("(").append(allColumns).append(") VALUES (").append(columnValues).append(')');
    }

    /**
     * Generate the '(columns) VALUES (values)' sql part of the insert into query and updates the bindings map.
     *
     * @param entity   The entity to get the columns as a {@link StringBuilder} representation of.
     * @param bindings The parameter bindings.
     * @return The generated query fragment.
     * @param <S> The entity type.
     */
    public static <S> StringBuilder getEntityColumnsIntoValuesWithSequence(S entity, Map<Integer, Object> bindings,
                                                                           String idField, String sequenceName) {
        StringBuilder allColumns = new StringBuilder();
        StringBuilder columnValues = new StringBuilder();

        List<EntityField> columns = EntityReflectionUtils.getEntityColumns(entity);
        for (EntityField column : columns) {
            if (column.getFieldName().equals(idField)) {
                allColumns.append(column.getColumnName());
                columnValues.append("nextval('").append(sequenceName).append("')");
                continue;
            }

            if (!allColumns.isEmpty()) {
                allColumns.append(", ");
                columnValues.append(", ");
            }

            allColumns.append(column.getColumnName());

            Object columnValue = column.getValue();
            bindings.put(bindings.size() + 1, columnValue);
            columnValues.append("?").append(bindings.size());
        }
        return new StringBuilder("(").append(allColumns).append(") VALUES (").append(columnValues).append(')');
    }

    /**
     * Generates the '(columns) VALUES (values), (more values) ...' sql part of the insert query and updates the
     * bindings map.
     * @param entries The entries to generate the part of.
     * @param bindings The bindings map.
     * @return The generated query fragment.
     * @param <S> The entity type.
     */
    public static <S> String getMultipleEntityColumnsIntoValues(Collection<S> entries, Map<Integer, Object> bindings) {
        StringBuilder mainQuery = new StringBuilder();
        for (Object entry : entries) {
          if (mainQuery.isEmpty()) {
              mainQuery = getEntityColumnsIntoValues(entry, bindings);
          } else {
              appendEntityColumnsIntoValues(entry, mainQuery, bindings);
          }
        }
        return mainQuery.toString();
    }

    /**
     * Generates the '(columns) VALUES (values), (more values) ...' sql part of the insert query and updates the
     * bindings map.
     * @param entries The entries to generate the part of.
     * @param bindings The bindings map.
     * @return The generated query fragment.
     * @param <S> The entity type.
     */
    public static <S> String getMultipleEntityColumnsIntoValuesWithSequence(Collection<S> entries,
                                                                            Map<Integer, Object> bindings,
                                                                            String idField,
                                                                            String sequenceName) {
        StringBuilder mainQuery = new StringBuilder();
        for (Object entry : entries) {
            if (sequenceName == null) {
                sequenceName = getEntitySequenceName(entry.getClass(), idField);
            }
            if (mainQuery.isEmpty()) {
                mainQuery = getEntityColumnsIntoValuesWithSequence(entry, bindings, idField, sequenceName);
            } else {
                appendEntityColumnsIntoValuesWithSequence(entry, mainQuery, bindings, idField, sequenceName);
            }
        }
        return mainQuery.toString();
    }


    /**
     * Gets the entity columns, placing the id column first.
     *
     * @param entry   The entry to get the columns of.
     * @param idParam The param to place first on the string.
     * @return A string with columns of the entity separated with commas.
     */
    public static String getEntityColumnsWithIdFirst(Object entry, String idParam) {
        StringBuilder allColumns = new StringBuilder();
        String idColumn = idParam;

        List<EntityField> columnsList = EntityReflectionUtils.getEntityColumns(entry);
        for (EntityField field : columnsList) {
            if (!allColumns.isEmpty() && !idParam.equals(field.getFieldName())) {
                allColumns.append(", ");
            } else {
                allColumns.append("{}");
            }

            if (idParam.equals(field.getFieldName())) {
                idColumn = field.getColumnName();
            } else {
                allColumns.append(field.getColumnName());
            }
        }

        return allColumns.replace(0, 2, idColumn).toString();
    }

    /**
     * Generates a list of inserts for a collection of entries.
     * @param entries The entries to generate the batch inserts of.
     * @param batchSize The batch size.
     * @return The generated batch insert queries.
     */
    public static List<QueryData> generateBatchInserts(Collection<?> entries, int batchSize) {
        int batchesSize = (int) Math.ceil(entries.size() / (double) batchSize);

        List<QueryData> queries = new ArrayList<>(batchesSize);
        List<?> entriesAsList = new ArrayList<>(entries);
        for (int i = 0; i < batchesSize; i++) {
            int start = i * batchSize;
            int end = Math.min((i + 1) * batchSize, entries.size());
            List<?> batchEntries = entriesAsList.subList(start, end);
            String entityTable = getEntityTable(batchEntries.get(0));
            for(Object entry : batchEntries) {
                Map<Integer, Object> bindings = new HashMap<>();
                String query = INSERT_INTO + entityTable + getEntityColumnsIntoValues(entry, bindings);
                queries.add(QueryData.builder().query(query).positionBindings(bindings).build());
            }
        }
        return queries;
    }

    /**
     * Generates a list of inserts for a collection of entries.
     * @param entries The entries to generate the batch inserts of.
     * @param batchSize The batch size.
     * @return The generated batch insert queries.
     */
    public static List<QueryData> generateBatchInsertsWithSequence(EntriesWithSequence entries, int batchSize) {

        int batchesSize = (int) Math.ceil(entries.getEntries().size() / (double) batchSize);

        List<QueryData> queries = new ArrayList<>(batchesSize);
        List<?> entriesAsList = new ArrayList<>(entries.getEntries());
        String sequenceName = entries.getSequenceName();
        String sequenceField = entries.getSequenceField();
        for (int i = 0; i < batchesSize; i++) {
            int start = i * batchSize;
            int end = Math.min((i + 1) * batchSize, entries.getEntries().size());
            List<?> batchEntries = entriesAsList.subList(start, end);
            String entityTable = getEntityTable(batchEntries.get(0));
            for(Object entry : batchEntries) {
                Map<Integer, Object> bindings = new HashMap<>();
                if (sequenceName == null) {
                    sequenceName = getEntitySequenceName(entry.getClass(), sequenceField);
                }
                String query = INSERT_INTO + entityTable + getEntityColumnsIntoValuesWithSequence(entry, bindings,
                        sequenceField, sequenceName);
                queries.add(QueryData.builder().query(query).positionBindings(bindings).build());
            }
        }
        return queries;
    }

    /**
     * Generates the "into" part of the insert statement.
     *
     * @param entity   The entity to generate the part of.
     * @param bindings The map with the parameter bindings.
     * @return The "into" part of the insert all statement for an element.
     */
    public static String generateIntoStatement(Object entity, Map<Integer, Object> bindings) {
        return "INTO " + getEntityTable(entity) + getEntityColumnsIntoValues(entity, bindings);
    }

    /**
     * Generates a multi-row insert.
     *
     * @param entries   The entries to generate the insert of.
     * @return The multi-row insert with the bindings.
     */
    public static <S> Pair<String, Map<Integer, Object>> generateMultiInsertStatement(Collection<S> entries) {
        S entity = entries.stream().filter(Objects::nonNull).findFirst()
                .orElseThrow(() -> new EntityReflectionException("Can't get table name of null collection object"));
        List<EntityField> columns = EntityReflectionUtils.getEntityColumns(entity);
        Map<Integer, Object> bindings = new HashMap<>(columns.size() * entries.size());
        String query = INSERT_INTO + getEntityTable(entity) + getMultipleEntityColumnsIntoValues(columns, bindings);
        return Pair.of(query, bindings);
    }

    /**
     * Generates a multi-row insert with a sequence-generated id.
     *
     * @param entries   The entries to generate the insert of.
     * @param idField The id field name.
     * @param sequenceName The sequence name. If empty, the sequence name of the class or above the id field
     *                     will be used.
     * @return The multi-row insert with the bindings.
     */
    public static <S> Pair<String, Map<Integer, Object>> generateMultiInsertWithSequenceId(Collection<S> entries,
                                                                                           String idField,
                                                                                           String sequenceName) {
        S entity = entries.stream().filter(Objects::nonNull).findFirst()
                .orElseThrow(() -> new EntityReflectionException("Can't get table name of null collection object"));
        List<EntityField> columns = EntityReflectionUtils.getEntityColumns(entity);
        Map<Integer, Object> bindings = new HashMap<>(columns.size() * entries.size());
        String query = INSERT_INTO + getEntityTable(entity) + getMultipleEntityColumnsIntoValuesWithSequence(entries,
           bindings, idField, sequenceName);
        return Pair.of(query, bindings);
    }

    /**
     * Generates the insert SQL for an entity.
     *
     * @param entriesToInsert List of entries to insert.
     * @return The generated insert SQL for the entity.
     */
    public static QueryData generateOracleInsertAllSQL(Iterable<?>... entriesToInsert) {
        StringBuilder insertQuery = new StringBuilder();
        Map<Integer, Object> bindings = new HashMap<>();

        if (entriesToInsert != null) {
            for (Iterable<?> entityList : entriesToInsert) {
                for (Object entity : entityList) {
                    insertQuery.append(generateIntoStatement(entity, bindings)).append("\n");
                }
            }
        }

        String query = "INSERT ALL \n" + insertQuery + "SELECT * FROM dual";

        return new QueryData(query, bindings);
    }

    /**
     * Generates the insert all SQL for an entity with a sequence-generated id for an oracle database.
     * @param entryList The entry list to insert.
     * @param idParamToGenerate The id parameter to autogenerate by a table sequence.
     * @return The generated insert SQL for the entity.
     * @param <S> The entity type.
     */
    public static <S> QueryData generateOracleInsertAllWithSequenceId(Iterable<S> entryList, String idParamToGenerate,
                                                                      String sequenceName) {
        StringBuilder insertQuery = new StringBuilder();
        Map<Integer, Object> bindings = new HashMap<>();
        String sequence = sequenceName;

        if (sequence == null || sequence.isEmpty()) {
            sequence = getEntitySequenceName(entryList.iterator().next().getClass(), idParamToGenerate);
        }

        for (S entity : entryList) {
            if (insertQuery.isEmpty()) {
                insertQuery.append(INSERT_INTO).append(getEntityTable(entity))
                        .append(" (").append(getEntityColumnsWithIdFirst(entity, idParamToGenerate))
                        .append(") SELECT ").append(sequence)
                        .append(".nextval, mt.* FROM(");
            } else {
                insertQuery.append(" UNION ");
            }

            insertQuery.append(generateOracleSelectForInsertFromEntryAndSequence(entity, idParamToGenerate, bindings));
        }

        insertQuery.append(") mt");

        return new QueryData(insertQuery.toString(), bindings);
    }

    /**
     * Generates a map of sequences for each entity.
     * @param entityManager The entity manager.
     * @param sequenceQuantityMap A map with the name of the sequence and the number of entities to generate.
     * @return A map with the number sequences for each entity.
     */
    public static Map<String, List<Long>> getOracleSequencesForEntities(EntityManager entityManager,
                                                                        Map<String, Integer> sequenceQuantityMap) {
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
     * Generates a select that returns a table with the values of the entry along with the sequence as id.
     *
     * @param entry The entity to get the columns of.
     * @param idParam The id param that will be generated with a sequence.
     * @return The columns of the entity as a string.
     */
    private static String generateOracleSelectForInsertFromEntryAndSequence(Object entry, String idParam,
                                                                            Map<Integer, Object> bindings) {
        StringBuilder columnValues = new StringBuilder();

        for (EntityField field : EntityReflectionUtils.getEntityColumns(entry)) {
            // Requires the @Column annotation to be in all entity columns we want to get
            if (idParam.equals(field.getFieldName())) {
                continue;
            }

            if (!columnValues.isEmpty()) {
                columnValues.append(", ");
            }

            Object columnValue = field.getValue();

            if (columnValue != null) {
                bindings.put(bindings.size() + 1, columnValue);
                columnValues.append("(?").append(bindings.size()).append(") as ").append(field.getColumnName());
            } else {
                columnValues.append("NULL as").append(field.getColumnName());
            }
        }
        return "SELECT " + columnValues + " FROM DUAL";
    }

    /**
     * Appends the entity columns into the query.
     * @param entry The entity to get the columns of.
     * @param mainQuery The main query.
     * @param bindings The bindings.
     */
    private static void appendEntityColumnsIntoValues(Object entry, StringBuilder mainQuery,
                                                          Map<Integer, Object> bindings) {
        mainQuery.append(", ").append('(');
        List<EntityField> fields = EntityReflectionUtils.getEntityColumns(entry);
        for (int i = 0; i < fields.size(); i++) {
            EntityField field = fields.get(i);
            bindings.put(bindings.size() + 1, field.getValue());
            mainQuery.append('?').append(bindings.size());
            if (i + 1 < fields.size()) {
                mainQuery.append(", ");
            }
        }
        mainQuery.append(')');
    }

    /**
     * Appends the entity columns into the query.
     * @param entry The entity to get the columns of.
     * @param mainQuery The main query.
     * @param bindings The bindings.
     * @param idField The sequence id field.
     * @param sequenceName The name of the sequence to use.
     */
    private static void appendEntityColumnsIntoValuesWithSequence(Object entry, StringBuilder mainQuery,
                                                      Map<Integer, Object> bindings, String idField,
                                                                  String sequenceName) {
        mainQuery.append(", ").append('(');
        List<EntityField> fields = EntityReflectionUtils.getEntityColumns(entry);
        for (int i = 0; i < fields.size(); i++) {
            EntityField field = fields.get(i);
            if (field.getFieldName().equals(idField)) {
                mainQuery.append("nextval('").append(sequenceName).append("')");
            } else {
                bindings.put(bindings.size() + 1, field.getValue());
                mainQuery.append('?').append(bindings.size());
            }
            if (i + 1 < fields.size()) {
                mainQuery.append(", ");
            }
        }
        mainQuery.append(')');
    }
}

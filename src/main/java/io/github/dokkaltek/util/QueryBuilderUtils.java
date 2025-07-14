package io.github.dokkaltek.util;

import io.github.dokkaltek.exception.EntityReflectionException;
import io.github.dokkaltek.helper.BatchData;
import io.github.dokkaltek.helper.EntityField;
import io.github.dokkaltek.helper.EntriesWithSequence;
import io.github.dokkaltek.helper.PrimaryKeyFields;
import io.github.dokkaltek.helper.QueryData;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

import static io.github.dokkaltek.util.EntityReflectionUtils.getEntitySequenceName;
import static io.github.dokkaltek.util.EntityReflectionUtils.getEntityTable;
import static io.github.dokkaltek.util.EntityReflectionUtils.getField;
import static io.github.dokkaltek.util.EntityReflectionUtils.resolveFieldColumnName;

/**
 * Utility class to build query strings operations.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class QueryBuilderUtils {
    private static final String INSERT_INTO = "INSERT INTO ";
    private static final String WHERE_CLAUSE_START = " WHERE ";
    private static final String AND = " AND ";
    private static final String QUESTION_MARK = "?";
    private static final Pattern POSITION_BINDINGS_PATTERN = Pattern.compile("\\?\\d+");

    /**
     * Generate the '(columns) VALUES (values)' sql part of the insert into query and updates the bindings map.
     *
     * @param entity   The entity to get the columns as a {@link StringBuilder} representation of.
     * @param bindings The parameter bindings.
     * @return The generated query fragment.
     * @param <S> The entity type.
     */
    public static <S> StringBuilder getEntityColumnsIntoValues(@NotNull S entity,
                                                               @NotNull Map<Integer, Object> bindings) {
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
        return new StringBuilder(" (").append(allColumns).append(") VALUES (").append(columnValues).append(')');
    }

    /**
     * Generate the '(columns) VALUES (values)' sql part of the insert into query and updates the bindings map.
     *
     * @param entity   The entity to get the columns as a {@link StringBuilder} representation of.
     * @param bindings The parameter bindings.
     * @return The generated query fragment.
     * @param <S> The entity type.
     */
    public static <S> StringBuilder getEntityColumnsIntoValuesWithSequence(@NotNull S entity,
                                                                           @NotNull Map<Integer, Object> bindings,
                                                                           @NotNull String idField,
                                                                           @NotNull String sequenceName) {
        StringBuilder allColumns = new StringBuilder();
        StringBuilder columnValues = new StringBuilder();

        String[] idFieldFrags = idField.split("\\.");

        List<EntityField> columns = EntityReflectionUtils.getEntityColumns(entity);
        for (EntityField column : columns) {
            if (!allColumns.isEmpty()) {
                allColumns.append(", ");
                columnValues.append(", ");
            }

            if (column.getFieldName().equals(idFieldFrags[idFieldFrags.length - 1])) {
                allColumns.append(column.getColumnName());
                columnValues.append("nextval('").append(sequenceName).append("')");
                continue;
            }

            allColumns.append(column.getColumnName());

            Object columnValue = column.getValue();
            bindings.put(bindings.size() + 1, columnValue);
            columnValues.append("?").append(bindings.size());
        }
        return new StringBuilder(" (").append(allColumns).append(") VALUES (").append(columnValues).append(')');
    }

    /**
     * Generates the '(columns) VALUES (values), (more values) ...' sql part of the insert query and updates the
     * bindings map.
     * @param entries The entries to generate the part of.
     * @param bindings The map where the bindings will be stored.
     * @return The generated query fragment.
     * @param <S> The entity type.
     */
    public static <S> StringBuilder getMultipleEntityColumnsIntoValues(@NotNull Collection<S> entries,
                                                                @NotNull Map<Integer, Object> bindings) {
        StringBuilder mainQuery = new StringBuilder();
        for (Object entry : entries) {
          if (mainQuery.isEmpty()) {
              mainQuery = getEntityColumnsIntoValues(entry, bindings);
          } else {
              appendEntityColumnsIntoValues(entry, mainQuery, bindings);
          }
        }
        return mainQuery;
    }

    /**
     * Generates the '(columns) VALUES (values), (more values) ...' sql part of the insert query and updates the
     * bindings map.
     * @param entries The entries to generate the part of.
     * @param bindings The map where the bindings will be stored.
     * @return The generated query fragment.
     * @param <S> The entity type.
     */
    public static <S> StringBuilder getMultipleEntityColumnsIntoValuesWithSequence(@NotNull Collection<S> entries,
                                                                            @NotNull Map<Integer, Object> bindings,
                                                                            @NotNull String idField,
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
        return mainQuery;
    }


    /**
     * Gets the entity columns, placing the id column first.
     *
     * @param entry   The entry to get the columns of.
     * @param idParam The param to place first on the string.
     * @return A string with columns of the entity separated with commas.
     */
    public static StringBuilder getEntityColumnsWithIdFirst(@NotNull Object entry, @NotNull String idParam) {
        StringBuilder allColumns = new StringBuilder("{}");
        String idColumn = idParam;

        List<EntityField> columnsList = EntityReflectionUtils.getEntityColumns(entry);
        for (EntityField field : columnsList) {
            if (!idParam.equals(field.getFieldName())) {
                allColumns.append(", ");
            } else {
                continue;
            }

            if (idParam.equals(field.getFieldName())) {
                idColumn = field.getColumnName();
            } else {
                allColumns.append(field.getColumnName());
            }
        }

        return allColumns.replace(0, 2, idColumn);
    }

    /**
     * Generates the where clause filtering by primary keys in JPQL format.
     * @param idList The ids to filter by.
     * @param entityClass The class that the ids belong to.
     * @param tableAlias The alias used for the table.
     * @return The generated where clause.
     */
    public static <I, T> QueryData getJPQLWhereClauseFilteringByPrimaryKeys(
            @NotNull List<I> idList,
            @NotNull Class<T> entityClass,
            @NotNull String tableAlias) {
        PrimaryKeyFields idClassFields = EntityReflectionUtils.getPrimaryKeyFields(entityClass);
        boolean isEmbeddedId = idClassFields.isEmbeddedId();

        String fieldPrefix = tableAlias + ".";

        // If embedded primary key
        if (isEmbeddedId) {
            fieldPrefix = fieldPrefix + idClassFields.getEmbeddedIdFieldName() + ".";
        }

        // Get the list of entity fields per primary key
        List<List<EntityField>> idFields = getIdFieldsOfIdList(idList, idClassFields.getFields(), fieldPrefix,
                isEmbeddedId);

        // If single primary key
        int idClassFieldsSize = idClassFields.getFields().size();
        if (!isEmbeddedId && idClassFieldsSize == 1) {
            return generateSinglePrimaryKeyWhereClause(idFields);
        } else {
            return generateMultiPrimaryKeyWhereClause(idFields, idClassFieldsSize);
        }
    }

    /**
     * Generates the where clause filtering by primary keys in native format.
     * @param idList The ids to filter by.
     * @param entityClass The class that the ids belong to.
     * @param tableAlias The alias used for the table.
     * @return The generated where clause.
     */
    public static <I, T> QueryData getNativeWhereClauseFilteringByPrimaryKeys(
            @NotNull List<I> idList,
            @NotNull Class<T> entityClass,
            @NotNull String tableAlias) {
        PrimaryKeyFields idClassFields = EntityReflectionUtils.getPrimaryKeyFields(entityClass);
        boolean isEmbeddedId = idClassFields.isEmbeddedId();

        String fieldPrefix = tableAlias + ".";

        // Get the list of entity fields per primary key
        List<List<EntityField>> idFields = getIdFieldsOfIdList(idList, idClassFields.getFields(), fieldPrefix, isEmbeddedId);

        // If single primary key
        int idClassFieldsSize = idClassFields.getFields().size();
        if (!isEmbeddedId && idClassFieldsSize == 1) {
            return generateNativeSinglePrimaryKeyWhereClause(idFields);
        } else {
            return generateNativeMultiPrimaryKeyWhereClause(idFields, idClassFieldsSize);
        }
    }

    /**
     * Generates the "into" part of the insert statement.
     *
     * @param entity   The entity to generate the part of.
     * @param bindings The map where the bindings will be stored.
     * @return The "into" part of the insert all statement for an element.
     */
    public static StringBuilder generateIntoStatement(@NotNull Object entity, @NotNull Map<Integer, Object> bindings) {
        return new StringBuilder("INTO ").append(getEntityTable(entity.getClass()))
                .append(getEntityColumnsIntoValues(entity, bindings));
    }

    /**
     * Generate a native insert statement query.
     * @param entity   The entity to create the insert statement of.
     * @return The generated query data.
     * @param <S> The entity type.
     */
    public static <S> QueryData generateInsertStatement(@NotNull S entity) {
        Map<Integer, Object> bindings = new HashMap<>(entity.getClass().getDeclaredFields().length);
        String entityTable = getEntityTable(entity.getClass());
        String query = INSERT_INTO + entityTable + getEntityColumnsIntoValues(entity, bindings);
        return new QueryData(query, bindings);
    }

    /**
     * Generate a native update statement query.
     *
     * @param entity   The entity to get the columns as a {@link StringBuilder} representation of.
     * @return The generated query fragment.
     * @param <S> The entity type.
     */
    public static <S> QueryData generateUpdateStatement(@NotNull S entity) {
        StringBuilder allColumns = new StringBuilder();
        StringBuilder whereClause = new StringBuilder(WHERE_CLAUSE_START);
        String entityTable = getEntityTable(entity.getClass());
        List<EntityField> columns = EntityReflectionUtils.getEntityColumns(entity);
        Map<Integer, Object> bindings = new HashMap<>(columns.size());
        fillUpdateQuery(allColumns, whereClause, columns, bindings);
        String query = new StringBuilder("UPDATE ").append(entityTable).append(" SET (").append(allColumns).append(')')
                .append(whereClause, 0, whereClause.length() - 5).toString();
        return new QueryData(query, bindings);

    }

    /**
     * Generates a native partial update statement query and updates the bindings map.
     *
     * @param entity   The entity to get the columns as a {@link StringBuilder} representation of.
     * @param fieldsToUpdate The names of the fields to update.
     * @return The generated query fragment.
     * @param <S> The entity type.
     * @throws IllegalArgumentException if there are no fields to update.
     */
    public static <S> QueryData generateUpdateStatement(@NotNull S entity,
                                                            @NotNull Set<String> fieldsToUpdate) {
        if (fieldsToUpdate.isEmpty())
            throw new IllegalArgumentException("Partial update must have at least one field to be updated");

        StringBuilder allColumns = new StringBuilder();
        StringBuilder whereClause = new StringBuilder(WHERE_CLAUSE_START);
        String entityTable = getEntityTable(entity.getClass());
        List<EntityField> columns = EntityReflectionUtils.getEntityColumns(entity).stream()
                .filter(item -> item.isId() || fieldsToUpdate.contains(item.getFieldName())).toList();
        if (columns.stream().allMatch(EntityField::isId))
            throw new IllegalArgumentException("The fields to update must contain at least one non-id field");

        Map<Integer, Object> bindings = new HashMap<>(columns.size());
        fillUpdateQuery(allColumns, whereClause, columns, bindings);
        String query = new StringBuilder("UPDATE ").append(entityTable).append(" SET (").append(allColumns).append(')')
                .append(whereClause, 0, whereClause.length() - 5).toString();

        return new QueryData(query, bindings);
    }

    /**
     * Generates a native delete statement query.
     * @param id The id to create the delete statement of.
     * @param entityClass The entity to delete given the id.
     * @return The generated query data.
     * @param <I> The id type.
     * @param <S> The entity type.
     */
    public static <I, S> QueryData generateDeleteStatement(@NotNull I id, @NotNull Class<S> entityClass) {
        String tableAlias = "ttd";
        QueryData queryPair = getNativeWhereClauseFilteringByPrimaryKeys(List.of(id), entityClass, tableAlias);
        String query = "DELETE FROM " + getEntityTable(entityClass) + queryPair.getQuery();
        return new QueryData(query.replace("ttd.", ""), queryPair.getPositionBindings());
    }

    /**
     * Generates a multi-row insert.
     *
     * @param entries   The entries to generate the insert of.
     * @return The multi-row insert with the bindings.
     */
    public static <S> QueryData generateMultiInsertStatement(@NotNull Collection<S> entries) {
        S entity = entries.stream().filter(Objects::nonNull).findFirst()
                .orElseThrow(() -> new EntityReflectionException("Can't get table name of null collection object"));
        Map<Integer, Object> bindings = new HashMap<>(
                entity.getClass().getDeclaredFields().length * entries.size());
        String query = INSERT_INTO + getEntityTable(entity.getClass()) + getMultipleEntityColumnsIntoValues(entries, bindings);
        return new QueryData(query, bindings);
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
    public static <S> QueryData generateMultiInsertWithSequenceId(
            @NotNull Collection<S> entries,
            @NotNull String idField,
            @NotNull String sequenceName) {
        S entity = entries.stream().filter(Objects::nonNull).findFirst()
                .orElseThrow(() -> new EntityReflectionException("Can't get table name of null collection object"));
        Map<Integer, Object> bindings = new HashMap<>(entity.getClass().getDeclaredFields().length *
                entries.size());
        String query = INSERT_INTO + getEntityTable(entity.getClass()) +
                getMultipleEntityColumnsIntoValuesWithSequence(entries, bindings, idField, sequenceName);
        return new QueryData(query, bindings);
    }

    /**
     * Generates the insert SQL for an entity.
     *
     * @param entriesToInsert List of entries to insert.
     * @return The generated insert SQL for the entity.
     */
    public static QueryData generateOracleInsertAllStatement(@NotNull Iterable<?>... entriesToInsert) {
        if (entriesToInsert.length == 0)
            throw new IllegalArgumentException("Insert all statement must have at least one entry");

        StringBuilder insertQuery = new StringBuilder();
        Map<Integer, Object> bindings = new HashMap<>();

        for (Iterable<?> entityList : entriesToInsert) {
            for (Object entity : entityList) {
                insertQuery.append(generateIntoStatement(entity, bindings)).append("\n");
            }
        }

        String query = "INSERT ALL\n" + insertQuery + "SELECT * FROM dual";

        return new QueryData(query, bindings);
    }

    /**
     * Generates the insert all SQL for an entity with a sequence-generated id for an oracle database.
     * @param entryList The entry list to insert.
     * @param idParamToGenerate The id parameter to autogenerate by a table sequence.
     * @return The generated insert SQL for the entity.
     * @param <S> The entity type.
     */
    public static <S> QueryData generateOracleInsertAllWithSequenceId(@NotNull Iterable<S> entryList,
                                                                      @NotNull String idParamToGenerate,
                                                                      String sequenceName) {
        if (!entryList.iterator().hasNext())
            throw new IllegalArgumentException("Insert all statement must have at least one entry");

        StringBuilder insertQuery = new StringBuilder();
        Map<Integer, Object> bindings = new HashMap<>();
        String sequence = sequenceName;

        if (sequence == null || sequence.isEmpty()) {
            sequence = getEntitySequenceName(entryList.iterator().next().getClass(), idParamToGenerate);
        }

        for (S entity : entryList) {
            if (insertQuery.isEmpty()) {
                insertQuery.append(INSERT_INTO).append(getEntityTable(entity.getClass()))
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
     * Removes the position index from query placeholders, leaving only the '?' placeholders.
     * @param query The query to clear.
     * @return The cleared query.
     */
    public static String clearPositionPlaceholderIndexes(@NotNull String query) {
        return POSITION_BINDINGS_PATTERN.matcher(query).replaceAll(QUESTION_MARK);
    }

    /**
     * Generates a list of inserts for a collection of entries.
     * @param entries The entries to generate the batch inserts of.
     * @param batchSize The batch size.
     * @return The generated batch insert queries.
     */
    public static List<BatchData> generateBatchInserts(@NotNull Collection<?> entries, int batchSize) {
        int batchesSize = (int) Math.ceil(entries.size() / (double) batchSize);

        List<BatchData> queries = new ArrayList<>(batchesSize);
        List<?> entriesAsList = new ArrayList<>(entries);
        for (int i = 0; i < batchesSize; i++) {
            BatchData batchData = new BatchData();
            batchData.setQueriesBindings(new ArrayList<>(batchSize));
            int start = i * batchSize;
            int end = Math.min((i + 1) * batchSize, entries.size());
            List<?> batchEntries = entriesAsList.subList(start, end);
            for(Object entry : batchEntries) {
                QueryData queryData = generateInsertStatement(entry);
                batchData.setQuery(queryData.getQuery());
                batchData.getQueriesBindings().add(queryData.getPositionBindings());
            }
            queries.add(batchData);
        }

        // Remove positional arguments from all queries and make them equal
        if (!queries.isEmpty() && !queries.get(0).getQueriesBindings().isEmpty()) {
            String sampleQuery = queries.get(0).getQuery();
            String clearQuery = clearPositionPlaceholderIndexes(sampleQuery);
            queries.forEach(query -> query.setQuery(clearQuery));
        }

        return queries;
    }

    /**
     * Generates a list of updates for a collection of entries.
     * @param entries The entries to generate the batch updates of.
     * @param batchSize The batch size.
     * @return The generated batch update queries.
     */
    public static List<BatchData> generateBatchUpdates(@NotNull Collection<?> entries, int batchSize) {
        int batchesSize = (int) Math.ceil(entries.size() / (double) batchSize);

        List<BatchData> queries = new ArrayList<>(batchesSize);
        List<?> entriesAsList = new ArrayList<>(entries);
        for (int i = 0; i < batchesSize; i++) {
            int start = i * batchSize;
            int end = Math.min((i + 1) * batchSize, entries.size());
            BatchData batchData = new BatchData();
            batchData.setQueriesBindings(new ArrayList<>(batchSize));
            List<?> batchEntries = entriesAsList.subList(start, end);
            for(Object entry : batchEntries) {
                QueryData queryData = generateUpdateStatement(entry);
                batchData.setQuery(queryData.getQuery());
                batchData.getQueriesBindings().add(queryData.getPositionBindings());
            }
            queries.add(batchData);
        }

        // Remove positional arguments from all queries and make them equal
        if (!queries.isEmpty() && !queries.get(0).getQueriesBindings().isEmpty()) {
            String sampleQuery = queries.get(0).getQuery();
            String clearQuery = clearPositionPlaceholderIndexes(sampleQuery);
            queries.forEach(query -> query.setQuery(clearQuery));
        }

        return queries;
    }

    /**
     * Generates a list of updates for a collection of entries.
     * @param entries The entries to generate the batch updates of.
     * @param batchSize The batch size.
     * @return The generated batch update queries.
     */
    public static List<BatchData> generateBatchDeletes(@NotNull Collection<?> entries, int batchSize) {
        int batchesSize = (int) Math.ceil(entries.size() / (double) batchSize);

        List<BatchData> queries = new ArrayList<>(batchesSize);
        List<?> entriesAsList = new ArrayList<>(entries);
        String entityTable = getEntityTable(entries.iterator().next().getClass());
        for (int i = 0; i < batchesSize; i++) {
            BatchData batchData = new BatchData();
            batchData.setQueriesBindings(new ArrayList<>(batchSize));
            int start = i * batchSize;
            int end = Math.min((i + 1) * batchSize, entries.size());
            List<?> batchEntries = entriesAsList.subList(start, end);

            for(Object entry : batchEntries) {
                List<EntityField> idFields = getIdEntityFields(entry);
                QueryData whereClause = generateNativePkWhereClause(idFields);
                String query = "DELETE FROM " + entityTable + whereClause.getQuery();
                batchData.setQuery(query);
                batchData.getQueriesBindings().add(whereClause.getPositionBindings());
            }
            queries.add(batchData);
        }

        if (!queries.isEmpty() && !queries.get(0).getQueriesBindings().isEmpty()) {
            String sampleQuery = queries.get(0).getQuery();
            String clearQuery = clearPositionPlaceholderIndexes(sampleQuery);
            queries.forEach(query -> query.setQuery(clearQuery));
        }

        return queries;
    }

    /**
     * Generates a list of inserts for a collection of entries.
     * @param entries The entries to generate the batch inserts of.
     * @param batchSize The batch size.
     * @return The generated batch insert queries.
     */
    public static List<BatchData> generateBatchInsertsWithSequence(@NotNull EntriesWithSequence entries,
                                                                   int batchSize) {
        int batchesSize = (int) Math.ceil(entries.getEntries().size() / (double) batchSize);
        int batches = entries.getEntries().size() / batchSize;

        List<BatchData> queries = new ArrayList<>(batches);
        List<?> entriesAsList = new ArrayList<>(entries.getEntries());
        String sequenceName = entries.getSequenceName();
        String sequenceField = entries.getSequenceField();
        for (int i = 0; i < batchesSize; i++) {
            BatchData batchData = new BatchData();
            int start = i * batchSize;
            int end = Math.min((i + 1) * batchSize, entries.getEntries().size());
            List<?> batchEntries = entriesAsList.subList(start, end);
            batchData.setQueriesBindings(new ArrayList<>(batchEntries.size()));
            String entityTable = getEntityTable(batchEntries.get(0).getClass());
            for(Object entry : batchEntries) {
                Map<Integer, Object> bindings = new HashMap<>();
                if (sequenceName == null) {
                    sequenceName = getEntitySequenceName(entry.getClass(), sequenceField);
                }
                String query = INSERT_INTO + entityTable + getEntityColumnsIntoValuesWithSequence(entry, bindings,
                        sequenceField, sequenceName);
                batchData.setQuery(query);
                batchData.getQueriesBindings().add(bindings);
            }
            queries.add(batchData);
        }

        if (!queries.isEmpty() && !queries.get(0).getQueriesBindings().isEmpty()) {
            String sampleQuery = queries.get(0).getQuery();
            String clearQuery = clearPositionPlaceholderIndexes(sampleQuery);
            queries.forEach(query -> query.setQuery(clearQuery));
        }

        return queries;
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
     * @param bindings The map where the bindings will be stored.
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
     * @param bindings The map where the bindings will be stored.
     * @param idField The sequence id field.
     * @param sequenceName The name of the sequence to use.
     */
    private static void appendEntityColumnsIntoValuesWithSequence(Object entry, StringBuilder mainQuery,
                                                      Map<Integer, Object> bindings, String idField,
                                                                  String sequenceName) {
        mainQuery.append(", ").append('(');
        String[] idFieldFrags = idField.split("\\.");
        List<EntityField> fields = EntityReflectionUtils.getEntityColumns(entry);
        for (int i = 0; i < fields.size(); i++) {
            EntityField field = fields.get(i);
            if (field.getFieldName().equals(idFieldFrags[idFieldFrags.length - 1])) {
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

    /**
     * Gets the fields of a list of id entries.
     * @param idList The list of id entries.
     * @param idClassFields The id fields of the object class.
     * @param fieldPrefix The prefix to add to the field names.
     * @param isEmbeddedId If the id is embedded.
     * @return The list of id fields.
     * @param <I> The id type.
     */
    private static <I> List<List<EntityField>> getIdFieldsOfIdList(List<I> idList, List<Field> idClassFields,
                                                                   String fieldPrefix, boolean isEmbeddedId) {
        List<List<EntityField>> idFields = new ArrayList<>(idList.size());
        for (I value : idList) {
            List<EntityField> idEntryFields = new ArrayList<>(idClassFields.size());
            for (Field idClassField : idClassFields) {
                EntityField field = new EntityField();
                field.setFieldName(fieldPrefix + idClassField.getName());
                field.setColumnName(fieldPrefix + resolveFieldColumnName(idClassField));
                Object idValue = value;
                if (isEmbeddedId || idClassFields.size() > 1) {
                    idValue = getField(value, idClassField.getName());
                }
                field.setValue(idValue);
                idEntryFields.add(field);
            }
            idFields.add(idEntryFields);
        }
        return idFields;
    }

    /**
     * Gets the fields of a list of id entries.
     * @param entity The entity to get the fields of
     * @return The list of id fields.
     * @param <S> The entity to get the id fields of.
     */
    private static <S> List<EntityField> getIdEntityFields(S entity) {
        PrimaryKeyFields idClassFields = EntityReflectionUtils.getPrimaryKeyFields(entity.getClass());
        Object embeddedId = null;
        if (idClassFields.isEmbeddedId())
            embeddedId = getField(entity, idClassFields.getEmbeddedIdFieldName());
        List<EntityField> idFields = new ArrayList<>(idClassFields.getFields().size());
        for (Field idClassField : idClassFields.getFields()) {
            EntityField field = new EntityField();
            field.setFieldName(idClassField.getName());
            field.setColumnName(resolveFieldColumnName(idClassField));
            Object idValue;
            if (idClassFields.isEmbeddedId())
                idValue = getField(Objects.requireNonNull(embeddedId), idClassField.getName());
            else
                idValue = getField(entity, idClassField.getName());
            field.setValue(idValue);
            idFields.add(field);
        }
        return idFields;
    }

    /**
     * Generates the where clause filtering by primary keys.
     * @param idFields The id fields.
     * @return The where clause and the bindings.
     */
    private static QueryData generateSinglePrimaryKeyWhereClause(
            List<List<EntityField>> idFields) {
        StringBuilder whereClause = new StringBuilder(WHERE_CLAUSE_START);
        List<EntityField> allEntries = idFields.stream().flatMap(java.util.List::stream).toList();
        String fieldName = allEntries.get(0).getFieldName();

        Map<Integer, Object> bindings = new HashMap<>(allEntries.size());

        if (allEntries.size() == 1) {
            bindings.put(1, allEntries.get(0).getValue());
            return new QueryData(whereClause.append(fieldName).append("= ?1").toString(), bindings);
        }

        whereClause.append(fieldName).append(" IN (");
        for (EntityField field : allEntries) {
            bindings.put(bindings.size() + 1, field.getValue());
            whereClause.append("?").append(bindings.size()).append(", ");
        }

        return new QueryData(whereClause.substring(0, whereClause.length() - 2) + ")", bindings);
    }

    /**
     * Generates the where clause filtering by primary keys.
     * @param idFields The id fields.
     * @return The where clause and the bindings.
     */
    private static QueryData generateNativeSinglePrimaryKeyWhereClause(
            List<List<EntityField>> idFields) {
        StringBuilder whereClause = new StringBuilder(WHERE_CLAUSE_START);
        List<EntityField> allEntries = idFields.stream().flatMap(java.util.List::stream).toList();
        String fieldName = allEntries.get(0).getColumnName();

        Map<Integer, Object> bindings = new HashMap<>(allEntries.size());

        if (allEntries.size() == 1) {
            bindings.put(1, allEntries.get(0).getValue());
            return new QueryData(whereClause.append(fieldName).append("= ?1").toString(), bindings);
        }

        whereClause.append(fieldName).append(" IN (");
        for (EntityField field : allEntries) {
            bindings.put(bindings.size() + 1, field.getValue());
            whereClause.append("?").append(bindings.size()).append(", ");
        }

        return new QueryData(whereClause.substring(0, whereClause.length() - 2) + ")", bindings);
    }

    /**
     * Generates the where clause filtering by primary keys for a multi-primary key entity.
     * @param idFields The id fields.
     * @param idClassFieldsSize The number of fields in the id class.
     * @return The where clause and the bindings.
     */
    private static QueryData generateMultiPrimaryKeyWhereClause(
            List<List<EntityField>> idFields, int idClassFieldsSize) {
        StringBuilder whereClause = new StringBuilder(WHERE_CLAUSE_START);
        Map<Integer, Object> bindings = new HashMap<>(idFields.size() * idClassFieldsSize);
        StringBuilder idClauses = new StringBuilder();
        StringBuilder entryWhereClause = new StringBuilder();
        for (List<EntityField> entryFields : idFields) {
            if (!entryWhereClause.isEmpty()) {
                idClauses.append(" OR ");
            }
            entryWhereClause = new StringBuilder();
            for (EntityField field : entryFields) {
                if (entryWhereClause.isEmpty()) {
                    entryWhereClause.append("(");
                } else {
                    entryWhereClause.append(AND);
                }

                bindings.put(bindings.size() + 1, field.getValue());
                entryWhereClause.append(field.getFieldName()).append(" = ?").append(bindings.size());
            }
            entryWhereClause.append(')');
            idClauses.append(entryWhereClause);
        }
        return new QueryData(whereClause.append(idClauses).toString(), bindings);
    }

    /**
     * Generates the where clause filtering by primary keys for a multi-primary key entity.
     * @param idFields The id fields.
     * @return The where clause and the bindings.
     */
    private static QueryData generateNativeMultiPrimaryKeyWhereClause(
            List<List<EntityField>> idFields, int idClassFieldsSize) {
        StringBuilder whereClause = new StringBuilder(WHERE_CLAUSE_START);
        Map<Integer, Object> bindings = new HashMap<>(idFields.size() * idClassFieldsSize);
        StringBuilder idClauses = new StringBuilder();
        StringBuilder entryWhereClause = new StringBuilder();
        for (List<EntityField> entryFields : idFields) {
            if (!entryWhereClause.isEmpty()) {
                idClauses.append(" OR ");
            }
            entryWhereClause = new StringBuilder();
            for (EntityField field : entryFields) {
                if (entryWhereClause.isEmpty()) {
                    entryWhereClause.append("(");
                } else {
                    entryWhereClause.append(AND);
                }

                bindings.put(bindings.size() + 1, field.getValue());
                entryWhereClause.append(field.getColumnName()).append(" = ?").append(bindings.size());
            }
            entryWhereClause.append(')');
            idClauses.append(entryWhereClause);
        }
        return new QueryData(whereClause.append(idClauses).toString(), bindings);
    }

    /**
     * Generates the where clause filtering by primary keys.
     * @param idFields The id fields.
     * @return The where clause and the bindings.
     */
    private static QueryData generateNativePkWhereClause(
            List<EntityField> idFields) {
        StringBuilder whereClause = new StringBuilder(WHERE_CLAUSE_START);

        Map<Integer, Object> bindings = new HashMap<>(idFields.size());

        if (idFields.size() == 1) {
            bindings.put(1, idFields.get(0).getValue());
            return new QueryData(whereClause.append(idFields.get(0).getColumnName()).append("= ?1").toString(), bindings);
        }

        StringBuilder paramsClause = new StringBuilder();
        for (EntityField field : idFields) {
            if (!paramsClause.isEmpty()) {
                paramsClause.append(AND);
            }
            paramsClause.append(field.getColumnName()).append(" = ?");
            bindings.put(bindings.size() + 1, field.getValue());
            paramsClause.append(bindings.size());
        }

        whereClause.append(paramsClause);

        return new QueryData(whereClause.toString(), bindings);
    }

    /**
     * Sets the columns and where clause for the update query.
     * @param columns The StringBuilder where the columns will be stored.
     * @param whereClause The StringBuilder where the where clause will be stored.
     * @param fields The entity fields.
     * @param bindings The map where the bindings will be stored.
     */
    private static void fillUpdateQuery(StringBuilder columns, StringBuilder whereClause, List<EntityField> fields,
                                        Map<Integer, Object> bindings) {
        for (EntityField column : fields) {
            Object columnValue = column.getValue();
            if (column.isId()) {
                bindings.put(bindings.size() + 1, columnValue);
                whereClause.append(column.getColumnName()).append(" = ?").append(bindings.size()).append(AND);
                continue;
            }

            if (!columns.isEmpty()) {
                columns.append(", ");
            }

            bindings.put(bindings.size() + 1, columnValue);
            columns.append(column.getColumnName()).append(" = ?").append(bindings.size());
        }
    }
}

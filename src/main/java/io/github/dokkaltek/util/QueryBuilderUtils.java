package io.github.dokkaltek.util;

import io.github.dokkaltek.exception.BatchOperationException;
import io.github.dokkaltek.exception.EntityReflectionException;
import io.github.dokkaltek.helper.BatchData;
import io.github.dokkaltek.helper.EntityField;
import io.github.dokkaltek.helper.EntriesWithSequence;
import io.github.dokkaltek.helper.PrimaryKeyFields;
import io.github.dokkaltek.helper.QueryData;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
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
     * Generate the '(columns) VALUES (values)' sql part of a native insert into query and updates the bindings map.
     *
     * @param entity   The entity to get the columns as a {@link StringBuilder} representation of.
     * @param bindings The parameter bindings.
     * @param isNative Whether the query is native or not.
     * @param <S>      The entity type.
     * @return The generated query fragment.
     */
    public static <S> StringBuilder getEntityColumnsIntoValues(@NotNull S entity,
                                                               @NotNull Map<Integer, Object> bindings,
                                                               boolean isNative) {
        StringBuilder allColumns = new StringBuilder();
        StringBuilder columnValues = new StringBuilder();

        List<EntityField> columns = EntityReflectionUtils.getEntityColumns(entity);
        for (EntityField column : columns) {

            if (!allColumns.isEmpty()) {
                allColumns.append(", ");
                columnValues.append(", ");
            }

            allColumns.append(isNative ? column.getColumnName() : column.getFieldName());

            Object columnValue = column.getValue();
            bindings.put(bindings.size() + 1, columnValue);
            columnValues.append("?").append(bindings.size());
        }
        return new StringBuilder(" (").append(allColumns).append(") VALUES (").append(columnValues).append(')');
    }

    /**
     * Generate the '(columns) VALUES (values)' sql part of a native insert into query and updates the bindings map.
     *
     * @param entity   The entity to get the columns as a {@link StringBuilder} representation of.
     * @param bindings The parameter bindings.
     * @param <S>      The entity type.
     * @return The generated query fragment.
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

            allColumns.append(column.getColumnName());

            if (column.getFieldName().equals(idFieldFrags[idFieldFrags.length - 1])) {
                columnValues.append(sequenceName).append(".nextval");
                continue;
            }

            Object columnValue = column.getValue();
            bindings.put(bindings.size() + 1, columnValue);
            columnValues.append("?").append(bindings.size());
        }
        return new StringBuilder(" (").append(allColumns).append(") VALUES (").append(columnValues).append(')');
    }

    /**
     * Generates the '(columns) VALUES (values), (more values) ...' sql part of a native insert query and updates the
     * bindings map.
     *
     * @param entries  The entries to generate the part of.
     * @param isNative Whether the query is native or not.
     * @param <S>      The entity type.
     * @return The generated query fragment.
     */
    public static <S> QueryData getMultipleEntityColumnsIntoValues(@NotNull @NotEmpty Collection<S> entries,
                                                                   boolean isNative) {
        StringBuilder mainQuery = new StringBuilder();
        S entity = entries.iterator().next();
        Map<Integer, Object> bindings = new HashMap<>(entity.getClass().getDeclaredFields().length);
        for (Object entry : entries) {
            if (mainQuery.isEmpty()) {
                mainQuery = getEntityColumnsIntoValues(entry, bindings, isNative);
            } else {
                appendEntityColumnsIntoValues(entry, mainQuery, bindings);
            }
        }
        return new QueryData(mainQuery.toString(), bindings);
    }

    /**
     * Generates the '(columns) VALUES (values), (more values) ...' sql part of the insert query and updates the
     * bindings map.
     *
     * @param entries      The entries to generate the part of.
     * @param idField      The id field of the entity.
     * @param sequenceName The name of the sequence to use.
     * @param <S>          The entity type.
     * @return The generated query fragment.
     * @throws BatchOperationException If the passed collection is empty.
     */
    public static <S> QueryData getMultipleEntityColumnsIntoValuesWithSequence(@NotNull @NotEmpty Collection<S> entries,
                                                                               @NotNull String idField,
                                                                               String sequenceName) {
        StringBuilder mainQuery = new StringBuilder();
        S entity = entries.iterator().next();
        Map<Integer, Object> bindings = new HashMap<>(entity.getClass().getDeclaredFields().length *
                entries.size());
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
        return new QueryData(mainQuery.toString(), bindings);
    }

    /**
     * Gets the entity columns joined by commas.
     *
     * @param entry    The entry to get the columns of.
     * @param isNative Whether the query is native or not.
     * @return A string with columns of the entity separated with commas.
     */
    public static StringBuilder getJoinedEntityColumns(@NotNull Object entry, boolean isNative) {
        StringBuilder allColumns = new StringBuilder();

        List<EntityField> columnsList = EntityReflectionUtils.getEntityColumns(entry);
        for (EntityField field : columnsList) {
            if (!allColumns.isEmpty())
                allColumns.append(", ");
            allColumns.append(isNative ? field.getColumnName() : field.getFieldName());
        }

        return allColumns;
    }

    /**
     * Gets the entity columns, placing the id column first. Aimed for native queries.
     *
     * @param entry   The entry to get the columns of.
     * @param idParam The param to place first on the string.
     * @return A string with columns of the entity separated with commas.
     */
    public static StringBuilder getJoinedEntityColumnsWithIdFirst(@NotNull Object entry, @NotNull String idParam) {
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
     *
     * @param idList      The ids to filter by.
     * @param entityClass The class that the ids belong to.
     * @param tableAlias  The alias used for the table.
     * @return The generated where clause.
     */
    public static <I, T> QueryData getWhereClauseFilteringByPrimaryKeys(
            @NotNull List<I> idList,
            @NotNull Class<T> entityClass,
            @NotNull String tableAlias,
            boolean isNative) {
        PrimaryKeyFields idClassFields = EntityReflectionUtils.getPrimaryKeyFields(entityClass);
        boolean isEmbeddedId = idClassFields.isEmbeddedId();

        String fieldPrefix = tableAlias + ".";

        // If embedded primary key
        if (isEmbeddedId && !isNative) {
            fieldPrefix = fieldPrefix + idClassFields.getEmbeddedIdFieldName() + ".";
        }

        // Get the list of entity fields per primary key
        List<List<EntityField>> idFields = getIdFieldsOfIdList(idList, idClassFields.getFields(), fieldPrefix,
                isEmbeddedId);

        // If single primary key
        int idClassFieldsSize = idClassFields.getFields().size();
        if (!isEmbeddedId && idClassFieldsSize == 1) {
            return generateSinglePrimaryKeyWhereClause(idFields, isNative);
        } else {
            return generateMultiPrimaryKeyWhereClause(idFields, idClassFieldsSize, isNative);
        }
    }

    /**
     * Generates the "SET" and "WHERE" clauses for the update query.
     *
     * @param fields   The entity fields.
     * @param isNative True if the query is native, false otherwise.
     * @return The columns and where clause.
     */
    public static QueryData generateUpdateColumnsAndWhereClause(List<EntityField> fields, boolean isNative) {
        StringBuilder columns = new StringBuilder();
        StringBuilder whereClause = new StringBuilder(WHERE_CLAUSE_START);
        Map<Integer, Object> bindings = new HashMap<>(fields.size());
        List<EntityField> orderedFields;
        if (!isNative)
            orderedFields = fields.stream().map(field -> {
                field.setColumnName(field.getFieldName());
                return field;
            }).sorted(Comparator.comparing(EntityField::isId)).toList();
        else
            orderedFields = fields.stream().sorted(Comparator.comparing(EntityField::isId)).toList();
        for (EntityField column : orderedFields) {
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

        String query = new StringBuilder(" SET ").append(columns)
                .append(whereClause, 0, whereClause.length() - 5).toString();
        return new QueryData(query, bindings);
    }

    /**
     * Generates the "into" part of a native insert statement.
     *
     * @param entity   The entity to generate the part of.
     * @param bindings The map where the bindings will be stored.
     * @param isNative True if the query is native, false otherwise.
     * @return The "into" part of the insert all statement for an element.
     */
    public static StringBuilder generateIntoStatement(@NotNull Object entity, @NotNull Map<Integer, Object> bindings,
                                                      boolean isNative) {
        return new StringBuilder("INTO ")
                .append(isNative ? getEntityTable(entity.getClass()) : entity.getClass().getSimpleName())
                .append(getEntityColumnsIntoValues(entity, bindings, isNative));
    }

    /**
     * Generate a insert statement query.
     *
     * @param entity   The entity to create the insert statement of.
     * @param isNative True if the query is native, false otherwise.
     * @param <S>      The entity type.
     * @return The generated query data.
     */
    public static <S> QueryData generateInsertStatement(@NotNull S entity, boolean isNative) {
        Map<Integer, Object> bindings = new HashMap<>(entity.getClass().getDeclaredFields().length);
        String entityTable = isNative ? getEntityTable(entity.getClass()) : entity.getClass().getSimpleName();
        String query = INSERT_INTO + entityTable + getEntityColumnsIntoValues(entity, bindings, isNative);
        return new QueryData(query, bindings);
    }

    /**
     * Generate a update statement query.
     *
     * @param entity   The entity to get the columns as a {@link StringBuilder} representation of.
     * @param isNative True if the query is native, false otherwise.
     * @param <S>      The entity type.
     * @return The generated query fragment.
     */
    public static <S> QueryData generateUpdateStatement(@NotNull S entity, boolean isNative) {
        String entityTable = isNative ? getEntityTable(entity.getClass()) : entity.getClass().getSimpleName();
        List<EntityField> columns = EntityReflectionUtils.getEntityColumns(entity);
        QueryData columnsAndWhereClause = generateUpdateColumnsAndWhereClause(columns, isNative);
        String query = "UPDATE " + entityTable + columnsAndWhereClause.getQuery();
        return new QueryData(query, columnsAndWhereClause.getPositionBindings());

    }

    /**
     * Generates a partial update statement query and updates the bindings map.
     *
     * @param entity         The entity to get the columns as a {@link StringBuilder} representation of.
     * @param fieldsToUpdate The names of the fields to update.
     * @param isNative       True if the query is native, false otherwise.
     * @param <S>            The entity type.
     * @return The generated query fragment.
     * @throws IllegalArgumentException if there are no fields to update.
     */
    public static <S> QueryData generateUpdateStatement(@NotNull S entity,
                                                        @NotNull Set<String> fieldsToUpdate,
                                                        boolean isNative) {
        if (fieldsToUpdate.isEmpty())
            throw new IllegalArgumentException("Partial update must have at least one field to be updated");

        String entityTable = isNative ? getEntityTable(entity.getClass()) : entity.getClass().getSimpleName();
        List<EntityField> columns = EntityReflectionUtils.getEntityColumns(entity).stream()
                .filter(item -> item.isId() || fieldsToUpdate.contains(item.getFieldName())).toList();
        if (columns.stream().allMatch(EntityField::isId))
            throw new IllegalArgumentException("The fields to update must contain at least one non-id field");

        QueryData columnsAndWhereClause = generateUpdateColumnsAndWhereClause(columns, isNative);
        String query = "UPDATE " + entityTable + columnsAndWhereClause.getQuery();

        return new QueryData(query, columnsAndWhereClause.getPositionBindings());
    }

    /**
     * Generates a delete statement query.
     *
     * @param id          The id to create the delete statement of.
     * @param entityClass The entity to delete given the id.
     * @param isNative    True if the query is native, false otherwise.
     * @param <I>         The id type.
     * @param <S>         The entity type.
     * @return The generated query data.
     */
    public static <I, S> QueryData generateDeleteStatement(@NotNull I id, @NotNull Class<S> entityClass,
                                                           boolean isNative) {
        String tableAlias = "ttd";
        String entityTable = isNative ? getEntityTable(entityClass) : entityClass.getSimpleName();
        QueryData queryPair = getWhereClauseFilteringByPrimaryKeys(List.of(id), entityClass, tableAlias, isNative);
        String query = "DELETE FROM " + entityTable + queryPair.getQuery();
        return new QueryData(query.replace("ttd.", ""), queryPair.getPositionBindings());
    }

    /**
     * Generates a multi-row insert.
     *
     * @param entries The entries to generate the insert of.
     * @return The multi-row insert with the bindings.
     */
    public static <S> QueryData generateMultiInsertStatement(@NotNull Collection<S> entries, boolean isNative) {
        S entity = entries.stream().filter(Objects::nonNull).findFirst()
                .orElseThrow(() -> new EntityReflectionException("Can't get table name of null collection object"));
        String entityTable = isNative ? getEntityTable(entity.getClass()) : entity.getClass().getSimpleName();
        QueryData queryData = getMultipleEntityColumnsIntoValues(entries, isNative);
        String query = INSERT_INTO + entityTable + queryData.getQuery();
        return new QueryData(query, queryData.getPositionBindings());
    }

    /**
     * Generates a multi-row insert with a sequence-generated id.
     *
     * @param entries      The entries to generate the insert of.
     * @param idField      The id field name.
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
        QueryData queryData = getMultipleEntityColumnsIntoValuesWithSequence(entries, idField, sequenceName);
        String query = INSERT_INTO + getEntityTable(entity.getClass()) +
                queryData.getQuery();
        return new QueryData(query, queryData.getPositionBindings());
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
                insertQuery.append(generateIntoStatement(entity, bindings, true)).append(" ");
            }
        }

        String query = "INSERT ALL " + insertQuery + "SELECT * FROM dual";

        return new QueryData(query, bindings);
    }

    /**
     * Generates the insert all SQL for an entity with a sequence-generated id for an oracle database.
     *
     * @param entryList         The entry list to insert.
     * @param idParamToGenerate The id parameter to autogenerate by a table sequence.
     * @param <S>               The entity type.
     * @return The generated insert SQL for the entity.
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
                        .append(" (").append(getJoinedEntityColumnsWithIdFirst(entity, idParamToGenerate))
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
     *
     * @param query The query to clear.
     * @return The cleared query.
     */
    public static String clearPositionPlaceholderIndexes(@NotNull String query) {
        return POSITION_BINDINGS_PATTERN.matcher(query).replaceAll(QUESTION_MARK);
    }

    /**
     * Generates a list of inserts for a collection of entries.
     *
     * @param entries   The entries to generate the batch inserts of.
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
            for (Object entry : batchEntries) {
                QueryData queryData = generateInsertStatement(entry, true);
                batchData.setQuery(queryData.getQuery());
                batchData.getQueriesBindings().add(queryData.getPositionBindings());
            }
            queries.add(batchData);
        }

        // Remove positional arguments from all queries and make them equal
        if (!queries.isEmpty() && !queries.get(0).getQueriesBindings().isEmpty()) {
            String sampleQuery = queries.get(0).getQuery();
            queries.forEach(query -> query.setQuery(sampleQuery));
        }

        return queries;
    }

    /**
     * Generates a list of updates for a collection of entries.
     *
     * @param entries   The entries to generate the batch updates of.
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
            for (Object entry : batchEntries) {
                QueryData queryData = generateUpdateStatement(entry, true);
                batchData.setQuery(queryData.getQuery());
                batchData.getQueriesBindings().add(queryData.getPositionBindings());
            }
            queries.add(batchData);
        }

        // Remove positional arguments from all queries and make them equal
        if (!queries.isEmpty() && !queries.get(0).getQueriesBindings().isEmpty()) {
            String sampleQuery = queries.get(0).getQuery();
            queries.forEach(query -> query.setQuery(sampleQuery));
        }

        return queries;
    }

    /**
     * Generates a list of deletes for a collection of entries.
     *
     * @param entryIds  The entry id list to generate the batch deletes of.
     * @param batchSize The batch size.
     * @return The generated batch delete queries.
     */
    public static List<BatchData> generateBatchDeletes(@NotNull Collection<?> entryIds, Class<?> entityClass,
                                                       int batchSize) {
        int batchesSize = (int) Math.ceil(entryIds.size() / (double) batchSize);

        List<BatchData> queries = new ArrayList<>(batchesSize);
        List<?> entriesAsList = new ArrayList<>(entryIds);
        String entityTable = getEntityTable(entityClass);
        for (int i = 0; i < batchesSize; i++) {
            BatchData batchData = new BatchData();
            batchData.setQueriesBindings(new ArrayList<>(batchSize));
            int start = i * batchSize;
            int end = Math.min((i + 1) * batchSize, entryIds.size());
            List<?> batchEntries = entriesAsList.subList(start, end);

            for (Object entryId : batchEntries) {
                String tableAlias = "ttd";
                QueryData whereClause = getWhereClauseFilteringByPrimaryKeys(List.of(entryId), entityClass,
                        tableAlias, true);
                String query = "DELETE FROM " + entityTable + " " + tableAlias + whereClause.getQuery();
                batchData.setQuery(query);
                batchData.getQueriesBindings().add(whereClause.getPositionBindings());
            }
            queries.add(batchData);
        }

        if (!queries.isEmpty() && !queries.get(0).getQueriesBindings().isEmpty()) {
            String sampleQuery = queries.get(0).getQuery();
            queries.forEach(query -> query.setQuery(sampleQuery));
        }

        return queries;
    }

    /**
     * Generates a list of inserts for a collection of entries.
     *
     * @param entries   The entries to generate the batch inserts of.
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
            for (Object entry : batchEntries) {
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
            queries.forEach(query -> query.setQuery(sampleQuery));
        }

        return queries;
    }

    /**
     * Generates a select that returns a table with the values of the entry along with the sequence as id.
     *
     * @param entry   The entity to get the columns of.
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
                columnValues.append("NULL as ").append(field.getColumnName());
            }
        }
        return "SELECT " + columnValues + " FROM DUAL";
    }

    /**
     * Appends the entity columns into the query.
     *
     * @param entry     The entity to get the columns of.
     * @param mainQuery The main query.
     * @param bindings  The map where the bindings will be stored.
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
     *
     * @param entry        The entity to get the columns of.
     * @param mainQuery    The main query.
     * @param bindings     The map where the bindings will be stored.
     * @param idField      The sequence id field.
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
                mainQuery.append(sequenceName).append(".nextval");
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
     *
     * @param idList        The list of id entries.
     * @param idClassFields The id fields of the object class.
     * @param fieldPrefix   The prefix to add to the field names.
     * @param isEmbeddedId  If the id is embedded.
     * @param <I>           The id type.
     * @return The list of id fields.
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
     * Generates the where clause filtering by primary keys.
     *
     * @param idFields The id fields.
     * @return The where clause and the bindings.
     */
    private static QueryData generateSinglePrimaryKeyWhereClause(
            List<List<EntityField>> idFields, boolean isNative) {
        StringBuilder whereClause = new StringBuilder(WHERE_CLAUSE_START);
        List<EntityField> allEntries = idFields.stream().flatMap(java.util.List::stream).toList();
        String fieldName = isNative ? allEntries.get(0).getColumnName() : allEntries.get(0).getFieldName();

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
     *
     * @param idFields          The id fields.
     * @param idClassFieldsSize The number of fields in the id class.
     * @return The where clause and the bindings.
     */
    private static QueryData generateMultiPrimaryKeyWhereClause(
            List<List<EntityField>> idFields, int idClassFieldsSize, boolean isNative) {
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
                entryWhereClause.append(isNative ? field.getColumnName() : field.getFieldName())
                        .append(" = ?").append(bindings.size());
            }
            entryWhereClause.append(')');
            idClauses.append(entryWhereClause);
        }
        return new QueryData(whereClause.append(idClauses).toString(), bindings);
    }
}

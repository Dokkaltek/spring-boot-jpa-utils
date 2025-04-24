package io.github.dokkaltek.util;

import io.github.dokkaltek.helper.EntityField;
import io.github.dokkaltek.helper.QueryData;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.github.dokkaltek.util.EntityReflectionUtils.getEntitySequenceName;
import static io.github.dokkaltek.util.EntityReflectionUtils.getEntityTable;

/**
 * Utility class to build query strings operations.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class QueryBuilderUtils {

    /**
     * Generate the '(columns) VALUES (values)' sql part of the insert into query and updates the bindings map.
     *
     * @param entity   The entity to get the columns as a {@link StringBuilder} representation of.
     * @param bindings The parameter bindings.
     * @return The generated query fragment.
     */
    public static StringBuilder getEntityColumnsIntoValues(Object entity, Map<Integer, Object> bindings) {
        var allColumns = new StringBuilder();
        var columnValues = new StringBuilder();

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
     * Gets the entity columns, placing the id column first.
     *
     * @param entry   The entry to get the columns of.
     * @param idParam The param to place first on the string.
     * @return A string with columns of the entity separated with commas.
     */
    public static String getEntityColumnsWithIdFirst(Object entry, String idParam) {
        var allColumns = new StringBuilder();
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
     * Generates the insert SQL for an entity.
     *
     * @param entryList The main entry list to insert.
     * @param extraEntities Other list of entries to insert.
     * @return The generated insert SQL for the entity.
     */
    public static <S> QueryData generateOracleInsertAllSQL(Iterable<S> entryList,
                                                           Iterable<?>... extraEntities) {
        StringBuilder insertQuery = new StringBuilder();
        Map<Integer, Object> bindings = new HashMap<>();
        for (S entity : entryList) {
            insertQuery.append(generateIntoStatement(entity, bindings)).append("\n");
        }

        if (extraEntities != null) {
            for (Iterable<?> entityList : extraEntities) {
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
            sequence = getEntitySequenceName(entryList.iterator().next());
        }

        for (S entity : entryList) {
            if (insertQuery.isEmpty()) {
                insertQuery.append("INSERT INTO ").append(getEntityTable(entity))
                        .append(" (").append(getEntityColumnsWithIdFirst(entity, idParamToGenerate))
                        .append(") SELECT ").append(sequence)
                        .append(".nextval, mt.* FROM(");
            } else {
                insertQuery.append(" UNION ");
            }

            insertQuery.append(generateSelectFromEntryAndSequence(entity, idParamToGenerate, bindings));
        }

        insertQuery.append(") mt");

        return new QueryData(insertQuery.toString(), bindings);
    }

    /**
     * Generates a select that returns a table with the values of the entry along with the sequence as id.
     *
     * @param entry The entity to get the columns of.
     * @param idParam The id param that will be generated with a sequence.
     * @return The columns of the entity as a string.
     */
    private static String generateSelectFromEntryAndSequence(Object entry, String idParam,
                                                             Map<Integer, Object> bindings) {
        var columnValues = new StringBuilder();

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
}

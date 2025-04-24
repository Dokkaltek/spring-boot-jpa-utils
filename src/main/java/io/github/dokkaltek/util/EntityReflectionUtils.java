package io.github.dokkaltek.util;

import io.github.dokkaltek.exception.EntityReflectionException;
import io.github.dokkaltek.helper.EntityField;
import jakarta.persistence.Column;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Utility class to reflect on database entities.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class EntityReflectionUtils {
    private static final Pattern CAPS_PATTERN = Pattern.compile("(?<![\\-_A-Z])[A-Z]+");

    /**
     * Gets a field from an object.
     * @param object The object to get the field from.
     * @param fieldName The name of the field.
     * @return The value of the field.
     * @param <T> The type of the field.
     */
    public static <T> T getField(Object object, String fieldName) {
        try {
            return (T) getClassField(object.getClass(), fieldName).get(object);
        } catch (IllegalAccessException e) {
            throw new EntityReflectionException(e);
        }
    }

    /**
     * Get all the declared fields of the class.
     *
     * @param entityClass The class to get the fields.
     * @return A List of the fields.
     */
    public static List<Field> retrieveClassFields(Class<?> entityClass) {
        List<Field> fields = new ArrayList<>(Arrays.asList(entityClass.getDeclaredFields()));
        // Add all fields of super classes
        while (entityClass.getSuperclass() != Object.class) {
            entityClass = entityClass.getSuperclass();
            fields.addAll(Arrays.asList(entityClass.getDeclaredFields()));
        }
        return fields;
    }

    /**
     * Get the entity table name.
     *
     * @param entity An instance of the entity to get the table of.
     * @return The name of the table of the entity.
     */
    public static String getEntityTable(Object entity) {
        Table entityTable = entity.getClass().getAnnotation(Table.class);
        if (entityTable == null) {
            throw new EntityReflectionException("Entity " + entity.getClass().getCanonicalName() +
                    " doesn't have a table annotation");
        }

        // If the table annotation doesn't have the value of the table name, use the class name
        if (entityTable.name().isEmpty()) {
            return entity.getClass().getSimpleName();
        }

        return entityTable.name();
    }

    /**
     * Gets the list of column names and their value.
     * @param entity The entity to get the columns from.
     * @return A map with the column name as key and the value of the column as value.
     */
    public static List<EntityField> getEntityColumns(Object entity) {
        List<Field> classFields = EntityReflectionUtils.retrieveClassFields(entity.getClass());
        List<EntityField> columnsList = new ArrayList<>(classFields.size());
        try {
            for (Field field : classFields) {
                EntityField entityField = EntityField.builder()
                        .fieldName(field.getName())
                        .value(field.get(entity))
                        .build();
                String columnName = field.getName();
                boolean skipColumnCheck = field.isAnnotationPresent(OneToMany.class) ||
                        field.isAnnotationPresent(ManyToOne.class) || field.isAnnotationPresent(ManyToMany.class);

                // If the column annotation is not present we save the name of the column and continue
                if (!field.isAnnotationPresent(Column.class) && !skipColumnCheck) {
                    if (!checkInvalidModifiers(field)) {
                        columnName = escapeCaseCaps(columnName);
                        entityField.setColumnName(columnName);
                        columnsList.add(entityField);
                    }
                    skipColumnCheck = true;
                }

                if (skipColumnCheck)
                    continue;

                // In case it has the column annotation with the name, we use that instead
                Column column = field.getAnnotation(Column.class);
                if (column != null && !column.name().isEmpty()) {
                    columnName = column.name();
                } else {
                    columnName = escapeCaseCaps(columnName);
                }

                entityField.setColumnName(columnName);
                columnsList.add(entityField);
            }
        } catch (IllegalAccessException ex) {
            throw new EntityReflectionException(ex);
        }
        return columnsList;
    }

    /**
     * Get the entity sequence name.
     *
     * @param entity An instance of the entity to get the table of.
     * @return The name of the sequence of the entity.
     */
    public static String getEntitySequenceName(Object entity) {
        Class<?> entityClass = entity.getClass();
        var sequenceName = "";

        while (entityClass != Object.class) {
            SequenceGenerator sequenceAnnotation = entityClass.getAnnotation(SequenceGenerator.class);

            if (sequenceAnnotation != null) {
                sequenceName = sequenceAnnotation.sequenceName();
                break;
            }

            entityClass = entityClass.getSuperclass();
        }

        return sequenceName;
    }

    /**
     * Gets a {@link Field} from an object.
     * @param objClass The object to get the {@link Field} from.
     * @param fieldName The name of the field to get.
     * @return The {@link Field} representing the field from the object.
     */
    private static Field getClassField(Class<?> objClass, String fieldName) {
        try {
            Field field = objClass.getDeclaredField(fieldName);
            field.setAccessible(Boolean.TRUE);
            return field;
        } catch (NoSuchFieldException e) {
            Class<?> superClass = objClass.getSuperclass();
            // If the super class reached the Object level, it didn't have a declared field with that name
            if (superClass != null && !superClass.equals(Object.class)) {
                return getClassField(superClass, fieldName);
            }
            throw new EntityReflectionException("Field " + fieldName + " not found for class " +
                    objClass.getCanonicalName());
        }
    }

    /**
     * Escapes each capital letters with the specified separator table column names.
     * @param str The string to escape the caps of.
     * @return The converted string.
     */
    private static String escapeCaseCaps(String str) {
        String escapedCapsStr = CAPS_PATTERN.matcher(str).replaceAll("_$0");

        if (escapedCapsStr.startsWith("_")) {
            escapedCapsStr = escapedCapsStr.substring(1);
        }

        return escapedCapsStr.toLowerCase(Locale.getDefault());
    }

    /**
     * Checks if a field has invalid column modifiers.
     * @param field The field to check.
     * @return True if the field has invalid column modifiers.
     */
    private static boolean checkInvalidModifiers(Field field) {
        return Modifier.isStatic(field.getModifiers()) ||
                Modifier.isFinal(field.getModifiers()) ||
                Modifier.isTransient(field.getModifiers());
    }
}

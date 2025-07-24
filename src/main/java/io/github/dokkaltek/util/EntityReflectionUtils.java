package io.github.dokkaltek.util;

import io.github.dokkaltek.exception.EntityReflectionException;
import io.github.dokkaltek.helper.EntityField;
import io.github.dokkaltek.helper.PrimaryKeyFields;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Utility class to reflect on database entities.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class EntityReflectionUtils {
    private static final Pattern CAPS_PATTERN = Pattern.compile("(?<![\\-_A-Z])[A-Z]+");

    /**
     * Gets a field from an object.
     *
     * @param object    The object to get the field from.
     * @param fieldName The name of the field.
     * @param <T>       The type of the field.
     * @return The value of the field.
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
        return fields.stream().map(field -> {
            field.setAccessible(true);
            return field;
        }).toList();
    }

    /**
     * Get the entity table name.
     *
     * @param entityClass An instance of the entity to get the table of.
     * @return The name of the table of the entity.
     */
    public static <S> String getEntityTable(Class<S> entityClass) {
        Table entityTable = entityClass.getAnnotation(Table.class);
        if (entityTable == null) {
            throw new EntityReflectionException("Entity " + entityClass.getCanonicalName() +
                    " doesn't have a table annotation");
        }

        // If the table annotation doesn't have the value of the table name, use the class name
        if (entityTable.name().isEmpty()) {
            return entityClass.getSimpleName();
        }

        return entityTable.name();
    }

    /**
     * Gets the list of column names and their value.
     *
     * @param entity The entity to get the columns from.
     * @param <S>    The type of the entity.
     * @return A list of {@link EntityField} with the information of the columns and their values.
     */
    public static <S> List<EntityField> getEntityColumns(@NotNull S entity) {
        List<Field> classFields = EntityReflectionUtils.retrieveClassFields(entity.getClass());
        List<EntityField> columnsList = new ArrayList<>(classFields.size());
        try {
            for (Field field : classFields) {
                boolean skipColumnCheck = field.isAnnotationPresent(OneToMany.class) ||
                        field.isAnnotationPresent(ManyToOne.class) || field.isAnnotationPresent(ManyToMany.class) ||
                        checkInvalidModifiers(field);

                if (field.isAnnotationPresent(EmbeddedId.class)) {
                    List<EntityField> embeddedIdColumns = resolveEmbeddedIdColumns(entity, field);
                    int initialSize = embeddedIdColumns.size() + classFields.size() - 1;
                    List<EntityField> newList = new ArrayList<>(initialSize);
                    newList.addAll(embeddedIdColumns);
                    newList.addAll(columnsList);
                    columnsList = newList;
                    skipColumnCheck = true;
                }

                if (skipColumnCheck)
                    continue;

                EntityField entityField = EntityField.builder()
                        .fieldName(field.getName())
                        .isId(field.isAnnotationPresent(Id.class))
                        .isGeneratedValue(field.isAnnotationPresent(GeneratedValue.class))
                        .value(field.get(entity))
                        .build();
                String columnName = resolveFieldColumnName(field);

                entityField.setColumnName(columnName);
                columnsList.add(entityField);
            }
        } catch (IllegalAccessException ex) {
            throw new EntityReflectionException(ex);
        }

        return columnsList;
    }

    /**
     * Gets the list of column names and their value for a list of entities.
     *
     * @param entityList The entity list to get the columns from.
     * @param <S>        The type of the entity.
     * @return A list of lists of {@link EntityField} with the information of the columns and their values for
     * each entity.
     */
    public static <S> List<List<EntityField>> getEntityListColumns(@NotNull List<S> entityList) {
        if (entityList.isEmpty())
            return Collections.emptyList();
        List<Field> classFields = EntityReflectionUtils.retrieveClassFields(entityList.get(0).getClass());
        List<List<EntityField>> entityListColumns = new ArrayList<>(entityList.size());
        for (Field field : classFields) {
            boolean skipColumnCheck = field.isAnnotationPresent(OneToMany.class) ||
                    field.isAnnotationPresent(ManyToOne.class) || field.isAnnotationPresent(ManyToMany.class) ||
                    checkInvalidModifiers(field);

            if (field.isAnnotationPresent(EmbeddedId.class)) {
                addEmbeddedIdColumns(entityListColumns, entityList, field, classFields.size());
                skipColumnCheck = true;
            }

            if (skipColumnCheck)
                continue;

            for (int i = 0; i < entityList.size(); i++) {
                Object entity = entityList.get(i);
                if (entityListColumns.size() <= i)
                    entityListColumns.add(new ArrayList<>(classFields.size()));
                List<EntityField> columnsList = entityListColumns.get(i);

                EntityField entityField = EntityField.builder()
                        .fieldName(field.getName())
                        .isId(field.isAnnotationPresent(Id.class))
                        .isGeneratedValue(field.isAnnotationPresent(GeneratedValue.class))
                        .value(getField(entity, field.getName()))
                        .build();
                String columnName = resolveFieldColumnName(field);

                entityField.setColumnName(columnName);
                columnsList.add(entityField);
                entityListColumns.set(i, columnsList);
            }
        }

        // Make sure to add the embedded id columns
        return entityListColumns;
    }

    /**
     * Get the entity sequence name.
     *
     * @param entityClass   The class to get the sequence from.
     * @param sequenceField The field that is generated by the sequence.
     * @return The name of the sequence of the entity.
     */
    public static String getEntitySequenceName(Class<?> entityClass, String sequenceField) {
        String sequenceName = "";

        while (entityClass != Object.class) {
            SequenceGenerator sequenceAnnotation = entityClass.getAnnotation(SequenceGenerator.class);

            if (sequenceField != null && sequenceAnnotation == null) {
                sequenceAnnotation = getClassField(entityClass, sequenceField).getAnnotation(SequenceGenerator.class);
            }

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
     *
     * @param objClass  The object to get the {@link Field} from.
     * @param fieldName The name of the field to get.
     * @return The {@link Field} representing the field from the object.
     */
    public static Field getClassField(Class<?> objClass, String fieldName) {
        try {
            while (fieldName.contains(".")) {
                String[] split = fieldName.split("\\.");
                Field field = getClassField(objClass, split[0]);
                fieldName = String.join(".", Arrays.copyOfRange(split, 1, split.length));
                objClass = field.getType();
            }
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
     * Sets a field of an object.
     *
     * @param object    The object to set the field of.
     * @param fieldName The name of the field.
     * @param value     The value to set the field to.
     * @param <T>       The type of the field.
     */
    public static <T> void setField(Object object, String fieldName, T value) {
        try {
            getClassField(object.getClass(), fieldName).set(object, value);
        } catch (IllegalAccessException e) {
            throw new EntityReflectionException(e);
        }
    }

    /**
     * Returns the list of primary key fields.
     *
     * @param entityClass The class to check.
     * @return The list of primary key fields.
     */
    public static PrimaryKeyFields getPrimaryKeyFields(Class<?> entityClass) {
        List<Field> fields = retrieveClassFields(entityClass);
        List<Field> idFields = fields.stream().filter(f -> f.isAnnotationPresent(Id.class)).toList();
        boolean isEmbeddedId = false;
        Class<?> embeddedIdClass = null;
        String embeddedIdFieldName = null;

        if (idFields.isEmpty()) {
            Optional<Field> optEmbeddedIdClass = fields.stream()
                    .filter(f -> f.isAnnotationPresent(EmbeddedId.class)).findFirst();

            if (optEmbeddedIdClass.isPresent()) {
                Field embeddedIdField = optEmbeddedIdClass.get();
                isEmbeddedId = true;
                embeddedIdClass = embeddedIdField.getType();
                embeddedIdFieldName = embeddedIdField.getName();
                idFields = retrieveClassFields(embeddedIdClass).stream()
                        .filter(field -> !checkInvalidModifiers(field)).toList();
            }
        }

        return PrimaryKeyFields.builder()
                .fields(idFields)
                .isEmbeddedId(isEmbeddedId)
                .embeddedIdClass(embeddedIdClass)
                .embeddedIdFieldName(embeddedIdFieldName)
                .build();
    }

    /**
     * Returns the primary key of an entity.
     *
     * @param entity The entity to get the primary key of.
     * @param <S>    The entity type.
     * @param <I>    The primary key type.
     * @return The primary key of the entity.
     */
    public static <S, I> I getPrimaryKey(S entity) {
        List<Field> fields = retrieveClassFields(entity.getClass());
        Field embeddedIdField = fields.stream()
                .filter(f -> f.isAnnotationPresent(EmbeddedId.class)).findFirst().orElse(null);

        // Check for embedded id first
        if (embeddedIdField != null)
            return getField(entity, embeddedIdField.getName());

        // If there was no embedded id, check for single id
        List<Field> idFields = fields.stream().filter(f -> f.isAnnotationPresent(Id.class)).toList();

        if (idFields.size() == 1)
            return getField(entity, idFields.get(0).getName());

        // If there was no single id, check for composite id
        IdClass idClass = entity.getClass().getAnnotation(IdClass.class);
        if (idClass != null) {
            Constructor<I> constructor;
            try {
                constructor = idClass.value().getConstructor(idFields.stream()
                        .map(Field::getType).toArray(Class<?>[]::new));
                return constructor.newInstance(idFields.stream()
                        .map(field -> getField(entity, field.getName())).toArray(Object[]::new));
            } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException |
                     InstantiationException e) {
                throw new EntityReflectionException(e);
            }
        }

        throw new EntityReflectionException("The primary key for entity " + entity.getClass().getCanonicalName() +
                "couldn't be found.");
    }

    /**
     * Gets the fields of an entry as a list.
     *
     * @param entity The entity to get the fields of
     * @param <S>    The entity to get the id fields of.
     * @return The list of id fields.
     */
    public static <S> List<EntityField> getIdFieldsFromEntity(S entity) {
        PrimaryKeyFields idClassFields = getPrimaryKeyFields(entity.getClass());
        Object embeddedId = null;
        if (idClassFields.isEmbeddedId())
            embeddedId = getField(entity, idClassFields.getEmbeddedIdFieldName());
        List<EntityField> idFields = new ArrayList<>(idClassFields.getFields().size());
        for (Field idClassField : idClassFields.getFields()) {
            EntityField field = new EntityField();
            field.setFieldName(idClassField.getName());
            field.setColumnName(resolveFieldColumnName(idClassField));
            Object idValue;
            if (idClassFields.isEmbeddedId()) {
                if (embeddedId == null)
                    idValue = null;
                else
                    idValue = getField(embeddedId, idClassField.getName());
            } else
                idValue = getField(entity, idClassField.getName());
            field.setValue(idValue);
            idFields.add(field);
        }
        return idFields;
    }

    /**
     * Gets the fields of an entry as a list.
     *
     * @param id          The value of the id.
     * @param entityClass The entity class that the id belongs to (not the id class).
     * @param <I>         The class of the id (Either a single id or a composite id).
     * @param <S>         The table entity class to check for the id fields to compare to the id object.
     * @return The list of id fields.
     */
    public static <I, S> List<EntityField> getFieldsFromIdClass(I id, Class<S> entityClass) {
        PrimaryKeyFields idClassFields = getPrimaryKeyFields(entityClass);
        Object embeddedId = null;
        if (idClassFields.isEmbeddedId())
            embeddedId = id;
        List<EntityField> idFields = new ArrayList<>(idClassFields.getFields().size());
        for (Field idClassField : idClassFields.getFields()) {
            EntityField field = new EntityField();
            field.setFieldName(idClassField.getName());
            field.setColumnName(resolveFieldColumnName(idClassField));
            Object idValue;
            if (idClassFields.isEmbeddedId()) {
                if (embeddedId == null)
                    idValue = null;
                else
                    idValue = getField(embeddedId, idClassField.getName());
            } else if (entityClass.isAnnotationPresent(IdClass.class) && id != null) {
                idValue = getField(id, idClassField.getName());
            } else
                idValue = id;
            field.setValue(idValue);
            idFields.add(field);
        }
        return idFields;
    }

    /**
     * Resolves the column name of a field.
     *
     * @param field The field to resolve.
     * @return The name of the column.
     */
    public static String resolveFieldColumnName(Field field) {
        String columnName = field.getName();
        boolean skipColumnCheck = field.isAnnotationPresent(OneToMany.class) ||
                field.isAnnotationPresent(ManyToOne.class) || field.isAnnotationPresent(ManyToMany.class) ||
                checkInvalidModifiers(field);

        if (skipColumnCheck)
            return columnName;

        // If the column annotation is not present we save the name of the column and continue
        if (!field.isAnnotationPresent(Column.class))
            return escapeCaseCaps(columnName);

        // In case it has the column annotation with the name, we use that instead
        Column column = field.getAnnotation(Column.class);
        if (column != null && !column.name().isEmpty()) {
            columnName = column.name();
        } else {
            columnName = escapeCaseCaps(columnName);
        }

        return columnName;
    }

    /**
     * Escapes each capital letters with the specified separator table column names.
     *
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
     *
     * @param field The field to check.
     * @return True if the field has invalid column modifiers.
     */
    private static boolean checkInvalidModifiers(Field field) {
        return Modifier.isStatic(field.getModifiers()) ||
                Modifier.isFinal(field.getModifiers()) ||
                Modifier.isTransient(field.getModifiers());
    }

    /**
     * Resolves the columns from any field that was an embedded id.
     *
     * @param entity The object to get the columns from.
     * @param field  The class field to check.
     */
    private static List<EntityField> resolveEmbeddedIdColumns(Object entity, Field field) {
        Object embeddedId = getField(entity, field.getName());
        List<EntityField> embeddedIdColumns;
        if (embeddedId != null) {
            embeddedIdColumns = getEntityColumns(embeddedId);
            embeddedIdColumns.forEach(column -> column.setId(true));
        } else {
            embeddedIdColumns = retrieveClassFields(field.getType())
                    .stream().filter(embeddedIdFields -> !checkInvalidModifiers(embeddedIdFields))
                    .map(embeddedIdField -> {
                        EntityField column = new EntityField();
                        column.setFieldName(embeddedIdField.getName());
                        column.setColumnName(resolveFieldColumnName(embeddedIdField));
                        column.setGeneratedValue(embeddedIdField.isAnnotationPresent(GeneratedValue.class));
                        column.setId(true);
                        return column;
                    }).toList();
        }

        return embeddedIdColumns;
    }

    /**
     * Adds the embedded id columns to the entity list columns.
     *
     * @param entityListColumns Entity list columns.
     * @param entityList        Entity list.
     * @param field             The class field to be added to the columns.
     * @param classFieldsSize   The size of the class fields.
     * @param <S>               The type of the entity.
     */
    private static <S> void addEmbeddedIdColumns(List<List<EntityField>> entityListColumns, List<S> entityList,
                                                 Field field, int classFieldsSize) {
        for (int i = 0; i < entityList.size(); i++) {
            Object entity = entityList.get(i);
            List<EntityField> embeddedIdColumns = resolveEmbeddedIdColumns(entity, field);
            int initialSize = embeddedIdColumns.size() + classFieldsSize - 1;
            List<EntityField> columnsList = new ArrayList<>(initialSize);
            if (entityListColumns.size() <= i)
                entityListColumns.add(columnsList);
            else
                columnsList.addAll(entityListColumns.get(i));

            columnsList.addAll(embeddedIdColumns);
            entityListColumns.set(i, columnsList);
        }
    }
}

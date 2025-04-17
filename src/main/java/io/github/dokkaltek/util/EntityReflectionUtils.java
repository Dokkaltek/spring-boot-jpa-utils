package io.github.dokkaltek.util;

import io.github.dokkaltek.exception.EntityReflectionException;
import jakarta.persistence.SequenceGenerator;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.IdGeneratorType;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Utility class to reflect on database entities.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class EntityReflectionUtils {

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
}

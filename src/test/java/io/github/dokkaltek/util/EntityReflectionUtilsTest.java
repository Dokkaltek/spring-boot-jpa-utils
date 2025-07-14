package io.github.dokkaltek.util;

import io.github.dokkaltek.exception.EntityReflectionException;
import io.github.dokkaltek.helper.EntityField;
import io.github.dokkaltek.helper.PrimaryKeyFields;
import io.github.dokkaltek.samples.SampleEmbeddedId;
import io.github.dokkaltek.samples.SampleEntity;
import io.github.dokkaltek.samples.SampleIdClassEntity;
import io.github.dokkaltek.samples.SamplePojo;
import io.github.dokkaltek.samples.SamplePojoParent;
import io.github.dokkaltek.samples.SampleSingleIdEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for {@link EntityReflectionUtils} class.
 */
class EntityReflectionUtilsTest {
    private static final String SAMPLE_VALUE = "KEVIN";
    private SamplePojo samplePojo;


    @BeforeEach
    void setup() {
        samplePojo = new SamplePojo();
        samplePojo.setName("John");
        samplePojo.setAge(30);
        samplePojo.setDescription("test");
        SamplePojo.emptyStaticValue = null;
    }

    /**
     * Test for {@link EntityReflectionUtils#getField(Object, String)} method.
     */
    @Test
    @DisplayName("Test getting a field")
    void testGetField() {
        assertNull(EntityReflectionUtils.getField(new SamplePojo(), "name"));
        assertEquals(samplePojo.getName(), EntityReflectionUtils.getField(samplePojo, "name"));
        assertEquals(samplePojo.getAge(), (int) EntityReflectionUtils.getField(samplePojo, "age"));
        assertDoesNotThrow(() -> EntityReflectionUtils.getField(samplePojo, "description"));
        assertEquals(samplePojo.getDescription(), EntityReflectionUtils.getField(samplePojo, "description"));
        assertThrows(EntityReflectionException.class, () -> EntityReflectionUtils.getField(samplePojo, "nonexistent_field"));
    }

    /**
     * Test for {@link EntityReflectionUtils#retrieveClassFields(Class)} method.
     */
    @Test
    @DisplayName("Test getting the fields of a class")
    void testRetrieveClassFields() {
        List<Field> fields = EntityReflectionUtils.retrieveClassFields(SamplePojo.class);
        assertEquals(5, fields.size());

        List<String> fieldNames = fields.stream().map(Field::getName).toList();
        assertTrue(fieldNames.contains("name"));
        assertTrue(fieldNames.contains("age"));
        assertTrue(fieldNames.contains("description"));
        assertTrue(fieldNames.contains("emptyStaticValue"));
        assertTrue(fieldNames.contains("SAMPLE_STATIC_VALUE"));
    }

    /**
     * Test for {@link EntityReflectionUtils#getEntityTable(Class)} method.
     */
    @Test
    @DisplayName("Test getting the fields of a class")
    void testGetEntityTable() {
        String tableName = EntityReflectionUtils.getEntityTable(SampleEntity.class);
        assertEquals("sample_entity", tableName);
        assertEquals("SamplePojoParent", EntityReflectionUtils.getEntityTable(SamplePojoParent.class));
    }

    /**
     * Test for {@link EntityReflectionUtils#getEntityColumns(Object)} method.
     */
    @Test
    @DisplayName("Test getting the fields of a class")
    void testGetEntityColumns() {
        List<EntityField> fields = EntityReflectionUtils.getEntityColumns(new SampleEntity());
        assertEquals(4, fields.size());

        List<String> fieldNames = fields.stream().map(EntityField::getFieldName).toList();
        assertTrue(fieldNames.contains("entity"));
        assertTrue(fieldNames.contains("name"));
        assertTrue(fieldNames.contains("description"));
        assertTrue(fieldNames.contains("id"));
    }

    /**
     * Test for {@link EntityReflectionUtils#getEntitySequenceName(Class, String)} method.
     */
    @Test
    @DisplayName("Test getting the sequence name of a field")
    void testGetEntitySequenceName() {
       String sequence = EntityReflectionUtils.getEntitySequenceName(SampleEntity.class, "embeddedId.id");
       assertEquals("sample_seq", sequence);
    }

    /**
     * Test for {@link EntityReflectionUtils#getClassField} method.
     */
    @Test
    @DisplayName("Test getting a field of a class")
    void testGetClassField() {
        Field idField = EntityReflectionUtils.getClassField(SampleEntity.class, "embeddedId.id");
        assertNotNull(idField);
        assertEquals(Long.class, idField.getType());
        assertEquals("id", idField.getName());

        Field descField = EntityReflectionUtils.getClassField(SamplePojo.class, "description");
        assertNotNull(descField);
        assertEquals(String.class, descField.getType());
        assertEquals("description", descField.getName());
    }

    /**
     * Test for {@link EntityReflectionUtils#setField(Object, String, Object)} method.
     */
    @Test
    @DisplayName("Test setting a field of a class")
    void testSetClassField() {
        SampleEntity sampleEntity = new SampleEntity();
        EntityReflectionUtils.setField(sampleEntity, "name", SAMPLE_VALUE);
        assertNotNull(sampleEntity.getName());
        assertEquals(SAMPLE_VALUE, sampleEntity.getName());
    }

    /**
     * Test for {@link EntityReflectionUtils#getPrimaryKeyFields(Class)} method.
     */
    @Test
    @DisplayName("Test getting the primary key fields of a class")
    void testGetPrimaryKeyFields() {
        PrimaryKeyFields fields = EntityReflectionUtils.getPrimaryKeyFields(SampleEntity.class);
        assertEquals(2, fields.getFields().size());
        assertEquals("embeddedId", fields.getEmbeddedIdFieldName());
        assertEquals(SampleEmbeddedId.class,fields.getEmbeddedIdClass());
        assertTrue(fields.isEmbeddedId());
    }

    /**
     * Test for {@link EntityReflectionUtils#getPrimaryKey} method.
     */
    @Test
    @DisplayName("Test getting the primary key of an entity")
    void testGetPrimaryKey() {
        // Test with @EmbeddedId annotation
        SampleEntity sampleEntity = new SampleEntity();
        SampleEmbeddedId embeddedId = new SampleEmbeddedId(1L, "0012");
        sampleEntity.setEmbeddedId(embeddedId);
        assertEquals(embeddedId, EntityReflectionUtils.getPrimaryKey(sampleEntity));

        // Test with @IdClass annotation
        SampleIdClassEntity sampleIdClassEntity = new SampleIdClassEntity();
        sampleIdClassEntity.setId(embeddedId.getId());
        sampleIdClassEntity.setEntity(embeddedId.getEntity());
        assertEquals(embeddedId, EntityReflectionUtils.getPrimaryKey(sampleIdClassEntity));

        // Test with single id entity
        SampleSingleIdEntity singleIdEntity = new SampleSingleIdEntity();
        singleIdEntity.setId(embeddedId.getId());
        assertEquals(embeddedId.getId(), EntityReflectionUtils.getPrimaryKey(singleIdEntity));

        // Test with empty class
        assertDoesNotThrow(() -> EntityReflectionUtils.getPrimaryKey(new SampleEntity()));
    }

    /**
     * Test for {@link EntityReflectionUtils#resolveFieldColumnName(Field)} method.
     */
    @Test
    @DisplayName("Test getting the primary key fields of a class")
    void testResolveFieldColumnName() {
        Field field = EntityReflectionUtils.getClassField(SampleEntity.class, "name");
        String nameFieldColumn = EntityReflectionUtils.resolveFieldColumnName(field);
        assertEquals("name", nameFieldColumn);

        field = EntityReflectionUtils.getClassField(SampleEntity.class, "embeddedId.entity");
        String entityFieldColumn = EntityReflectionUtils.resolveFieldColumnName(field);
        assertEquals("entity2", entityFieldColumn);
    }
}

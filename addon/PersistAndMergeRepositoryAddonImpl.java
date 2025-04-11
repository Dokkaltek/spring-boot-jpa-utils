package com.santander.sgt.apm2095.sgtapm2095procmanageback.repository.addon;

import com.santander.sgt.apm2095.sgtapm2095procmanageback.exceptions.OpenflowGenericException;
import jakarta.persistence.Column;
import jakarta.persistence.EntityManager;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.RequiredArgsConstructor;
import org.hibernate.annotations.GenericGenerator;
import org.infinispan.commons.util.ReflectionUtil;
import org.springframework.data.jpa.repository.JpaContext;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.santander.sgt.apm2095.sgtapm2095procmanageback.constant.ErrorConstants.DATABASE_TRANSACTION_ERROR;

/**
 * Implementation of {@link PersistAndMergeRepositoryAddon}.
 *
 * @param <T> The entity of the repository.
 */
@RequiredArgsConstructor
public class PersistAndMergeRepositoryAddonImpl<T> implements PersistAndMergeRepositoryAddon<T> {
  private static final int ID_PLACEHOLDER_SIZE = 2;
  private final JpaContext jpaContext;

  @Transactional
  @Override
  public <S extends T> S persist(S entity) {
    jpaContext.getEntityManagerByManagedType(entity.getClass()).persist(entity);
    return entity;
  }

  @Transactional
  @Override
  public <S extends T> S persistAndFlush(S entity) {
    var entityManager = jpaContext.getEntityManagerByManagedType(entity.getClass());
    entityManager.persist(entity);
    entityManager.flush();
    return entity;
  }

  @Transactional
  @Override
  public <S extends T> List<S> persistAll(Iterable<S> entryList) {
    if (!entryList.iterator().hasNext()) {
      return Collections.emptyList();
    }

    EntityManager entityManager = null;
    List<S> result = new ArrayList<>();

    for (S entity : entryList) {
      if (entityManager == null) {
        entityManager = jpaContext.getEntityManagerByManagedType(entity.getClass());
      }
      entityManager.persist(entity);
      result.add(entity);
    }

    return result;
  }

  @Transactional
  @Override
  public <S extends T> List<S> persistAllAndFlush(Iterable<S> entryList) {
    if (!entryList.iterator().hasNext()) {
      return Collections.emptyList();
    }

    EntityManager entityManager = null;
    List<S> result = new ArrayList<>();

    for (S entity : entryList) {
      if (entityManager == null) {
        entityManager = jpaContext.getEntityManagerByManagedType(entity.getClass());
      }
      entityManager.persist(entity);
      entityManager.flush();
      result.add(entity);
    }

    return result;
  }

  /**
   * Deletes a registry from the database without checking if it exists or not.
   *
   * @param entity The entity to delete.
   */
  @Override
  public <S extends T> void removeWithoutChecks(S entity) {
    var entityManager = jpaContext.getEntityManagerByManagedType(entity.getClass());
    entityManager.remove(entity);
  }

  @Transactional
  @Override
  public <S extends T> S merge(S entity) {
    return jpaContext.getEntityManagerByManagedType(entity.getClass()).merge(entity);
  }

  @Transactional
  @Override
  public <S extends T> S mergeAndFlush(S entity) {
    var entityManager = jpaContext.getEntityManagerByManagedType(entity.getClass());
    S result = entityManager.merge(entity);
    entityManager.flush();
    return result;
  }

  @Transactional
  @Override
  public <S extends T> List<S> mergeAll(Iterable<S> entryList) {
    if (!entryList.iterator().hasNext()) {
      return Collections.emptyList();
    }

    EntityManager entityManager = null;
    List<S> result = new ArrayList<>();

    for (S entity : entryList) {
      if (entityManager == null) {
        entityManager = jpaContext.getEntityManagerByManagedType(entity.getClass());
      }
      entityManager.merge(entity);
      result.add(entity);
    }

    return result;
  }

  @Transactional
  @Override
  public <S extends T> List<S> mergeAllAndFlush(Iterable<S> entryList) {
    if (!entryList.iterator().hasNext()) {
      return Collections.emptyList();
    }

    EntityManager entityManager = null;
    List<S> result = new ArrayList<>();

    for (S entity : entryList) {
      if (entityManager == null) {
        entityManager = jpaContext.getEntityManagerByManagedType(entity.getClass());
      }
      entityManager.merge(entity);
      entityManager.flush();
      result.add(entity);
    }

    return result;
  }

  /**
   * Creates a registry in the database without checking if it exists for each element of each collection.
   * All collections should use the same transaction manager, which is derived from the main entity list passed.
   *
   * @param entryList     The entity to save.
   * @param extraEntities Other table entities to store in the same query.
   */
  @Transactional
  @Modifying
  @Override
  public <S extends T> void insertAll(Iterable<S> entryList, Iterable<?>... extraEntities) {
    if (entryList == null || !entryList.iterator().hasNext()) {
      return;
    }

    EntityManager entityManager = null;
    var insertQuery = new StringBuilder();
    Map<Integer, Object> bindings = new HashMap<>();
    for (S entity : entryList) {
      if (entityManager == null) {
        entityManager = jpaContext.getEntityManagerByManagedType(entity.getClass());
      }
      insertQuery.append(generateIntoStatement(entity, bindings));
    }

    for (Iterable<?> entityList : extraEntities) {
      for (Object entity : entityList) {
        insertQuery.append(generateIntoStatement(entity, bindings));
      }
    }

    insertAllItems(entityManager, generateInsertAllSQL(insertQuery), bindings);
  }

  /**
   * Creates a registry in the database without checking if it exists for each element of the collection.
   *
   * @param entryList The entity to save.
   */
  @Transactional
  @Modifying
  @Override
  public <S extends T> void insertAll(Iterable<S> entryList) {
    if (entryList == null || !entryList.iterator().hasNext()) {
      return;
    }

    EntityManager entityManager = null;
    var insertQuery = new StringBuilder();
    Map<Integer, Object> bindings = new HashMap<>();
    for (S entity : entryList) {
      if (entityManager == null) {
        entityManager = jpaContext.getEntityManagerByManagedType(entity.getClass());
      }
      insertQuery.append(generateIntoStatement(entity, bindings));
    }

    insertAllItems(entityManager, generateInsertAllSQL(insertQuery), bindings);
  }

  /**
   * Creates a registry in the database calculating the id for each entry.
   *
   * @param entryList         The entity to save.
   * @param idParamToGenerate The id parameter to autogenerate by a table sequence.
   */
  @Transactional
  @Modifying
  @Override
  public <S extends T> void insertAllWithSequenceId(Iterable<S> entryList, String idParamToGenerate) {
    if (entryList == null || !entryList.iterator().hasNext()) {
      return;
    }

    EntityManager entityManager = null;
    var insertQuery = new StringBuilder();
    Map<Integer, Object> bindings = new HashMap<>();

    for (S entity : entryList) {
      if (entityManager == null) {
        entityManager = jpaContext.getEntityManagerByManagedType(entity.getClass());
      }

      if (insertQuery.isEmpty()) {
        insertQuery.append("INSERT INTO ").append(getEntityTable(entity))
          .append(" (").append(getEntityColumns(entity, idParamToGenerate))
          .append(") SELECT ").append(getEntitySequenceName(entity))
          .append(".nextval, mt.* FROM(");
      } else {
        insertQuery.append(" UNION ");
      }

      insertQuery.append(generateSelectFromEntryAndSequence(entity, idParamToGenerate, bindings));
    }

    insertQuery.append(") mt");

    insertAllItems(entityManager, insertQuery.toString(), bindings);
  }


  /**
   * Performs the insert of the elements from the insert all query.
   *
   * @param entityManager The entity manager to manage the persistence.
   * @param insertQuery   The insert query.
   * @param bindings      The bindings to set as values of the query parameters.
   */
  private static void insertAllItems(EntityManager entityManager, String insertQuery, Map<Integer, Object> bindings) {
    if (entityManager == null || insertQuery.isBlank()) {
      throw new OpenflowGenericException(DATABASE_TRANSACTION_ERROR,
        "Could not find the entity manager for a batch insert operation.",
        HttpStatus.INTERNAL_SERVER_ERROR);
    }

    var query = entityManager.createNativeQuery(insertQuery);
    bindings.forEach(query::setParameter);
    query.executeUpdate();
  }

  /**
   * Generates the insert SQL for an entity.
   *
   * @param insertsSql The insert sql statements.
   * @return The generated insert SQL for the entity.
   */
  private static String generateInsertAllSQL(StringBuilder insertsSql) {
    return "INSERT ALL \n " + insertsSql.toString() + " SELECT * FROM dual";
  }

  /**
   * Generates the "into" part of the insert all statement.
   *
   * @param entity   The entity to generate the part of.
   * @param bindings The map with the parameter bindings.
   * @return The "into" part of the insert all statement for an element.
   */
  private static String generateIntoStatement(Object entity, Map<Integer, Object> bindings) {
    return "INTO " + getEntityTable(entity) + getEntityColumnsIntoValues(entity, bindings);
  }

  /**
   * Get the entity table name.
   *
   * @param entity An instance of the entity to get the table of.
   * @return The name of the table of the entity.
   */
  private static String getEntityTable(Object entity) {
    Table entityTable = entity.getClass().getAnnotation(Table.class);
    return entityTable.name();
  }

  /**
   * Get the entity sequence name.
   *
   * @param entity An instance of the entity to get the table of.
   * @return The name of the sequence of the entity.
   */
  private static String getEntitySequenceName(Object entity) {
    Class<?> entityClass = entity.getClass();
    var sequenceName = "";

    while (entityClass != Object.class) {
      GenericGenerator genericGenerator = entityClass.getAnnotation(GenericGenerator.class);
      SequenceGenerator sequenceAnnotation = entityClass.getAnnotation(SequenceGenerator.class);

      if (sequenceAnnotation != null || genericGenerator != null) {
        if (sequenceAnnotation != null) {
          sequenceName = sequenceAnnotation.sequenceName();
        } else {
          sequenceName = Arrays.stream(genericGenerator.parameters())
            .filter(param -> "sequence_name".equals(param.name()))
            .findFirst().map(org.hibernate.annotations.Parameter::value)
            .orElse("");
        }

        break;
      }

      entityClass = entityClass.getSuperclass();
    }

    return sequenceName;
  }

  /**
   * Generate the '(columns) VALUES (values)' sql part of the insert into query.
   *
   * @param entity   The entity to get the columns as a {@link StringBuilder} representation of.
   * @param bindings The parameter bindings.
   * @return The generated query fragment.
   */
  private static StringBuilder getEntityColumnsIntoValues(Object entity, Map<Integer, Object> bindings) {
    var allColumns = new StringBuilder();
    var columnValues = new StringBuilder();

    for (Field field : retrieveFields(entity.getClass())) {
      // Requires the @Column annotation to be in all entity columns we want to get
      if (!field.isAnnotationPresent(Column.class)) {
        continue;
      }

      Column column = field.getAnnotation(Column.class);
      if (!allColumns.isEmpty()) {
        allColumns.append(", ");
        columnValues.append(", ");
      }

      allColumns.append(column.name());

      Object columnValue = ReflectionUtil.getValue(entity, field.getName());
      bindings.put(bindings.size() + 1, columnValue);
      columnValues.append("?").append(bindings.size());
    }
    return new StringBuilder("(").append(allColumns).append(") VALUES (").append(columnValues).append(")\n");
  }

  /**
   * Gets the entity columns, placing the id column first.
   *
   * @param entry   The entry to get the columns of.
   * @param idParam The param to place first on the string.
   * @return A string with columns of the entity separated with commas.
   */
  private static String getEntityColumns(Object entry, String idParam) {
    var allColumns = new StringBuilder();
    String idColumn = idParam;


    for (Field field : retrieveFields(entry.getClass())) {
      // Requires the @Column annotation to be in all entity columns we want to get
      if (!field.isAnnotationPresent(Column.class)) {
        continue;
      }

      Column column = field.getAnnotation(Column.class);
      if (!allColumns.isEmpty() && !idParam.equals(field.getName())) {
        allColumns.append(", ");
      } else {
        allColumns.append("{}");
      }

      if (idParam.equals(field.getName())) {
        idColumn = column.name();
      } else {
        allColumns.append(column.name());
      }
    }

    return allColumns.replace(0, ID_PLACEHOLDER_SIZE, idColumn).toString();
  }

  /**
   * Generates a select that returns a table with the values of the array along with the sequence as id.
   *
   * @param entry The entity to get the columns as a {@link StringBuilder} representation of.
   * @return The columns of the entity as a string.
   */
  private static String generateSelectFromEntryAndSequence(Object entry, String idParam,
                                                           Map<Integer, Object> bindings) {
    var columnValues = new StringBuilder();

    for (Field field : retrieveFields(entry.getClass())) {
      // Requires the @Column annotation to be in all entity columns we want to get
      if (!field.isAnnotationPresent(Column.class) || idParam.equals(field.getName())) {
        continue;
      }

      Column column = field.getAnnotation(Column.class);
      if (!columnValues.isEmpty()) {
        columnValues.append(", ");
      }

      Object columnValue = ReflectionUtil.getValue(entry, field.getName());

      if (columnValue != null) {
        bindings.put(bindings.size() + 1, columnValue);
        columnValues.append("(?").append(bindings.size()).append(") as ").append(column.name());
      } else {
        columnValues.append("NULL as").append(column.name());
      }
    }
    return "SELECT " + columnValues + " FROM DUAL";
  }

  /**
   * Get all the declared fields of the class.
   *
   * @param entityClass The class to get the fields.
   * @return A List of the fields.
   */
  private static List<Field> retrieveFields(Class<?> entityClass) {
    List<Field> fields = new ArrayList<>(Arrays.asList(entityClass.getDeclaredFields()));
    // Add all fields of super classes
    while (entityClass.getSuperclass() != Object.class) {
      entityClass = entityClass.getSuperclass();
      fields.addAll(Arrays.asList(entityClass.getDeclaredFields()));
    }
    return fields;
  }
}

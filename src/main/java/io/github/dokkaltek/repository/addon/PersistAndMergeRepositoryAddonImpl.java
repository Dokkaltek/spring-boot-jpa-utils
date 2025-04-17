package io.github.dokkaltek.repository.addon;

import io.github.dokkaltek.helper.QueryData;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.hibernate.HibernateException;
import org.springframework.data.jpa.repository.JpaContext;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static io.github.dokkaltek.util.QueryBuilderUtils.generateOracleInsertAllSQL;
import static io.github.dokkaltek.util.QueryBuilderUtils.generateOracleInsertAllWithSequenceId;

/**
 * Implementation of {@link PersistAndMergeRepositoryAddon}.
 *
 * @param <T> The entity of the repository.
 */
@RequiredArgsConstructor
public class PersistAndMergeRepositoryAddonImpl<T> implements PersistAndMergeRepositoryAddon<T> {
  private final JpaContext jpaContext;

  @Transactional
  @Override
  public <S extends T> S persist(S entity) {
    jpaContext.getEntityManagerByManagedType(entity.getClass()).persist(entity);
    return entity;
  }

  /**
   * {@inheritDoc}
   */
  @Transactional
  @Override
  public <S extends T> S persistAndFlush(S entity) {
    EntityManager entityManager = jpaContext.getEntityManagerByManagedType(entity.getClass());
    entityManager.persist(entity);
    entityManager.flush();
    return entity;
  }

  /**
   * {@inheritDoc}
   */
  @Transactional
  @Override
  public <S extends T> List<S> persistAll(Iterable<S> entryList) {
    if (!entryList.iterator().hasNext()) {
      return Collections.emptyList();
    }

      List<S> result;
      try (EntityManager entityManager = resolveEntityManagerFromLists(entryList)) {
        result = new ArrayList<>();

        for (S entity : entryList) {
            entityManager.persist(entity);
            result.add(entity);
        }
      }

      return result;
  }

  /**
   * {@inheritDoc}
   */
  @Transactional
  @Override
  public <S extends T> List<S> persistAllAndFlush(Iterable<S> entryList) {
    if (!entryList.iterator().hasNext()) {
      return Collections.emptyList();
    }

      List<S> result;
      try (EntityManager entityManager = resolveEntityManagerFromLists(entryList)) {
        result = new ArrayList<>();

        for (S entity : entryList) {
            entityManager.persist(entity);
            entityManager.flush();
            result.add(entity);
        }
      }

      return result;
  }

  /**
   * {@inheritDoc}
   */
  @Transactional
  @Override
  public <S extends T> void removeWithoutChecks(S entity) {
    EntityManager entityManager = jpaContext.getEntityManagerByManagedType(entity.getClass());
    entityManager.remove(entity);
  }

  /**
   * {@inheritDoc}
   */
  @Transactional
  @Override
  public <S extends T> S merge(S entity) {
    return jpaContext.getEntityManagerByManagedType(entity.getClass()).merge(entity);
  }

  /**
   * {@inheritDoc}
   */
  @Transactional
  @Override
  public <S extends T> S mergeAndFlush(S entity) {
    EntityManager entityManager = jpaContext.getEntityManagerByManagedType(entity.getClass());
    S result = entityManager.merge(entity);
    entityManager.flush();
    return result;
  }

  /**
   * {@inheritDoc}
   */
  @Transactional
  @Override
  public <S extends T> List<S> mergeAll(Iterable<S> entryList) {
    if (!entryList.iterator().hasNext()) {
      return Collections.emptyList();
    }

      List<S> result;
      try (EntityManager entityManager = resolveEntityManagerFromLists(entryList)) {
        result = new ArrayList<>();

        for (S entity : entryList) {
            entityManager.merge(entity);
            result.add(entity);
        }
      }

      return result;
  }

  /**
   * {@inheritDoc}
   */
  @Transactional
  @Override
  public <S extends T> List<S> mergeAllAndFlush(Iterable<S> entryList) {
    if (!entryList.iterator().hasNext()) {
      return Collections.emptyList();
    }

    List<S> result = new ArrayList<>();

    try (EntityManager entityManager = resolveEntityManagerFromLists(entryList)) {
      for (S entity : entryList) {
          entityManager.merge(entity);
          entityManager.flush();
          result.add(entity);
      }
    }

    return result;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public <S extends T> void insertAll(Iterable<S> entryList, Iterable<?>... extraEntities) {
    // TODO
  }

  /**
   * {@inheritDoc}
   */
  @Transactional
  @Modifying
  @Override
  public <S extends T> void oracleInsertAll(Iterable<S> entryList, Iterable<?>... extraEntities) {
    if (entryList == null || !entryList.iterator().hasNext()) {
      return;
    }

    EntityManager entityManager = resolveEntityManagerFromLists(entryList);
    QueryData oracleInsert = generateOracleInsertAllSQL(entryList, extraEntities);
    insertAllItems(entityManager, oracleInsert.getQuery(), oracleInsert.getPositionBindings());
  }

  /**
   * {@inheritDoc}
   */
  @Transactional
  @Modifying
  @Override
  public <S extends T> void oracleInsertAllWithSequenceId(Iterable<S> entryList, String idParamToGenerate) {
    oracleInsertAllWithSequence(entryList, idParamToGenerate, null);
  }

  /**
   * {@inheritDoc}
   */
  @Transactional
  @Modifying
  @Override
  public <S extends T> void oracleInsertAllWithSequenceId(Iterable<S> entryList, String idParamToGenerate,
                                                          String sequenceName) {
    oracleInsertAllWithSequence(entryList, idParamToGenerate, sequenceName);
  }

  /**
   * Implementation of the oracle insert all with sequence.
   * @param entryList The entries to insert.
   * @param idParamToGenerate The id parameter to generate with a sequence.
   * @param sequenceName The sequence name.
   * @param <S> The entries type.
   */
  private <S extends T> void oracleInsertAllWithSequence(Iterable<S> entryList, String idParamToGenerate,
                                                         String sequenceName) {
    if (entryList == null || !entryList.iterator().hasNext()) {
      return;
    }

    EntityManager entityManager = resolveEntityManagerFromLists(entryList);
    QueryData queryData = generateOracleInsertAllWithSequenceId(entryList, idParamToGenerate, sequenceName);
    insertAllItems(entityManager, queryData.getQuery(), queryData.getPositionBindings());
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
      throw new HibernateException("Could not find the entity manager for the batch insert operation.");
    }

    var query = entityManager.createNativeQuery(insertQuery);
    bindings.forEach(query::setParameter);
    query.executeUpdate();
  }

  /**
   * Resolves the entity manager to use from a group of lists of entries.
   * @param mainList The main list to search for.
   * @param extraLists The extra lists to search for.
   * @return The found {@link EntityManager} or null.
   */
  private EntityManager resolveEntityManagerFromLists(Iterable<?> mainList, Iterable<?>... extraLists) {
      if (mainList != null && mainList.iterator().hasNext())
        return jpaContext.getEntityManagerByManagedType(mainList.iterator().next().getClass());
      else if (extraLists != null){
        for (Iterable<?> extraList : extraLists) {
          if (extraList != null && extraList.iterator().hasNext())
            return jpaContext.getEntityManagerByManagedType(extraList.iterator().next().getClass());
        }
      }
      throw new NullPointerException("No entry was found to get the entity manager from.");
  }
}

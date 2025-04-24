package io.github.dokkaltek.repository.addon;

import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaContext;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Implementation of {@link EntityManagerRepository}.
 *
 * @param <T> The entity of the repository.
 */
public class EntityManagerRepositoryImpl<T, I> implements EntityManagerRepository<T, I> {
  private final EntityManager entityManager;

  /**
   * Default constructor.
   * @param jpaContext The JPA context.
   */
  public EntityManagerRepositoryImpl(@Autowired JpaContext jpaContext) {
    this.entityManager = getEntityManager(jpaContext);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public <S extends T> Optional<S> find(Class<S> entityClass, I id) {
    S result = entityManager.find(entityClass, id);

    if (result == null) {
      return Optional.empty();
    } else {
      return Optional.of(result);
    }
  }

  @Transactional
  @Override
  public <S extends T> S persist(S entity) {
    entityManager.persist(entity);
    return entity;
  }

  /**
   * {@inheritDoc}
   */
  @Transactional
  @Override
  public <S extends T> S persistAndFlush(S entity) {
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

      result = new ArrayList<>();

      for (S entity : entryList) {
          entityManager.persist(entity);
          result.add(entity);
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

      result = new ArrayList<>();

      for (S entity : entryList) {
          entityManager.persist(entity);
          entityManager.flush();
          result.add(entity);
      }

      return result;
  }

  /**
   * {@inheritDoc}
   */
  @Transactional
  @Override
  public <S extends T> void removeWithoutChecks(S entity) {
    entityManager.remove(entity);
  }

  /**
   * {@inheritDoc}
   */
  @Transactional
  @Override
  public <S extends T> S merge(S entity) {
    return entityManager.merge(entity);
  }

  /**
   * {@inheritDoc}
   */
  @Transactional
  @Override
  public <S extends T> S mergeAndFlush(S entity) {
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

      result = new ArrayList<>();

      for (S entity : entryList) {
          entityManager.merge(entity);
          result.add(entity);
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

    for (S entity : entryList) {
        entityManager.merge(entity);
        entityManager.flush();
        result.add(entity);
    }

    return result;
  }

  /**
   * Detaches an entity from the managed context.
   *
   * @param entity The entity to detach.
   */
  @Override
  public <S extends T> void detach(S entity) {
    entityManager.detach(entity);
  }

  /**
   * Detaches a collection of entities from the managed context.
   *
   * @param entryList The entity collection to detach.
   */
  @Override
  public <S extends T> void detachAll(Iterable<S> entryList) {
      for (S entity : entryList) {
          entityManager.detach(entity);
      }
  }

  /**
   * Refreshes the state of an entity from the database.
   *
   * @param entity The entity to refresh.
   */
  @Override
  public <S extends T> void refresh(S entity) {
    entityManager.refresh(entity);
  }

  /**
   * Refreshes the state of a list of entities from the database.
   * Usually it's better to just create a new <code>findByxxx</code> method instead, since this will refresh
   * each item of the list one by one in the same transaction.
   *
   * @param entryList The entity collection to refresh.
   */
  @Transactional(readOnly = true)
  @Override
  public <S extends T> void refreshAll(Iterable<S> entryList) {
      for (S entity : entryList) {
        entityManager.refresh(entity);
      }
  }

  /**
   * Returns wether a given entity is managed.
   *
   * @param entity The entity to refresh.
   */
  @Override
  public <S extends T> boolean contains(S entity) {
    return entityManager.contains(entity);
  }

  /**
   * Gets the managed type of the repository.
   * @return The managed type.
   */
  private Class<T> getManagedType() {
    return (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
  }

  /**
   * Resolves the {@link EntityManager} from the parametrized type.
   * @param jpaContext The JPA context.
   * @return The found {@link EntityManager}.
   */
  private EntityManager getEntityManager(JpaContext jpaContext) {
    return jpaContext.getEntityManagerByManagedType(getManagedType());
  }
}

package io.github.dokkaltek.repository.addon;

import io.github.dokkaltek.helper.QueryData;
import io.github.dokkaltek.util.QueryBuilderUtils;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaContext;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.StreamSupport;

import static io.github.dokkaltek.util.QueryBuilderUtils.getJPQLWhereClauseFilteringByPrimaryKeys;

/**
 * Implementation of {@link SimpleRepositoryAddon}.
 */
@Repository
@RequiredArgsConstructor
public class SimpleRepositoryAddonImpl implements SimpleRepositoryAddon {
    private final JpaContext jpaContext;

    /**
     * {@inheritDoc}
     */
    @Override
    public <S, I> Optional<S> find(Class<S> entityClass, I id) {
        EntityManager entityManager = jpaContext.getEntityManagerByManagedType(entityClass);
        S result = entityManager.find(entityClass, id);

        if (result == null) {
            return Optional.empty();
        } else {
            return Optional.of(result);
        }
    }

    @Transactional
    @Override
    public <S> S persist(S entity) {
        EntityManager entityManager = jpaContext.getEntityManagerByManagedType(entity.getClass());
        entityManager.persist(entity);
        return entity;
    }

    /**
     * {@inheritDoc}
     */
    @Transactional
    @Override
    public <S> S persistAndFlush(S entity) {
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
    public <S> List<S> persistAll(Iterable<S> entryList) {
        if (!entryList.iterator().hasNext()) {
            return Collections.emptyList();
        }

        EntityManager entityManager = jpaContext.getEntityManagerByManagedType(entryList.iterator().next().getClass());
        return StreamSupport.stream(entryList.spliterator(), false).map(entity -> {
            entityManager.persist(entity);
            return entity;
        }).toList();
    }

    /**
     * {@inheritDoc}
     */
    @Transactional
    @Override
    public <S> List<S> persistAllAndFlush(Iterable<S> entryList) {
        if (!entryList.iterator().hasNext()) {
            return Collections.emptyList();
        }

        EntityManager entityManager = jpaContext.getEntityManagerByManagedType(entryList.iterator().next().getClass());
        return StreamSupport.stream(entryList.spliterator(), false).map(entity -> {
            entityManager.persist(entity);
            entityManager.flush();
            return entity;
        }).toList();
    }

    /**
     * {@inheritDoc}
     */
    @Transactional
    @Override
    public <S> void remove(S entity) {
        EntityManager entityManager = jpaContext.getEntityManagerByManagedType(entity.getClass());
        entityManager.remove(entity);
    }

  /**
   * {@inheritDoc}
   */
  @Transactional
  @Override
  public <S> void removeAll(Iterable<S> entryList) {
    EntityManager entityManager = jpaContext.getEntityManagerByManagedType(entryList.iterator().next().getClass());
    for (S entity : entryList) {
      entityManager.remove(entity);
    }
  }

    /**
     * {@inheritDoc}
     */
    @Override
    @Modifying
    @Transactional
    public <S, I> void deleteByIdIn(@NotNull List<I> idList, Class<S> entityClass) {
        if (idList.isEmpty())
            return;
        I id = idList.get(0);
        String tableAlias = "mt";
        QueryData queryPair = getJPQLWhereClauseFilteringByPrimaryKeys(idList, entityClass,
                tableAlias);

        EntityManager entityManager = jpaContext.getEntityManagerByManagedType(id.getClass());
        Query query = entityManager.createQuery("DELETE FROM " + entityClass.getSimpleName() + " " + tableAlias +
                queryPair.getQuery());
        queryPair.getPositionBindings().forEach(query::setParameter);
        query.executeUpdate();
    }

    /**
     * {@inheritDoc}
     */
    @Transactional
    @Override
    public <S> S merge(S entity) {
        EntityManager entityManager = jpaContext.getEntityManagerByManagedType(entity.getClass());
        return entityManager.merge(entity);
    }

    /**
     * {@inheritDoc}
     */
    @Transactional
    @Override
    public <S> S mergeAndFlush(S entity) {
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
    public <S> List<S> mergeAll(Iterable<S> entryList) {
        if (!entryList.iterator().hasNext()) {
            return Collections.emptyList();
        }

        EntityManager entityManager = jpaContext.getEntityManagerByManagedType(entryList.iterator().next().getClass());
        return StreamSupport.stream(entryList.spliterator(), false).map(entity -> {
            entityManager.merge(entity);
            return entity;
        }).toList();
    }

    /**
     * {@inheritDoc}
     */
    @Transactional
    @Override
    public <S> List<S> mergeAllAndFlush(Iterable<S> entryList) {
        if (!entryList.iterator().hasNext()) {
            return Collections.emptyList();
        }

        EntityManager entityManager = jpaContext.getEntityManagerByManagedType(entryList.iterator().next().getClass());

        return StreamSupport.stream(entryList.spliterator(), false)
                .map(entity -> {
                    entityManager.merge(entity);
                    entityManager.flush();
                    return entity;
                }).toList();
    }

    /**
     * {@inheritDoc}
     */
    @Transactional
    @Override
    public <S> S update(S entity) {
        EntityManager entityManager = jpaContext.getEntityManagerByManagedType(entity.getClass());
        QueryData updateStatement = QueryBuilderUtils.generateUpdateStatement(entity);
        Query query = entityManager.createQuery(updateStatement.getQuery());
        updateStatement.getPositionBindings().forEach(query::setParameter);
        query.executeUpdate();
        return entity;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <S> S update(S entity, Set<String> fieldsToUpdate) {
        EntityManager entityManager = jpaContext.getEntityManagerByManagedType(entity.getClass());
        QueryData updateStatement = QueryBuilderUtils.generateUpdateStatement(entity, fieldsToUpdate);
        Query query = entityManager.createNativeQuery(updateStatement.getQuery());
        updateStatement.getPositionBindings().forEach(query::setParameter);
        query.executeUpdate();
        return entity;
    }

    /**
     * {@inheritDoc}
     */
    @Transactional
    @Override
    public <S> List<S> updateAll(Iterable<S> entryList) {
        if (!entryList.iterator().hasNext()) {
            return Collections.emptyList();
        }

        return StreamSupport.stream(entryList.spliterator(), false)
                .map(this::update).toList();
    }

    /**
     * {@inheritDoc}
     */
    @Transactional
    @Override
    public <S> List<S> updateAll(Iterable<S> entryList, Set<String> fieldsToUpdate) {
        if (!entryList.iterator().hasNext()) {
            return Collections.emptyList();
        }

        return StreamSupport.stream(entryList.spliterator(), false)
                .map(entity -> update(entity, fieldsToUpdate)).toList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <S> void detach(S entity) {
        EntityManager entityManager = jpaContext.getEntityManagerByManagedType(entity.getClass());
        entityManager.detach(entity);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <S> void detachAll(Iterable<S> entryList) {
        EntityManager entityManager = jpaContext.getEntityManagerByManagedType(entryList.iterator().next().getClass());
        for (S entity : entryList) {
            entityManager.detach(entity);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <S> void refresh(S entity) {
        EntityManager entityManager = jpaContext.getEntityManagerByManagedType(entity.getClass());
        entityManager.refresh(entity);
    }

    /**
     * {@inheritDoc}
     */
    @Transactional(readOnly = true)
    @Override
    public <S> void refreshAll(Iterable<S> entryList) {
        EntityManager entityManager = jpaContext.getEntityManagerByManagedType(entryList.iterator().next().getClass());
        for (S entity : entryList) {
            entityManager.refresh(entity);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <S> boolean contains(S entity) {
        EntityManager entityManager = jpaContext.getEntityManagerByManagedType(entity.getClass());
        return entityManager.contains(entity);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <S> boolean containsAll(Iterable<S> entryList) {
      if (!entryList.iterator().hasNext()) {
        return false;
      }

      EntityManager entityManager = jpaContext.getEntityManagerByManagedType(entryList.iterator().next().getClass());
      for (S entity : entryList) {
        if (!entityManager.contains(entity))
          return false;
      }
      return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <S> EntityManager getEntityManager(Class<S> managedEntityClass) {
        return jpaContext.getEntityManagerByManagedType(managedEntityClass);
    }
}

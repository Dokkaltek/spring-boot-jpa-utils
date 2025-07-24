package io.github.dokkaltek.repository.addon;

import io.github.dokkaltek.helper.EntityField;
import io.github.dokkaltek.helper.QueryData;
import io.github.dokkaltek.util.EntityReflectionUtils;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaContext;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.StreamSupport;

import static io.github.dokkaltek.util.QueryBuilderUtils.generateUpdateColumnsAndWhereClause;
import static io.github.dokkaltek.util.QueryBuilderUtils.getJPQLWhereClauseFilteringByPrimaryKeys;

/**
 * Implementation of {@link SimpleRepositoryAddon}.
 */
@Repository
@RequiredArgsConstructor
public class SimpleRepositoryAddonImpl implements SimpleRepositoryAddon {
    private static final String UPDATE = "UPDATE ";
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

    /**
     * {@inheritDoc}
     */
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
    public <S, I> boolean deleteById(@NotNull I id, Class<S> entityClass) {
        String tableAlias = "mt";
        QueryData queryPair = getJPQLWhereClauseFilteringByPrimaryKeys(Collections.singletonList(id), entityClass,
                tableAlias);
        EntityManager entityManager = jpaContext.getEntityManagerByManagedType(entityClass);
        Query query = entityManager.createQuery("DELETE FROM " + entityClass.getSimpleName() + " " + tableAlias +
                queryPair.getQuery());
        queryPair.getPositionBindings().forEach(query::setParameter);
        int entriesDeleted = query.executeUpdate();
        return entriesDeleted > 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Modifying
    @Transactional
    public <S, I> int deleteAllById(@NotNull Iterable<I> ids, Class<S> entityClass) {
        if (!ids.iterator().hasNext())
            return 0;

        List<I> idList = StreamSupport.stream(ids.spliterator(), false).toList();
        String tableAlias = "mt";
        QueryData queryPair = getJPQLWhereClauseFilteringByPrimaryKeys(idList, entityClass, tableAlias);
        EntityManager entityManager = jpaContext.getEntityManagerByManagedType(entityClass);
        Query query = entityManager.createQuery("DELETE FROM " + entityClass.getSimpleName() + " " + tableAlias +
                queryPair.getQuery());
        queryPair.getPositionBindings().forEach(query::setParameter);
        return query.executeUpdate();
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
    public <S> boolean update(S entity) {
        EntityManager entityManager = jpaContext.getEntityManagerByManagedType(entity.getClass());

        List<EntityField> columns = EntityReflectionUtils.getEntityColumns(entity).stream()
                .map(item -> {
                    // Set the column name to the field name for the JPQL query
                    item.setColumnName(item.getFieldName());
                    return item;
                }).toList();
        Map<Integer, Object> bindings = new HashMap<>(columns.size());
        StringBuilder columnsAndWhereClause = generateUpdateColumnsAndWhereClause(columns, bindings);
        String sqlQuery = UPDATE + entity.getClass().getSimpleName() + columnsAndWhereClause;

        Query query = entityManager.createQuery(sqlQuery);
        bindings.forEach(query::setParameter);
        return query.executeUpdate() > 0;
    }

    /**
     * {@inheritDoc}
     */
    @Transactional
    @Override
    public <S> boolean update(S entity, Set<String> fieldsToUpdate) {
        EntityManager entityManager = jpaContext.getEntityManagerByManagedType(entity.getClass());

        List<EntityField> columns = EntityReflectionUtils.getEntityColumns(entity).stream()
                .filter(item -> item.isId() || fieldsToUpdate.contains(item.getFieldName()))
                .map(item -> {
                    // Set the column name to the field name for the JPQL query
                    item.setColumnName(item.getFieldName());
                    return item;
                }).toList();
        if (columns.stream().allMatch(EntityField::isId))
            throw new IllegalArgumentException("The fields to update must contain at least one non-id field");

        Map<Integer, Object> bindings = new HashMap<>(columns.size());
        StringBuilder columnsAndWhereClause = generateUpdateColumnsAndWhereClause(columns, bindings);
        String sqlQuery = UPDATE + entity.getClass().getSimpleName() + columnsAndWhereClause;
        Query query = entityManager.createQuery(sqlQuery);
        bindings.forEach(query::setParameter);
        return query.executeUpdate() > 0;
    }

    /**
     * {@inheritDoc}
     */
    @Transactional
    @Override
    public <S> int updateAll(Iterable<S> entryList) {
        if (!entryList.iterator().hasNext()) {
            return 0;
        }

        List<S> entityList = StreamSupport.stream(entryList.spliterator(), false).toList();
        List<List<EntityField>> listColumns = EntityReflectionUtils.getEntityListColumns(entityList);

        listColumns.forEach(item -> item.forEach(field ->
                // Set the column name to the field name for the JPQL query
                field.setColumnName(field.getFieldName())
        ));

        int counter = 0;
        EntityManager entityManager = jpaContext.getEntityManagerByManagedType(entityList.get(0).getClass());
        for (int i = 0; i < entityList.size(); i++) {
            S entity = entityList.get(i);
            List<EntityField> columns = listColumns.get(i);
            Map<Integer, Object> bindings = new HashMap<>(columns.size());
            StringBuilder columnsAndWhereClause = generateUpdateColumnsAndWhereClause(columns, bindings);
            String sqlQuery = UPDATE + entity.getClass().getSimpleName() + columnsAndWhereClause;

            Query query = entityManager.createQuery(sqlQuery);
            bindings.forEach(query::setParameter);
            counter += query.executeUpdate();
        }

        return counter;
    }

    /**
     * {@inheritDoc}
     */
    @Transactional
    @Override
    public <S> int updateAll(Iterable<S> entryList, Set<String> fieldsToUpdate) {
        if (!entryList.iterator().hasNext()) {
            return 0;
        }

        List<S> entityList = StreamSupport.stream(entryList.spliterator(), false).toList();
        List<List<EntityField>> listColumns = EntityReflectionUtils.getEntityListColumns(entityList);

        listColumns = listColumns.stream().map(list -> list.stream()
                .filter(item -> item.isId() || fieldsToUpdate.contains(item.getFieldName()))
                .map(item -> {
                    // Set the column name to the field name for the JPQL query
                    item.setColumnName(item.getFieldName());
                    return item;
                }).toList()).toList();

        int counter = 0;
        EntityManager entityManager = jpaContext.getEntityManagerByManagedType(entityList.get(0).getClass());
        for (int i = 0; i < entityList.size(); i++) {
            S entity = entityList.get(i);
            List<EntityField> columns = listColumns.get(i);
            Map<Integer, Object> bindings = new HashMap<>(columns.size());
            StringBuilder columnsAndWhereClause = generateUpdateColumnsAndWhereClause(columns, bindings);
            String sqlQuery = UPDATE + entity.getClass().getSimpleName() + columnsAndWhereClause;

            Query query = entityManager.createQuery(sqlQuery);
            bindings.forEach(query::setParameter);
            counter += query.executeUpdate();
        }

        return counter;
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
    @Transactional(readOnly = true)
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

package io.github.dokkaltek.repository.addon;

import jakarta.persistence.EntityManager;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Repository addon to extend other repositories with {@link EntityManager} operations.
 */
public interface SimpleRepositoryAddon {

  /**
   * Finds an entity of any type in the database by the id.
   *
   * @param entityClass The entity class to find.
   * @param id          The id to find.
   * @param <S>    The entity type.
   * @param <I>    The id type.
   * @return The entity saved.
   */
  <S, I> Optional<S> find(Class<S> entityClass, I id);

  /**
   * Creates a registry in the database without checking if it exists.
   *
   * @param entity The entity to save.
   * @param <S>    The entity type.
   * @return The entity saved.
   */
  <S> S persist(S entity);

  /**
   * Creates a registry in the database without checking if it exists and flush the entry.
   *
   * @param entity The entity to save.
   * @param <S>    The entity type.
   * @return The entity saved.
   */
  <S> S persistAndFlush(S entity);

  /**
   * Creates all registries from a list in the database without checking if they exist.
   *
   * @param entryList The entries to save.
   * @param <S>       The entity type.
   * @return The entries saved.
   */
  <S> List<S> persistAll(Iterable<S> entryList);

  /**
   * Creates all registries from a list in the database without checking if it exists and flush the entries.
   *
   * @param entryList The entries to save.
   * @param <S>       The entity type.
   * @return The entries saved.
   */
  <S> List<S> persistAllAndFlush(Iterable<S> entryList);

  /**
   * Deletes a registry from the database without checking if it exists or not.
   *
   * @param entity The entity to delete.
   * @param <S>    The entity type.
   */
  <S> void remove(S entity);

  /**
   * Deletes a list of registry from the database without checking if they exist or not.
   *
   * @param entryList The list of entries to delete.
   * @param <S>    The entity type.
   */
  <S> void removeAll(Iterable<S> entryList);

  /**
   * Deletes all entities in the database by id without checking if they exist.
   * @param idList The ids of the entries to delete.
   * @param entityClass The entity class.
   */
  <S, I> void deleteByIdIn(@NotNull List<I> idList, Class<S> entityClass);

  /**
   * Updates a registry in the database without checking if it exists. When the
   * entity is managed it will perform an update, otherwise it will perform an insert.
   *
   * @param entity The entity to save.
   * @param <S>    The entity type.
   * @return The entity saved.
   */
  <S> S merge(S entity);

  /**
   * Updates a registry in the database without checking if it exists and flushes the entry. When the
   * entity is managed it will perform an update, otherwise it will perform an insert.
   *
   * @param entity The entity to save.
   * @param <S>    The entity type.
   * @return The entity saved.
   */
  <S> S mergeAndFlush(S entity);

  /**
   * Updates all registries from a list in the database without checking if they exist. When the
   * entity is managed it will perform an update, otherwise it will perform an insert.
   *
   * @param entryList The entries to save.
   * @param <S>       The entity type.
   * @return The entries saved.
   */
  <S> List<S> mergeAll(Iterable<S> entryList);

  /**
   * Updates all registries from a list in the database without checking if they exist and flushes the entries. When the
   * entity is managed it will perform an update, otherwise it will perform an insert.
   *
   * @param entryList The entries to save.
   * @param <S>       The entity type.
   * @return The entries saved.
   */
  <S> List<S> mergeAllAndFlush(Iterable<S> entryList);

  /**
   * Updates a registry in the database without checking if it exists and without checking if it's managed.
   * @param entity The entity to save.
   * @param <S>    The entity type.
   */
  <S> S update(S entity);

  /**
   * Updates the specified fields of a registry in the database.
   *
   * @param entity         The entity to save.
   * @param fieldsToUpdate The name of the fields to update.
   */
  <S> S update(S entity, Set<String> fieldsToUpdate);

  /**
   * Updates all registries from a list in the database without checking if they exist and without checking if
   * they are managed.
   * @param entryList The list of updated entries.
   * @param <S>    The entity type.
   */
  <S> List<S> updateAll(Iterable<S> entryList);

  /**
   * Updates the specified fields of all registries from a list in the database without checking if they exist
   * and without checking if they are managed.
   * @param entryList The list of updated entries.
   * @param <S>    The entity type.
   */
  <S> List<S> updateAll(Iterable<S> entryList, Set<String> fieldsToUpdate);

  /**
   * Detaches an entity from the managed context.
   *
   * @param entity The entity to detach.
   * @param <S>    The entity type.
   */
  <S> void detach(S entity);

  /**
   * Detaches an entity from the managed context.
   *
   * @param entryList The entity collection to detach.
   * @param <S>    The entity type.
   */
  <S> void detachAll(Iterable<S> entryList);

  /**
   * Refreshes the state of an entity from the database.
   *
   * @param entity The entity to refresh.
   * @param <S>    The entity type.
   */
  <S> void refresh(S entity);

  /**
   * Refreshes the state of a list of entities from the database.
   * Usually it's better to just create a new <code>findByxxx</code> method instead, since this will refresh
   * each item of the list one by one in the same transaction.
   *
   * @param entryList The entity collection to refresh.
   * @param <S>    The entity type.
   */
  <S> void refreshAll(Iterable<S> entryList);

  /**
   * Returns wether a given entity is managed.
   *
   * @param entity The entity to check if it's managed.
   * @param <S>    The entity type.
   */
  <S> boolean contains(S entity);

  /**
   * Returns wether a given set of entries are managed. It will return false if at least one of the entries
   * is not managed, or if the collection is empty.
   *
   * @param entryList The entries to check if they are managed.
   * @param <S>    The entity type.
   */
  <S> boolean containsAll(Iterable<S> entryList);

  /**
   * Gets the entity manager for the given class.
   * @param managedEntityClass The managed entity class.
   * @return The entity manager for the given entity.
   */
  <S> EntityManager getEntityManager(Class<S> managedEntityClass);
}

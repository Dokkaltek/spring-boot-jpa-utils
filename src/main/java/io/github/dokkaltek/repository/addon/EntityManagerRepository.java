package io.github.dokkaltek.repository.addon;

import jakarta.persistence.EntityManager;

import java.util.List;
import java.util.Optional;

/**
 * Repository addon to extend other repositories with {@link EntityManager} operations.
 *
 * @param <T> The entity class.
 */
public interface EntityManagerRepository<T, I> {
  /**
   * Finds an entity in the database by the id.
   *
   * @param entityClass The entity class to find.
   * @param id          The id to find.
   * @param <S>    The entity type.
   * @return The entity saved.
   */
  <S extends T> Optional<S> find(Class<S> entityClass, I id);

  /**
   * Creates a registry in the database without checking if it exists.
   *
   * @param entity The entity to save.
   * @param <S>    The entity type.
   * @return The entity saved.
   */
  <S extends T> S persist(S entity);

  /**
   * Creates a registry in the database without checking if it exists and flush the entry.
   *
   * @param entity The entity to save.
   * @param <S>    The entity type.
   * @return The entity saved.
   */
  <S extends T> S persistAndFlush(S entity);

  /**
   * Creates all registries from a list in the database without checking if they exist.
   *
   * @param entryList The entries to save.
   * @param <S>       The entity type.
   * @return The entries saved.
   */
  <S extends T> List<S> persistAll(Iterable<S> entryList);

  /**
   * Creates all registries from a list in the database without checking if it exists and flush the entries.
   *
   * @param entryList The entries to save.
   * @param <S>       The entity type.
   * @return The entries saved.
   */
  <S extends T> List<S> persistAllAndFlush(Iterable<S> entryList);

  /**
   * Deletes a registry from the database without checking if it exists or not.
   *
   * @param entity The entity to delete.
   * @param <S>    The entity type.
   */
  <S extends T> void removeWithoutChecks(S entity);

  /**
   * Updates a registry in the database without checking if it exists. When the
   * entity is managed it will perform an update, otherwise it will perform an insert.
   *
   * @param entity The entity to save.
   * @param <S>    The entity type.
   * @return The entity saved.
   */
  <S extends T> S merge(S entity);

  /**
   * Updates a registry in the database without checking if it exists and flushes the entry. When the
   * entity is managed it will perform an update, otherwise it will perform an insert.
   *
   * @param entity The entity to save.
   * @param <S>    The entity type.
   * @return The entity saved.
   */
  <S extends T> S mergeAndFlush(S entity);

  /**
   * Updates all registries from a list in the database without checking if they exist. When the
   * entity is managed it will perform an update, otherwise it will perform an insert.
   *
   * @param entryList The entries to save.
   * @param <S>       The entity type.
   * @return The entries saved.
   */
  <S extends T> List<S> mergeAll(Iterable<S> entryList);

  /**
   * Updates all registries from a list in the database without checking if they exist and flushes the entries. When the
   * entity is managed it will perform an update, otherwise it will perform an insert.
   *
   * @param entryList The entries to save.
   * @param <S>       The entity type.
   * @return The entries saved.
   */
  <S extends T> List<S> mergeAllAndFlush(Iterable<S> entryList);

  /**
   * Detaches an entity from the managed context.
   *
   * @param entity The entity to detach.
   * @param <S>    The entity type.
   */
  <S extends T> void detach(S entity);

  /**
   * Detaches an entity from the managed context.
   *
   * @param entryList The entity collection to detach.
   * @param <S>    The entity type.
   */
  <S extends T> void detachAll(Iterable<S> entryList);

  /**
   * Refreshes the state of an entity from the database.
   *
   * @param entity The entity to refresh.
   * @param <S>    The entity type.
   */
  <S extends T> void refresh(S entity);

  /**
   * Refreshes the state of a list of entities from the database.
   * Usually it's better to just create a new <code>findByxxx</code> method instead, since this will refresh
   * each item of the list one by one in the same transaction.
   *
   * @param entryList The entity collection to refresh.
   * @param <S>    The entity type.
   */
  <S extends T> void refreshAll(Iterable<S> entryList);

  /**
   * Returns wether a given entity is managed.
   *
   * @param entity The entity to refresh.
   * @param <S>    The entity type.
   */
  <S extends T> boolean contains(S entity);

  // TODO - Add entity graph, create native query, etc...
}

package io.github.dokkaltek.repository.addon;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repository addon to extend {@link JpaRepository} class with persist and merge operations.
 *
 * @param <T> The entity class.
 */
public interface PersistAndMergeRepositoryAddon<T> {
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
   * Updates a registry in the database without checking if it exists.
   *
   * @param entity The entity to save.
   * @param <S>    The entity type.
   * @return The entity saved.
   */
  <S extends T> S merge(S entity);

  /**
   * Updates a registry in the database without checking if it exists and flush the entry.
   *
   * @param entity The entity to save.
   * @param <S>    The entity type.
   * @return The entity saved.
   */
  <S extends T> S mergeAndFlush(S entity);

  /**
   * Updates all registries from a list in the database without checking if they exist.
   *
   * @param entryList The entries to save.
   * @param <S>       The entity type.
   * @return The entries saved.
   */
  <S extends T> List<S> mergeAll(Iterable<S> entryList);

  /**
   * Updates all registries from a list in the database without checking if they exist and flush the entries.
   *
   * @param entryList The entries to save.
   * @param <S>       The entity type.
   * @return The entries saved.
   */
  <S extends T> List<S> mergeAllAndFlush(Iterable<S> entryList);

  /**
   * Creates a registry in the database without checking if it exists for each element of each collection.
   * All collections should use the same transaction manager, which is derived from the main entity list passed.
   *
   * @param entryList     The entity to save.
   * @param extraEntities Other table entities to store in the same query.
   * @param <S>           The main entity type.
   */
  <S extends T> void insertAll(Iterable<S> entryList, Iterable<?>... extraEntities);

  /**
   * Creates a registry in the database without checking if it exists for each element of the collection.
   *
   * @param entryList The entity to save.
   * @param <S>       The entity type.
   */
  <S extends T> void insertAll(Iterable<S> entryList);

  /**
   * Creates a registry in the database calculating the id for each entry.
   *
   * @param entryList         The entity to save.
   * @param idParamToGenerate The id parameter to autogenerate by a table sequence.
   */
  <S extends T> void insertAllWithSequenceId(Iterable<S> entryList, String idParamToGenerate);
}

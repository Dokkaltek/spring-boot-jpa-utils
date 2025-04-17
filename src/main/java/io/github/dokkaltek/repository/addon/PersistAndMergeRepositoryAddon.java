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
   * Inserts all registries in the database without checking if they exist for each element of each collection.
   * All collections should use the same transaction manager, which is derived from the main entity list passed.
   * <br><br>
   * If more than one collection is passed, it inserts the entries using CTE's to insert all of them in one commit,
   * so make sure that your database supports CTE's if you pass more than a different entity collection at a time.
   * <br><br>
   * If you only pass one collection, it uses the <code>INSERT INTO ... VALUES ...</code> syntax.
   *
   * @param entryList     The entries to save.
   * @param extraEntities Other table entities to store in the same query.
   * @param <S>           The main entity type.
   */
  <S extends T> void insertAll(Iterable<S> entryList, Iterable<?>... extraEntities);

  /**
   * Inserts all registries in the database without checking if they exist for each element of each collection.
   * All collections should use the same transaction manager, which is derived from the main entity list passed.
   * <br><br>
   * <b>Oracle only</b>. This uses the <code>INSERT ALL INTO</code> syntax.
   *
   * @param entryList     The entries to save.
   * @param extraEntities Other table entities to store in the same query.
   * @param <S>           The main entity type.
   */
  <S extends T> void oracleInsertAll(Iterable<S> entryList, Iterable<?>... extraEntities);

  /**
   * Inserts all registries in the database calculating the id for each entry using the sequence from the
   * <code>@SequenceGenerator</code> annotation in the class. If you generate your sequence in some other way, you
   * can use the overridden method that passes the sequence name.
   * <br><br>
   * <b>Oracle only</b>. This uses the <code>INSERT ALL INTO</code> syntax.
   *
   * @param entryList         The entries to save.
   * @param idParamToGenerate The id parameter to autogenerate by a table sequence.
   */
  <S extends T> void oracleInsertAllWithSequenceId(Iterable<S> entryList, String idParamToGenerate);

  /**
   * Inserts all registries in the database calculating the id for each entry.
   * <br><br>
   * <b>Oracle only</b>. This uses the <code>INSERT ALL INTO</code> syntax.
   *
   * @param entryList         The entries to save.
   * @param idParamToGenerate The id parameter to autogenerate by a table sequence.
   * @param sequenceName The name of the sequence for the entity.
   */
  <S extends T> void oracleInsertAllWithSequenceId(Iterable<S> entryList, String idParamToGenerate,
                                                          String sequenceName);
}

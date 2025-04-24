package io.github.dokkaltek.repository.addon;

import jakarta.persistence.EntityManager;

/**
 * Repository addon to extend other repositories with {@link EntityManager} operations.
 *
 * @param <T> The entity class.
 * @param <I> The id of the entity class.
 */
public interface NativeRepository<T, I> {
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
   * Inserts all registries in the database without checking if they exist for each element of the collection
   * using CTEs.
   * It generates the given id parameter of each element with a sequence.
   *
   * @param entryList     The entries to save.
   * @param idParamToGenerate The id parameter to autogenerate by a table sequence.
   * @param <S>           The main entity type.
   */
  <S extends T> void insertAllWithSequenceId(Iterable<S> entryList, String idParamToGenerate);

  /**
   * Inserts all registries in the database without checking if they exist for each element of the collection
   * using CTEs.
   * It generates the given id parameter of each element with a sequence.
   *
   * @param entryList     The entries to save.
   * @param idParamToGenerate The id parameter to autogenerate by a table sequence.
   * @param sequenceName The name of the sequence to use for the entity.
   * @param <S>           The main entity type.
   */
  <S extends T> void insertAllWithSequenceId(Iterable<S> entryList, String idParamToGenerate, String sequenceName);

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
   * @param sequenceName The name of the sequence to use for the entity.
   */
  <S extends T> void oracleInsertAllWithSequenceId(Iterable<S> entryList, String idParamToGenerate,
                                                   String sequenceName);

  // TODO - Add update all, delete by id in, and implement insert all
}

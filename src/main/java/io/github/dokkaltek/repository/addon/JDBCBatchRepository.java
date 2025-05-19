package io.github.dokkaltek.repository.addon;

import io.github.dokkaltek.helper.EntriesWithSequence;
import jakarta.persistence.EntityManager;

import java.util.Collection;

/**
 * Repository addon to extend other repositories with {@link EntityManager} operations.
 * By default, this repository only takes into account MySQL, Oracle and Postgres, but their methods can be used for
 * other databases that use similar syntaxes.
 *
 * @param <T> The entity class.
 * @param <I> The id of the entity class.
 */
public interface JDBCBatchRepository<T, I> {

  /**
   * Inserts all registries in the database without checking if they exist for each element of each collection
   * using the <code>INSERT INTO ... VALUES ...</code> statement. This method doesn't have a limit to how many entries
   * can be inserted in the same query, so it's up to the user to split the list if needed.
   * <br><br>
   * If this method is called when using Oracle database with a version lower than 23, it will call the oracleInsertAll
   * method instead, which doesn't support sequences, so make sure to check if this is your case before calling this
   * method.
   *
   * @param entryList     The entries to save.
   * @param <S>           The main entity type.
   */
  <S extends T> void insertAll(Collection<S> entryList);

  /**
   * Inserts all registries in the database without checking if they exist for each element of each collection.
   * All entries must belong to the same transaction manager.
   * <br><br>
   * This uses JDBC batching, which is faster than using the batch methods from hibernate batch, but will only
   * make a difference if you are inserting values with a sequence or with their id's already calculated.
   *
   * @param entryList     The entries to save.
   * @param extraEntries  The extra collections of entries to save.
   * @param <S>           The main entity type.
   */
  <S extends T> void insertAllInBatch(Collection<S> entryList, Collection<?>... extraEntries);

  /**
   * Inserts all registries in the database without checking if they exist for each element of each collection.
   * All entries must belong to the same transaction manager.
   * <br><br>
   * This uses JDBC batching, which is faster than using the batch methods from hibernate batch, but will only
   * make a difference if you are inserting values with a sequence or with their id's already calculated.
   *
   * @param batchSize     The batch size.
   * @param entryList     The entries to save.
   * @param extraEntries  The extra collections of entries to save.
   * @param <S>           The main entity type.
   */
  <S extends T> void insertAllInBatchOfSize(int batchSize, Collection<S> entryList, Collection<?>... extraEntries);

  /**
   * Inserts all registries in the database without checking if they exist for each element of each collection. It will
   * generate the sequence value directly in the query calling the nextval function. If you want the database to
   * generate the sequence, use the {@link #insertAllInBatch(Collection, Collection...)} method instead.
   *<br><br>
   * This uses JDBC batching, which is faster than using the batch methods from hibernate batch. If you don't pass the
   * name of the sequence, it will resolve the sequence name from the @SequenceGenerator annotation, otherwise it will
   * throw an exception.
   *
   * @param entryList     The entries to save with the sequence.
   * @throws io.github.dokkaltek.exception.EntityReflectionException In case the sequence name can't be resolved.
   */
  void insertAllInBatchWithSequence(Collection<EntriesWithSequence> entryList);

  /**
   * Inserts all registries in the database without checking if they exist for each element of each collection. It will
   * generate the sequence value directly in the query calling the nextval function. If you want the database to
   * generate the sequence, use the {@link #insertAllInBatch(Collection, Collection...)} method instead.
   *<br><br>
   * This uses JDBC batching, which is faster than using the batch methods from hibernate batch. If you don't pass the
   * name of the sequence, it will resolve the sequence name from the @SequenceGenerator annotation, otherwise it will
   * throw an exception.
   *
   * @param batchSize     The batch size.
   * @param entryList     The entries to save with the sequence.
   * @throws io.github.dokkaltek.exception.EntityReflectionException In case the sequence name can't be resolved.
   */
  void insertAllInBatchOfSizeWithSequence(int batchSize, Collection<EntriesWithSequence> entryList);

  /**
   * Inserts all registries in the database without checking if they exist for each element of each collection.
   * All collections should use the same transaction manager, which is derived from the main entity list passed.
   * <br><br>
   * <b>Oracle only</b>. This uses the <code>INSERT ALL INTO</code> syntax.
   *
   * @param entriesToInsert Entries to insert.
   */
  void oracleInsertAll(Collection<?>... entriesToInsert);

  /**
   * Inserts all registries in the database calculating the id for each entry using the sequence from the
   * <code>@SequenceGenerator</code> annotation in the class if no sequenceName was provided.
   * <br><br>
   * <b>Oracle only</b>. This uses the <code>INSERT ALL INTO</code> syntax.
   *
   * @param entryList         The entries to save.
   */
  void oracleInsertAllWithSequenceId(Collection<EntriesWithSequence> entryList);

  /**
   * Updates all registries in the database without checking if they exist for each element of each collection.
   * @param entryList The entries to update.
   * @param extraEntries The extra collections of entries to update.
   * @param <S> The main entity type.
   */
  <S extends T> void updateAllInBatch(Collection<S> entryList, Collection<?>... extraEntries);

  /**
   * Updates all registries in the database without checking if they exist for each element of each collection.
   * @param batchSize The batch size.
   * @param entryList The entries to update.
   * @param extraEntries The extra collections of entries to update.
   * @param <S> The main entity type.
   */
  <S extends T> void updateAllInBatchOfSize(int batchSize, Collection<S> entryList, Collection<?>... extraEntries);

  /**
   * Deletes all entities in the database by id without checking if they exist.
   * @param idList The list of ids of the entries to delete.
   */
  void deleteByIdIn(Collection<I>... idList);

  /**
   * Deletes all entities in the database without checking if they exist in batch.
   * @param entryList The main list of entries to delete.
   * @param extraEntities The extra collections of entries to delete.
   */
  <S extends T> void deleteAllInBatch(Collection<S> entryList, Collection<?>... extraEntities);

  /**
   * Deletes all entities in the database without checking if they exist in batch.
   * @param batchSize The batch size.
   * @param entryList The main list of entries to delete.
   * @param extraEntities The extra collections of entries to delete.
   */
  <S extends T> void deleteAllInBatchOfSize(int batchSize, Collection<S> entryList, Collection<?>... extraEntities);
}

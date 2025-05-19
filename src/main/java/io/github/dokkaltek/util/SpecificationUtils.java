package io.github.dokkaltek.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Utility class to perform operations related to specifications.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SpecificationUtils {

  /**
   * Generates a specification to check a nullable field value for any object.
   *
   * @param paramToSearch The value of the parameter to search.
   * @param paramPair     The name of the parameter in the object along with the class of the object
   *                      to generate the spec for.
   * @return The {@link Specification} object that belongs to the custom query.
   */
  public static <T> Specification<T> getNullableValSpec(Object paramToSearch,
                                                        Pair<String, Class<T>> paramPair) {
    return (Root<T> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) ->
      matchNullableValCriteria(splitRoot(root, paramPair.getFirst()), criteriaBuilder, paramToSearch);
  }

  /**
   * Generates a specification to check if any field has a value in any object.
   *
   * @param paramToSearch The value of the parameter to search.
   * @param paramPair     The name of the parameter in the object along with the class of the object
   *                      to generate the spec for.
   * @return The {@link Specification} object that belongs to the custom query.
   */
  public static <T> Specification<T> getExactValSpec(Object paramToSearch, Pair<String, Class<T>> paramPair) {
    return (Root<T> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) ->
      matchExactValCriteria(splitRoot(root, paramPair.getFirst()), criteriaBuilder, paramToSearch);
  }

  /**
   * Divide root if embedded
   *
   * @param root Root to be mapped.
   * @param s    String to be divided.
   * @return The {@link Path} of the string.
   */
  public <T> Path<T> splitRoot(Root<T> root, String s) {
    Path<T> solution;
    if (s.contains(".")) {
      String[] split = s.split("\\.");
      solution = root.get(split[0]);
      for (int i = 1; i < split.length; i++) {
        solution = solution.get(split[i]);
      }
    } else {
      solution = root.get(s);
    }
    return solution;
  }

  /**
   * Generates a specification to compare if the entity date is greater than a passed date for
   * the object.
   *
   * @param dateToCompare The value of the parameter to search.
   * @param paramPair     The name of the parameter in the object along with the class of the object
   *                      to generate the spec for.
   * @return The {@link Specification} object that belongs to the custom query.
   */
  public static <T> Specification<T> getGreaterOrEqualsDateThanSpec(Date dateToCompare,
                                                                    Pair<String, Class<T>> paramPair) {
    return (Root<T> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) ->
      matchGreaterOrEqualsThanDateCriteria(splitRoot(root, paramPair.getFirst()), criteriaBuilder,
        dateToCompare);
  }

  /**
   * Generates a specification to compare if the entity date is lesser than a passed date for
   * any object.
   *
   * @param dateToCompare The value of the parameter to search.
   * @param paramPair     The name of the parameter in the object along with the class of the object
   *                      to generate the spec for.
   * @return The {@link Specification} object that belongs to the custom query.
   */
  public static <T> Specification<T> getLesserDateThanSpec(Date dateToCompare,
                                                           Pair<String, Class<T>> paramPair) {
    return (Root<T> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) ->
      matchLesserThanDateCriteria(splitRoot(root, paramPair.getFirst()), criteriaBuilder, dateToCompare);
  }

  /**
   * The criteria matcher to search by nullable arguments in the database.
   *
   * @param rootPath        The {@link Expression} field of the object to search for.
   * @param criteriaBuilder The criteria builder to generate the custom query.
   * @param dateToCompare   The value of the date being compared to the one in the database.
   * @return The Predicate generated.
   */
  private Predicate matchGreaterOrEqualsThanDateCriteria(Path<?> rootPath, CriteriaBuilder criteriaBuilder,
                                                         Date dateToCompare) {
    if (dateToCompare != null) {
      return criteriaBuilder.greaterThanOrEqualTo(rootPath.as(Date.class), dateToCompare);
    } else {
      return criteriaBuilder.isTrue(criteriaBuilder.literal(Boolean.TRUE));
    }
  }

  /**
   * The criteria matcher to search by nullable arguments in the database.
   *
   * @param rootPath        The {@link Expression} field of the object to search for.
   * @param criteriaBuilder The criteria builder to generate the custom query.
   * @param dateToCompare   The value of the date being compared to the one in the database.
   * @return The Predicate generated.
   */
  private Predicate matchLesserThanDateCriteria(Path<?> rootPath, CriteriaBuilder criteriaBuilder,
                                                Date dateToCompare) {
    if (dateToCompare != null) {
      return criteriaBuilder.lessThanOrEqualTo(rootPath.as(Date.class), dateToCompare);
    } else {
      return criteriaBuilder.isTrue(criteriaBuilder.literal(Boolean.TRUE));
    }
  }

  /**
   * The criteria matcher to search by nullable arguments in the database.
   *
   * @param rootPath        The {@link Expression} field of the object to search for.
   * @param criteriaBuilder The criteria builder to generate the custom query.
   * @param paramToSearch   The value of the parameter to search for.
   * @return The Predicate generated.
   */
  private Predicate matchNullableValCriteria(Path<?> rootPath, CriteriaBuilder criteriaBuilder,
                                             Object paramToSearch) {
    Predicate nullVal = criteriaBuilder.isNull(rootPath);

    if (paramToSearch != null) {
      Predicate validVal = criteriaBuilder.equal(rootPath, paramToSearch);
      return criteriaBuilder.or(validVal, nullVal);
    } else {
      return nullVal;
    }
  }

  /**
   * The criteria matcher to search by nullable arguments in the database.
   *
   * @param rootPath        The {@link Expression} field of the object to search for.
   * @param criteriaBuilder The criteria builder to generate the custom query.
   * @param paramToSearch   The value of the parameter to search for.
   * @return The Predicate generated.
   */
  private Predicate matchExactValCriteria(Path<?> rootPath, CriteriaBuilder criteriaBuilder,
                                          Object paramToSearch) {
    if (paramToSearch != null) {
      return criteriaBuilder.equal(rootPath, paramToSearch);
    } else {
      return criteriaBuilder.isNull(rootPath);
    }
  }
}

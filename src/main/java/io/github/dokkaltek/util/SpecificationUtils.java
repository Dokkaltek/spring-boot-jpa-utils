package io.github.dokkaltek.util;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Utility class to perform operations related to specifications.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SpecificationUtils {
    private static final Pattern DOT_PATTERN = Pattern.compile("\\.");

    /**
     * Generates a specification that matches any row that contains null or the value for the passed field.
     *
     * @param valueToSearch The value of the parameter to search.
     * @param parameterName The name of the parameter in the object.
     * @return The {@link Specification} object that belongs to the custom query.
     */
    public static <T> Specification<T> matchNullableValue(Object valueToSearch,
                                                          String parameterName) {
        return (Root<T> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) ->
                matchNullableValCriteria(splitRoot(root, parameterName), criteriaBuilder, valueToSearch);
    }

    /**
     * Generates a specification to check if any field has the exact value of the passed object.
     *
     * @param valueToSearch The value of the parameter to search.
     * @param parameterName The name of the parameter in the object.
     * @return The {@link Specification} object that belongs to the custom query.
     */
    public static <T> Specification<T> matchExactValue(Object valueToSearch, String parameterName) {
        return (Root<T> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) ->
                matchExactValCriteria(splitRoot(root, parameterName), criteriaBuilder, valueToSearch);
    }

    /**
     * Generates a specification to compare if the passed date is greater the compared parameter
     *
     * @param dateToCompare The value of the parameter to search.
     * @param parameterName The name of the parameter in the object.
     * @return The {@link Specification} object that belongs to the custom query.
     */
    public static <T> Specification<T> matchGreaterDateThan(Date dateToCompare,
                                                            String parameterName) {
        return (Root<T> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) ->
                matchGreaterThanDateCriteria(splitRoot(root, parameterName), criteriaBuilder,
                        dateToCompare);
    }

    /**
     * Generates a specification to compare if the passed date is greater the compared parameter
     *
     * @param dateToCompare The value of the parameter to search.
     * @param parameterName The name of the parameter in the object.
     * @return The {@link Specification} object that belongs to the custom query.
     */
    public static <T> Specification<T> matchGreaterDateThan(LocalDate dateToCompare,
                                                            String parameterName) {
        return (Root<T> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) ->
                matchGreaterThanDateCriteria(splitRoot(root, parameterName), criteriaBuilder,
                        dateToCompare);
    }

    /**
     * Generates a specification to compare if the passed date is greater the compared parameter
     *
     * @param dateToCompare The value of the parameter to search.
     * @param parameterName The name of the parameter in the object.
     * @return The {@link Specification} object that belongs to the custom query.
     */
    public static <T> Specification<T> matchGreaterDateThan(LocalTime dateToCompare,
                                                            String parameterName) {
        return (Root<T> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) ->
                matchGreaterThanDateCriteria(splitRoot(root, parameterName), criteriaBuilder,
                        dateToCompare);
    }

    /**
     * Generates a specification to compare if the passed date is greater the compared parameter
     *
     * @param dateToCompare The value of the parameter to search.
     * @param parameterName The name of the parameter in the object.
     * @return The {@link Specification} object that belongs to the custom query.
     */
    public static <T> Specification<T> matchGreaterDateThan(LocalDateTime dateToCompare,
                                                            String parameterName) {
        return (Root<T> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) ->
                matchGreaterThanDateCriteria(splitRoot(root, parameterName), criteriaBuilder,
                        dateToCompare);
    }

    /**
     * Generates a specification to compare if the passed date is greater the compared parameter
     *
     * @param dateToCompare The value of the parameter to search.
     * @param parameterName The name of the parameter in the object.
     * @return The {@link Specification} object that belongs to the custom query.
     */
    public static <T> Specification<T> matchGreaterDateThan(OffsetTime dateToCompare,
                                                            String parameterName) {
        return (Root<T> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) ->
                matchGreaterThanDateCriteria(splitRoot(root, parameterName), criteriaBuilder,
                        dateToCompare);
    }

    /**
     * Generates a specification to compare if the passed date is greater the compared parameter
     *
     * @param dateToCompare The value of the parameter to search.
     * @param parameterName The name of the parameter in the object.
     * @return The {@link Specification} object that belongs to the custom query.
     */
    public static <T> Specification<T> matchGreaterDateThan(OffsetDateTime dateToCompare,
                                                            String parameterName) {
        return (Root<T> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) ->
                matchGreaterThanDateCriteria(splitRoot(root, parameterName), criteriaBuilder,
                        dateToCompare);
    }

    /**
     * Generates a specification to compare if the passed date is greater the compared parameter
     *
     * @param numberToCompare The value of the parameter to search.
     * @param parameterName The name of the parameter in the object.
     * @return The {@link Specification} object that belongs to the custom query.
     */
    public static <T> Specification<T> matchGreaterNumberThan(Number numberToCompare,
                                                              String parameterName) {
        return (Root<T> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) ->
                matchGreaterThanNumberCriteria(splitRoot(root, parameterName), criteriaBuilder,
                        numberToCompare);
    }

    /**
     * Generates a specification to compare if the entity column is greater than a passed date for
     * the object.
     *
     * @param dateToCompare The value of the parameter to search.
     * @param parameterName The name of the parameter in the object.
     * @return The {@link Specification} object that belongs to the custom query.
     */
    public static <T> Specification<T> matchGreaterOrEqualDateThan(Date dateToCompare,
                                                                   String parameterName) {
        return (Root<T> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) ->
                matchGreaterOrEqualsThanDateCriteria(splitRoot(root, parameterName), criteriaBuilder,
                        dateToCompare);
    }

    /**
     * Generates a specification to compare if the entity column is greater than a passed date for
     * the object.
     *
     * @param dateToCompare The value of the parameter to search.
     * @param parameterName The name of the parameter in the object.
     * @return The {@link Specification} object that belongs to the custom query.
     */
    public static <T> Specification<T> matchGreaterOrEqualDateThan(LocalDate dateToCompare,
                                                                   String parameterName) {
        return (Root<T> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) ->
                matchGreaterOrEqualsThanDateCriteria(splitRoot(root, parameterName), criteriaBuilder,
                        dateToCompare);
    }

    /**
     * Generates a specification to compare if the entity column is greater than a passed date for
     * the object.
     *
     * @param dateToCompare The value of the parameter to search.
     * @param parameterName The name of the parameter in the object.
     * @return The {@link Specification} object that belongs to the custom query.
     */
    public static <T> Specification<T> matchGreaterOrEqualDateThan(LocalTime dateToCompare,
                                                                   String parameterName) {
        return (Root<T> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) ->
                matchGreaterOrEqualsThanDateCriteria(splitRoot(root, parameterName), criteriaBuilder,
                        dateToCompare);
    }

    /**
     * Generates a specification to compare if the entity column is greater than a passed date for
     * the object.
     *
     * @param dateToCompare The value of the parameter to search.
     * @param parameterName The name of the parameter in the object.
     * @return The {@link Specification} object that belongs to the custom query.
     */
    public static <T> Specification<T> matchGreaterOrEqualDateThan(LocalDateTime dateToCompare,
                                                                   String parameterName) {
        return (Root<T> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) ->
                matchGreaterOrEqualsThanDateCriteria(splitRoot(root, parameterName), criteriaBuilder,
                        dateToCompare);
    }

    /**
     * Generates a specification to compare if the entity column is greater than a passed date for
     * the object.
     *
     * @param dateToCompare The value of the parameter to search.
     * @param parameterName The name of the parameter in the object.
     * @return The {@link Specification} object that belongs to the custom query.
     */
    public static <T> Specification<T> matchGreaterOrEqualDateThan(OffsetTime dateToCompare,
                                                                   String parameterName) {
        return (Root<T> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) ->
                matchGreaterOrEqualsThanDateCriteria(splitRoot(root, parameterName), criteriaBuilder,
                        dateToCompare);
    }

    /**
     * Generates a specification to compare if the entity column is greater than a passed date for
     * the object.
     *
     * @param dateToCompare The value of the parameter to search.
     * @param parameterName The name of the parameter in the object.
     * @return The {@link Specification} object that belongs to the custom query.
     */
    public static <T> Specification<T> matchGreaterOrEqualDateThan(OffsetDateTime dateToCompare,
                                                                   String parameterName) {
        return (Root<T> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) ->
                matchGreaterOrEqualsThanDateCriteria(splitRoot(root, parameterName), criteriaBuilder,
                        dateToCompare);
    }

    /**
     * Generates a specification to compare if entity column is greater than a passed number for
     * the object.
     *
     * @param numberToCompare The value of the parameter to search.
     * @param parameterName   The name of the parameter in the object.
     * @return The {@link Specification} object that belongs to the custom query.
     */
    public static <T> Specification<T> matchGreaterOrEqualNumberThan(Number numberToCompare,
                                                                     String parameterName) {
        return (Root<T> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) ->
                matchGreaterOrEqualsThanNumberCriteria(splitRoot(root, parameterName), criteriaBuilder,
                        numberToCompare);
    }

    /**
     * Generates a specification to compare if the entity column is lesser than a passed date for
     * any object.
     *
     * @param dateToCompare The value of the parameter to search.
     * @param parameterName The name of the parameter in the object.
     * @return The {@link Specification} object that belongs to the custom query.
     */
    public static <T> Specification<T> matchLesserDateThan(Date dateToCompare,
                                                           String parameterName) {
        return (Root<T> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) ->
                matchLesserThanDateCriteria(splitRoot(root, parameterName), criteriaBuilder, dateToCompare);
    }

    /**
     * Generates a specification to compare if the entity column is lesser than a passed date for
     * any object.
     *
     * @param dateToCompare The value of the parameter to search.
     * @param parameterName The name of the parameter in the object.
     * @return The {@link Specification} object that belongs to the custom query.
     */
    public static <T> Specification<T> matchLesserDateThan(LocalDate dateToCompare,
                                                           String parameterName) {
        return (Root<T> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) ->
                matchLesserThanDateCriteria(splitRoot(root, parameterName), criteriaBuilder, dateToCompare);
    }

    /**
     * Generates a specification to compare if the entity column is lesser than a passed date for
     * any object.
     *
     * @param dateToCompare The value of the parameter to search.
     * @param parameterName The name of the parameter in the object.
     * @return The {@link Specification} object that belongs to the custom query.
     */
    public static <T> Specification<T> matchLesserDateThan(LocalTime dateToCompare,
                                                           String parameterName) {
        return (Root<T> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) ->
                matchLesserThanDateCriteria(splitRoot(root, parameterName), criteriaBuilder, dateToCompare);
    }

    /**
     * Generates a specification to compare if the entity column is lesser than a passed date for
     * any object.
     *
     * @param dateToCompare The value of the parameter to search.
     * @param parameterName The name of the parameter in the object.
     * @return The {@link Specification} object that belongs to the custom query.
     */
    public static <T> Specification<T> matchLesserDateThan(LocalDateTime dateToCompare,
                                                           String parameterName) {
        return (Root<T> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) ->
                matchLesserThanDateCriteria(splitRoot(root, parameterName), criteriaBuilder, dateToCompare);
    }

    /**
     * Generates a specification to compare if the entity column is lesser than a passed date for
     * any object.
     *
     * @param dateToCompare The value of the parameter to search.
     * @param parameterName The name of the parameter in the object.
     * @return The {@link Specification} object that belongs to the custom query.
     */
    public static <T> Specification<T> matchLesserDateThan(OffsetTime dateToCompare,
                                                           String parameterName) {
        return (Root<T> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) ->
                matchLesserThanDateCriteria(splitRoot(root, parameterName), criteriaBuilder, dateToCompare);
    }

    /**
     * Generates a specification to compare if the entity column is lesser than a passed date for
     * any object.
     *
     * @param dateToCompare The value of the parameter to search.
     * @param parameterName The name of the parameter in the object.
     * @return The {@link Specification} object that belongs to the custom query.
     */
    public static <T> Specification<T> matchLesserDateThan(OffsetDateTime dateToCompare,
                                                           String parameterName) {
        return (Root<T> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) ->
                matchLesserThanDateCriteria(splitRoot(root, parameterName), criteriaBuilder, dateToCompare);
    }

    /**
     * Generates a specification to compare if the entity column is lesser than a passed number for
     * any object.
     *
     * @param numberToCompare The value of the parameter to search.
     * @param parameterName   The name of the parameter in the object.
     * @return The {@link Specification} object that belongs to the custom query.
     */
    public static <T> Specification<T> matchLesserNumberThan(Number numberToCompare,
                                                             String parameterName) {
        return (Root<T> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) ->
                matchLesserThanNumberCriteria(splitRoot(root, parameterName), criteriaBuilder,
                        numberToCompare);
    }

    /**
     * Generates a specification to compare if the entity column is lesser than a passed date for
     * any object.
     *
     * @param dateToCompare The value of the parameter to search.
     * @param parameterName The name of the parameter in the object.
     * @return The {@link Specification} object that belongs to the custom query.
     */
    public static <T> Specification<T> matchLesserOrEqualDateThan(Date dateToCompare,
                                                                  String parameterName) {
        return (Root<T> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) ->
                matchLesserOrEqualsThanDateCriteria(splitRoot(root, parameterName), criteriaBuilder,
                        dateToCompare);
    }

    /**
     * Generates a specification to compare if the entity column is lesser than a passed date for
     * any object.
     *
     * @param dateToCompare The value of the parameter to search.
     * @param parameterName The name of the parameter in the object.
     * @return The {@link Specification} object that belongs to the custom query.
     */
    public static <T> Specification<T> matchLesserOrEqualDateThan(LocalDate dateToCompare,
                                                                  String parameterName) {
        return (Root<T> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) ->
                matchLesserOrEqualsThanDateCriteria(splitRoot(root, parameterName), criteriaBuilder,
                        dateToCompare);
    }

    /**
     * Generates a specification to compare if the entity column is lesser than a passed date for
     * any object.
     *
     * @param dateToCompare The value of the parameter to search.
     * @param parameterName The name of the parameter in the object.
     * @return The {@link Specification} object that belongs to the custom query.
     */
    public static <T> Specification<T> matchLesserOrEqualDateThan(LocalTime dateToCompare,
                                                                  String parameterName) {
        return (Root<T> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) ->
                matchLesserOrEqualsThanDateCriteria(splitRoot(root, parameterName), criteriaBuilder,
                        dateToCompare);
    }

    /**
     * Generates a specification to compare if the entity column is lesser than a passed date for
     * any object.
     *
     * @param dateToCompare The value of the parameter to search.
     * @param parameterName The name of the parameter in the object.
     * @return The {@link Specification} object that belongs to the custom query.
     */
    public static <T> Specification<T> matchLesserOrEqualDateThan(LocalDateTime dateToCompare,
                                                                  String parameterName) {
        return (Root<T> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) ->
                matchLesserOrEqualsThanDateCriteria(splitRoot(root, parameterName), criteriaBuilder,
                        dateToCompare);
    }

    /**
     * Generates a specification to compare if the entity column is lesser than a passed date for
     * any object.
     *
     * @param dateToCompare The value of the parameter to search.
     * @param parameterName The name of the parameter in the object.
     * @return The {@link Specification} object that belongs to the custom query.
     */
    public static <T> Specification<T> matchLesserOrEqualDateThan(OffsetTime dateToCompare,
                                                                  String parameterName) {
        return (Root<T> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) ->
                matchLesserOrEqualsThanDateCriteria(splitRoot(root, parameterName), criteriaBuilder,
                        dateToCompare);
    }

    /**
     * Generates a specification to compare if the entity column is lesser than a passed date for
     * any object.
     *
     * @param dateToCompare The value of the parameter to search.
     * @param parameterName The name of the parameter in the object.
     * @return The {@link Specification} object that belongs to the custom query.
     */
    public static <T> Specification<T> matchLesserOrEqualDateThan(OffsetDateTime dateToCompare,
                                                                  String parameterName) {
        return (Root<T> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) ->
                matchLesserOrEqualsThanDateCriteria(splitRoot(root, parameterName), criteriaBuilder,
                        dateToCompare);
    }

    /**
     * Generates a specification to compare if the entity column is lesser than a passed number for
     * any object.
     *
     * @param numberToCompare The value of the parameter to search.
     * @param parameterName   The name of the parameter in the object.
     * @return The {@link Specification} object that belongs to the custom query.
     */
    public static <T> Specification<T> matchLesserOrEqualNumberThan(Number numberToCompare,
                                                                    String parameterName) {
        return (Root<T> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) ->
                matchLesserOrEqualsThanNumberCriteria(splitRoot(root, parameterName), criteriaBuilder,
                        numberToCompare);
    }

    /**
     * Generates a specification to match a column between two dates (both inclusive).
     *
     * @param startDate The initial Date.
     * @param endDate   The end Date.
     * @param parameterName The name of the parameter in the object.
     * @param <T>           The class of the object to generate the spec for.
     * @return The {@link Specification} object that belongs to the custom query.
     */
    public static <T> Specification<T> matchDateBetween(Date startDate, Date endDate, String parameterName) {
        return (Root<T> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) ->
                matchDateBetweenCriteria(splitRoot(root, parameterName), criteriaBuilder,
                        startDate, endDate);
    }

    /**
     * Generates a specification to match a column between two dates (both inclusive).
     *
     * @param startDate The initial Date.
     * @param endDate   The end Date.
     * @param parameterName The name of the parameter in the object.
     * @param <T>           The class of the object to generate the spec for.
     * @return The {@link Specification} object that belongs to the custom query.
     */
    public static <T> Specification<T> matchDateBetween(LocalDate startDate, LocalDate endDate, String parameterName) {
        return (Root<T> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) ->
                matchDateBetweenCriteria(splitRoot(root, parameterName), criteriaBuilder,
                        startDate, endDate);
    }

    /**
     * Generates a specification to match a column between two dates (both inclusive).
     *
     * @param startDate The initial Date.
     * @param endDate   The end Date.
     * @param parameterName The name of the parameter in the object.
     * @param <T>           The class of the object to generate the spec for.
     * @return The {@link Specification} object that belongs to the custom query.
     */
    public static <T> Specification<T> matchDateBetween(LocalTime startDate, LocalTime endDate, String parameterName) {
        return (Root<T> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) ->
                matchDateBetweenCriteria(splitRoot(root, parameterName), criteriaBuilder,
                        startDate, endDate);
    }

    /**
     * Generates a specification to match a column between two dates (both inclusive).
     *
     * @param startDate The initial Date.
     * @param endDate   The end Date.
     * @param parameterName The name of the parameter in the object.
     * @param <T>           The class of the object to generate the spec for.
     * @return The {@link Specification} object that belongs to the custom query.
     */
    public static <T> Specification<T> matchDateBetween(LocalDateTime startDate, LocalDateTime endDate, String parameterName) {
        return (Root<T> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) ->
                matchDateBetweenCriteria(splitRoot(root, parameterName), criteriaBuilder,
                        startDate, endDate);
    }

    /**
     * Generates a specification to match a column between two dates (both inclusive).
     *
     * @param startDate The initial Date.
     * @param endDate   The end Date.
     * @param parameterName The name of the parameter in the object.
     * @param <T>           The class of the object to generate the spec for.
     * @return The {@link Specification} object that belongs to the custom query.
     */
    public static <T> Specification<T> matchDateBetween(OffsetTime startDate, OffsetTime endDate, String parameterName) {
        return (Root<T> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) ->
                matchDateBetweenCriteria(splitRoot(root, parameterName), criteriaBuilder,
                        startDate, endDate);
    }

    /**
     * Generates a specification to match a column between two dates (both inclusive).
     *
     * @param startDate The initial Date.
     * @param endDate   The end Date.
     * @param parameterName The name of the parameter in the object.
     * @param <T>           The class of the object to generate the spec for.
     * @return The {@link Specification} object that belongs to the custom query.
     */
    public static <T> Specification<T> matchDateBetween(OffsetDateTime startDate, OffsetDateTime endDate,
                                                        String parameterName) {
        return (Root<T> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) ->
                matchDateBetweenCriteria(splitRoot(root, parameterName), criteriaBuilder,
                        startDate, endDate);
    }

    /**
     * Generates a specification to match a column between two numbers (both inclusive).
     *
     * @param lowestNumber The lowest number to compare.
     * @param highestNumber   The highest number to compare.
     * @param parameterName The name of the parameter in the object.
     * @param <T>           The class of the object to generate the spec for.
     * @return The {@link Specification} object that belongs to the custom query.
     */
    public static <T> Specification<T> matchNumberBetween(Number lowestNumber, Number highestNumber,
                                                        String parameterName) {
        return (Root<T> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) ->
                matchNumberBetweenCriteria(splitRoot(root, parameterName), criteriaBuilder,
                        lowestNumber, highestNumber);
    }

    /**
     * Generates a specification to only match if the parameter is true.
     *
     * @param parameterName The name of the parameter in the object.
     * @param <T>           The class of the object to generate the spec for.
     * @return The {@link Specification} object that belongs to the custom query.
     */
    public static <T> Specification<T> matchIfTrue(String parameterName) {
        return (Root<T> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) ->
                criteriaBuilder.isTrue(splitRoot(root, parameterName).as(Boolean.class));
    }

    /**
     * Generates a specification to only match if the parameter is false.
     *
     * @param parameterName The name of the parameter in the object.
     * @param <T>           The class of the object to generate the spec for.
     * @return The {@link Specification} object that belongs to the custom query.
     */
    public static <T> Specification<T> matchIfFalse(String parameterName) {
        return (Root<T> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) ->
                criteriaBuilder.isFalse(splitRoot(root, parameterName).as(Boolean.class));
    }

    /**
     * Generates a specification to only match if the column is false.
     *
     * @param parameterName The name of the parameter in the object.
     * @param <T>           The class of the object to generate the spec for.
     * @return The {@link Specification} object that belongs to the custom query.
     */
    public static <T> Specification<T> matchIfNullOrFalse(String parameterName) {
        return (Root<T> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) ->
                criteriaBuilder.or(criteriaBuilder.isFalse(splitRoot(root, parameterName).as(Boolean.class)),
                        criteriaBuilder.isNull(splitRoot(root, parameterName)));
    }

    /**
     * Generates a specification to only match if the column is null.
     *
     * @param parameterName The name of the parameter in the object.
     * @param <T>           The class of the object to generate the spec for.
     * @return The {@link Specification} object that belongs to the custom query.
     */
    public static <T> Specification<T> matchIfNull(String parameterName) {
        return (Root<T> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) ->
                criteriaBuilder.isNull(splitRoot(root, parameterName));
    }

    /**
     * Generates a specification to only match if the column is null.
     *
     * @param parameterName The name of the parameter in the object.
     * @param <T>           The class of the object to generate the spec for.
     * @return The {@link Specification} object that belongs to the custom query.
     */
    public static <T> Specification<T> matchIfNotNull(String parameterName) {
        return (Root<T> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) ->
                criteriaBuilder.not(criteriaBuilder.isNull(splitRoot(root, parameterName)));
    }

    /**
     * Generates a specification to match if the parameter matches the passed boolean value, or is null in case the
     * value to match is null or false.
     *
     * @param parameterName The name of the parameter in the object.
     * @param <T>           The class of the object to generate the spec for.
     * @return The {@link Specification} object that belongs to the custom query.
     */
    public static <T> Specification<T> matchBooleanValue(Boolean valueToMatch, String parameterName) {
        return (Root<T> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) ->
                matchBooleanValCriteria(splitRoot(root, parameterName), criteriaBuilder, valueToMatch);
    }

    /**
     * Generates a specification to check if the passed string is like the parameter.
     * <br>
     * The method passes the parameter to the like method as-is.
     *
     * @param parameterName The name of the parameter in the object.
     * @param <T>           The class of the object to generate the spec for.
     * @return The {@link Specification} object that belongs to the custom query.
     */
    public static <T> Specification<T> matchStringRawLike(String valueToMatch, String parameterName) {
        return (Root<T> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) ->
                matchStringLikeCriteria(splitRoot(root, parameterName), criteriaBuilder, valueToMatch);
    }

    /**
     * Generates a specification to check if the passed string is like the parameter.
     * <br>
     * The method surrounds the parameter with '%' characters.
     *
     * @param parameterName The name of the parameter in the object.
     * @param <T>           The class of the object to generate the spec for.
     * @return The {@link Specification} object that belongs to the custom query.
     */
    public static <T> Specification<T> matchStringLike(String valueToMatch, String parameterName) {
        return (Root<T> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) ->
                matchStringLikeCriteria(splitRoot(root, parameterName), criteriaBuilder, "%" + valueToMatch + "%");
    }

    /**
     * Generates a specification to check if the passed string is like the parameter.
     * <br>
     * The method passes the parameter to the like method as-is.
     *
     * @param parameterName The name of the parameter in the object.
     * @param <T>           The class of the object to generate the spec for.
     * @return The {@link Specification} object that belongs to the custom query.
     */
    public static <T> Specification<T> matchStringRawNotLike(String valueToMatch, String parameterName) {
        return (Root<T> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) ->
                matchStringNotLikeCriteria(splitRoot(root, parameterName), criteriaBuilder,
                        valueToMatch);
    }

    /**
     * Generates a specification to check if the passed string is not like the parameter.
     * <br>
     * The method surrounds the parameter with '%' characters.
     *
     * @param parameterName The name of the parameter in the object.
     * @param <T>           The class of the object to generate the spec for.
     * @return The {@link Specification} object that belongs to the custom query.
     */
    public static <T> Specification<T> matchStringNotLike(String valueToMatch, String parameterName) {
        return (Root<T> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) ->
                matchStringNotLikeCriteria(splitRoot(root, parameterName), criteriaBuilder,
                        "%" + valueToMatch + "%");
    }

    /**
     * Generates a specification to check if the passed string is like the parameter casting both sides to lowercase
     * for a case-insensitive search. You may want to create a lowercase index of the column in the database to
     * improve performance.
     * <br>
     * The method passes the value in lowercase without surrounding it with '%'.
     *
     * @param parameterName The name of the parameter in the object.
     * @param <T>           The class of the object to generate the spec for.
     * @return The {@link Specification} object that belongs to the custom query.
     */
    public static <T> Specification<T> matchStringRawLikeCaseInsensitive(String valueToMatch, String parameterName) {
        return (Root<T> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) ->
                matchStringCaseInsensitiveLikeCriteria(splitRoot(root, parameterName), criteriaBuilder,
                        valueToMatch);
    }

    /**
     * Generates a specification to check if the passed string is like the parameter casting both sides to lowercase
     * for a case-insensitive search. You may want to create a lowercase index of the column in the database to
     * improve performance.
     * <br>
     * The method surrounds the parameter with '%' characters.
     *
     * @param parameterName The name of the parameter in the object.
     * @param <T>           The class of the object to generate the spec for.
     * @return The {@link Specification} object that belongs to the custom query.
     */
    public static <T> Specification<T> matchStringLikeCaseInsensitive(String valueToMatch, String parameterName) {
        return (Root<T> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) ->
                matchStringCaseInsensitiveLikeCriteria(splitRoot(root, parameterName), criteriaBuilder,
                        "%" + valueToMatch + "%");
    }

    /**
     * Generates a specification to check if the passed string is like the parameter casting both sides to lowercase
     * for a case-insensitive search. You may want to create a lowercase index of the column in the database to
     * improve performance.
     * <br>
     * The method passes the value in lowercase without surrounding it with '%'.
     *
     * @param parameterName The name of the parameter in the object.
     * @param <T>           The class of the object to generate the spec for.
     * @return The {@link Specification} object that belongs to the custom query.
     */
    public static <T> Specification<T> matchStringRawNotLikeCaseInsensitive(String valueToMatch, String parameterName) {
        return (Root<T> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) ->
                matchStringCaseInsensitiveNotLikeCriteria(splitRoot(root, parameterName), criteriaBuilder,
                        valueToMatch);
    }

    /**
     * Generates a specification to check if the passed string is like the parameter casting both sides to lowercase
     * for a case-insensitive search. You may want to create a lowercase index of the column in the database to
     * improve performance.
     * <br>
     * The method surrounds the parameter with '%' characters.
     *
     * @param parameterName The name of the parameter in the object.
     * @param <T>           The class of the object to generate the spec for.
     * @return The {@link Specification} object that belongs to the custom query.
     */
    public static <T> Specification<T> matchStringNotLikeCaseInsensitive(String valueToMatch, String parameterName) {
        return (Root<T> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) ->
                matchStringCaseInsensitiveNotLikeCriteria(splitRoot(root, parameterName), criteriaBuilder,
                        "%" + valueToMatch + "%");
    }

    /**
     * Generates a specification to match list columns that are empty.
     *
     * @param parameterName The name of the parameter in the object.
     * @param <T>           The class of the object to generate the spec for.
     * @return The {@link Specification} object that belongs to the custom query.
     */
    public static <T> Specification<T> matchEmpty(String parameterName) {
        return (Root<T> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) ->
                criteriaBuilder.isEmpty(splitRoot(root, parameterName).as(List.class));
    }

    /**
     * Generates a specification to match list columns that are not empty.
     *
     * @param parameterName The name of the parameter in the object.
     * @param <T>           The class of the object to generate the spec for.
     * @return The {@link Specification} object that belongs to the custom query.
     */
    public static <T> Specification<T> matchNotEmpty(String parameterName) {
        return (Root<T> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) ->
                criteriaBuilder.isNotEmpty(splitRoot(root, parameterName).as(List.class));
    }

    /**
     * Generates a specification to match value from a list to be one from the passed collection.
     * <br>
     * If the collection is empty, the criteria will be skipped.
     *
     * @param valuesToMatch The list of values to match.
     * @param parameterName The name of the parameter in the object.
     * @return The {@link Specification} object that belongs to the custom query.
     * @param <T> The class of the object to generate the spec for.
     */
    public static <T> Specification<T> matchValueIn(Collection<?> valuesToMatch, String parameterName) {
        return (Root<T> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) ->
                matchValueInCriteria(splitRoot(root, parameterName), criteriaBuilder, valuesToMatch);
    }

    /**
     * Generates a specification to check if the passed subquery result exists.
     *
     * @param spec The subquery to check for existence.
     * @return The {@link Specification} object that belongs to the custom query.
     * @param <T> The class of the object to generate the spec for.
     */
    public static <T, E> Specification<T> matchExists(@NotNull Specification<E> spec, @NotNull Class<E> subQueryClass) {
        return (Root<T> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) -> {
            assert query != null;
            Subquery<E> subquery = query.subquery(subQueryClass);
            Root<E> subqueryRoot = subquery.from(subQueryClass);
            subquery.select(subqueryRoot).where(spec.toPredicate(subqueryRoot, query, criteriaBuilder));
            return criteriaBuilder.exists(subquery);
        };
    }

    /**
     * Generates a specification to check if the passed subquery result exists.
     *
     * @param spec The subquery to check for existence.
     * @return The {@link Specification} object that belongs to the custom query.
     * @param <T> The class of the object to generate the spec for.
     */
    public static <T, E> Specification<T> matchNotExists(@NotNull Specification<E> spec, @NotNull Class<E> subQueryClass) {
        return (Root<T> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) -> {
            assert query != null;
            Subquery<E> subquery = query.subquery(subQueryClass);
            Root<E> subqueryRoot = subquery.from(subQueryClass);
            subquery.select(subqueryRoot).where(spec.toPredicate(subqueryRoot, query, criteriaBuilder));
            return criteriaBuilder.not(criteriaBuilder.exists(subquery));
        };
    }

    /**
     * The criteria compare a date as bigger than the compared one.
     *
     * @param rootPath        The {@link Expression} field of the object to search for.
     * @param criteriaBuilder The criteria builder to generate the custom query.
     * @param dateToCompare   The value of the date being compared to the one in the database.
     * @return The Predicate generated.
     */
    private static Predicate matchGreaterThanDateCriteria(Path<?> rootPath, CriteriaBuilder criteriaBuilder,
                                                          Date dateToCompare) {
        if (dateToCompare != null) {
            return criteriaBuilder.greaterThan(rootPath.as(Date.class), dateToCompare);
        } else {
            return criteriaBuilder.isTrue(criteriaBuilder.literal(Boolean.TRUE));
        }
    }

    /**
     * The criteria compare a date as bigger than the compared one.
     *
     * @param rootPath        The {@link Expression} field of the object to search for.
     * @param criteriaBuilder The criteria builder to generate the custom query.
     * @param dateToCompare   The value of the date being compared to the one in the database.
     * @return The Predicate generated.
     */
    private static Predicate matchGreaterThanDateCriteria(Path<?> rootPath, CriteriaBuilder criteriaBuilder,
                                                          LocalDate dateToCompare) {
        if (dateToCompare != null) {
            return criteriaBuilder.greaterThan(rootPath.as(LocalDate.class), dateToCompare);
        } else {
            return criteriaBuilder.isTrue(criteriaBuilder.literal(Boolean.TRUE));
        }
    }

    /**
     * The criteria compare a date as bigger than the compared one.
     *
     * @param rootPath        The {@link Expression} field of the object to search for.
     * @param criteriaBuilder The criteria builder to generate the custom query.
     * @param dateToCompare   The value of the date being compared to the one in the database.
     * @return The Predicate generated.
     */
    private static Predicate matchGreaterThanDateCriteria(Path<?> rootPath, CriteriaBuilder criteriaBuilder,
                                                          LocalTime dateToCompare) {
        if (dateToCompare != null) {
            return criteriaBuilder.greaterThan(rootPath.as(LocalTime.class), dateToCompare);
        } else {
            return criteriaBuilder.isTrue(criteriaBuilder.literal(Boolean.TRUE));
        }
    }

    /**
     * The criteria compare a date as bigger than the compared one.
     *
     * @param rootPath        The {@link Expression} field of the object to search for.
     * @param criteriaBuilder The criteria builder to generate the custom query.
     * @param dateToCompare   The value of the date being compared to the one in the database.
     * @return The Predicate generated.
     */
    private static Predicate matchGreaterThanDateCriteria(Path<?> rootPath, CriteriaBuilder criteriaBuilder,
                                                          LocalDateTime dateToCompare) {
        if (dateToCompare != null) {
            return criteriaBuilder.greaterThan(rootPath.as(LocalDateTime.class), dateToCompare);
        } else {
            return criteriaBuilder.isTrue(criteriaBuilder.literal(Boolean.TRUE));
        }
    }

    /**
     * The criteria compare a date as bigger than the compared one.
     *
     * @param rootPath        The {@link Expression} field of the object to search for.
     * @param criteriaBuilder The criteria builder to generate the custom query.
     * @param dateToCompare   The value of the date being compared to the one in the database.
     * @return The Predicate generated.
     */
    private static Predicate matchGreaterThanDateCriteria(Path<?> rootPath, CriteriaBuilder criteriaBuilder,
                                                          OffsetTime dateToCompare) {
        if (dateToCompare != null) {
            return criteriaBuilder.greaterThan(rootPath.as(OffsetTime.class), dateToCompare);
        } else {
            return criteriaBuilder.isTrue(criteriaBuilder.literal(Boolean.TRUE));
        }
    }

    /**
     * The criteria compare a date as bigger than the compared one.
     *
     * @param rootPath        The {@link Expression} field of the object to search for.
     * @param criteriaBuilder The criteria builder to generate the custom query.
     * @param dateToCompare   The value of the date being compared to the one in the database.
     * @return The Predicate generated.
     */
    private static Predicate matchGreaterThanDateCriteria(Path<?> rootPath, CriteriaBuilder criteriaBuilder,
                                                          OffsetDateTime dateToCompare) {
        if (dateToCompare != null) {
            return criteriaBuilder.greaterThan(rootPath.as(OffsetDateTime.class), dateToCompare);
        } else {
            return criteriaBuilder.isTrue(criteriaBuilder.literal(Boolean.TRUE));
        }
    }

    /**
     * The criteria to compare a number being bigger than the compared one.
     *
     * @param rootPath        The {@link Expression} field of the object to search for.
     * @param criteriaBuilder The criteria builder to generate the custom query.
     * @param numberToCompare The value of the number being compared to the one in the database.
     * @return The Predicate generated.
     */
    private static Predicate matchGreaterThanNumberCriteria(Path<?> rootPath, CriteriaBuilder criteriaBuilder,
                                                            Number numberToCompare) {
        if (numberToCompare != null) {
            if (numberToCompare instanceof Integer num) {
                return criteriaBuilder.greaterThan(rootPath.as(Integer.class), num);
            } else if (numberToCompare instanceof Long num) {
                return criteriaBuilder.greaterThan(rootPath.as(Long.class), num);
            } else if (numberToCompare instanceof Short num) {
                return criteriaBuilder.greaterThan(rootPath.as(Short.class), num);
            } else if (numberToCompare instanceof Double num) {
                return criteriaBuilder.greaterThan(rootPath.as(Double.class), num);
            } else if (numberToCompare instanceof Float num) {
                return criteriaBuilder.greaterThan(rootPath.as(Float.class), num);
            } else if (numberToCompare instanceof BigDecimal num) {
                return criteriaBuilder.greaterThan(rootPath.as(BigDecimal.class), num);
            } else {
                return criteriaBuilder.greaterThan(rootPath.as(Integer.class), numberToCompare.intValue());
            }
        } else {
            return criteriaBuilder.isTrue(criteriaBuilder.literal(Boolean.TRUE));
        }
    }

    /**
     * The criteria compare a date as bigger than the compared one.
     *
     * @param rootPath        The {@link Expression} field of the object to search for.
     * @param criteriaBuilder The criteria builder to generate the custom query.
     * @param dateToCompare   The value of the date being compared to the one in the database.
     * @return The Predicate generated.
     */
    private static Predicate matchGreaterOrEqualsThanDateCriteria(Path<?> rootPath, CriteriaBuilder criteriaBuilder,
                                                                  Date dateToCompare) {
        if (dateToCompare != null) {
            return criteriaBuilder.greaterThanOrEqualTo(rootPath.as(Date.class), dateToCompare);
        } else {
            return criteriaBuilder.isTrue(criteriaBuilder.literal(Boolean.TRUE));
        }
    }

    /**
     * The criteria compare a date as bigger than the compared one.
     *
     * @param rootPath        The {@link Expression} field of the object to search for.
     * @param criteriaBuilder The criteria builder to generate the custom query.
     * @param dateToCompare   The value of the date being compared to the one in the database.
     * @return The Predicate generated.
     */
    private static Predicate matchGreaterOrEqualsThanDateCriteria(Path<?> rootPath, CriteriaBuilder criteriaBuilder,
                                                                  LocalDate dateToCompare) {
        if (dateToCompare != null) {
            return criteriaBuilder.greaterThanOrEqualTo(rootPath.as(LocalDate.class), dateToCompare);
        } else {
            return criteriaBuilder.isTrue(criteriaBuilder.literal(Boolean.TRUE));
        }
    }

    /**
     * The criteria compare a date as bigger than the compared one.
     *
     * @param rootPath        The {@link Expression} field of the object to search for.
     * @param criteriaBuilder The criteria builder to generate the custom query.
     * @param dateToCompare   The value of the date being compared to the one in the database.
     * @return The Predicate generated.
     */
    private static Predicate matchGreaterOrEqualsThanDateCriteria(Path<?> rootPath, CriteriaBuilder criteriaBuilder,
                                                                  LocalTime dateToCompare) {
        if (dateToCompare != null) {
            return criteriaBuilder.greaterThanOrEqualTo(rootPath.as(LocalTime.class), dateToCompare);
        } else {
            return criteriaBuilder.isTrue(criteriaBuilder.literal(Boolean.TRUE));
        }
    }

    /**
     * The criteria compare a date as bigger than the compared one.
     *
     * @param rootPath        The {@link Expression} field of the object to search for.
     * @param criteriaBuilder The criteria builder to generate the custom query.
     * @param dateToCompare   The value of the date being compared to the one in the database.
     * @return The Predicate generated.
     */
    private static Predicate matchGreaterOrEqualsThanDateCriteria(Path<?> rootPath, CriteriaBuilder criteriaBuilder,
                                                                  LocalDateTime dateToCompare) {
        if (dateToCompare != null) {
            return criteriaBuilder.greaterThanOrEqualTo(rootPath.as(LocalDateTime.class), dateToCompare);
        } else {
            return criteriaBuilder.isTrue(criteriaBuilder.literal(Boolean.TRUE));
        }
    }

    /**
     * The criteria compare a date as bigger than the compared one.
     *
     * @param rootPath        The {@link Expression} field of the object to search for.
     * @param criteriaBuilder The criteria builder to generate the custom query.
     * @param dateToCompare   The value of the date being compared to the one in the database.
     * @return The Predicate generated.
     */
    private static Predicate matchGreaterOrEqualsThanDateCriteria(Path<?> rootPath, CriteriaBuilder criteriaBuilder,
                                                                  OffsetDateTime dateToCompare) {
        if (dateToCompare != null) {
            return criteriaBuilder.greaterThanOrEqualTo(rootPath.as(OffsetDateTime.class), dateToCompare);
        } else {
            return criteriaBuilder.isTrue(criteriaBuilder.literal(Boolean.TRUE));
        }
    }

    /**
     * The criteria compare a date as bigger than the compared one.
     *
     * @param rootPath        The {@link Expression} field of the object to search for.
     * @param criteriaBuilder The criteria builder to generate the custom query.
     * @param dateToCompare   The value of the date being compared to the one in the database.
     * @return The Predicate generated.
     */
    private static Predicate matchGreaterOrEqualsThanDateCriteria(Path<?> rootPath, CriteriaBuilder criteriaBuilder,
                                                                  OffsetTime dateToCompare) {
        if (dateToCompare != null) {
            return criteriaBuilder.greaterThanOrEqualTo(rootPath.as(OffsetTime.class), dateToCompare);
        } else {
            return criteriaBuilder.isTrue(criteriaBuilder.literal(Boolean.TRUE));
        }
    }

    /**
     * The criteria to compare a number being bigger or equal than the compared one.
     *
     * @param rootPath        The {@link Expression} field of the object to search for.
     * @param criteriaBuilder The criteria builder to generate the custom query.
     * @param numberToCompare The value of the number being compared to the one in the database.
     * @return The Predicate generated.
     */
    private static Predicate matchGreaterOrEqualsThanNumberCriteria(Path<?> rootPath, CriteriaBuilder criteriaBuilder,
                                                                    Number numberToCompare) {
        if (numberToCompare != null) {
            if (numberToCompare instanceof Integer num) {
                return criteriaBuilder.greaterThanOrEqualTo(rootPath.as(Integer.class), num);
            } else if (numberToCompare instanceof Long num) {
                return criteriaBuilder.greaterThanOrEqualTo(rootPath.as(Long.class), num);
            } else if (numberToCompare instanceof Short num) {
                return criteriaBuilder.greaterThanOrEqualTo(rootPath.as(Short.class), num);
            } else if (numberToCompare instanceof Double num) {
                return criteriaBuilder.greaterThanOrEqualTo(rootPath.as(Double.class), num);
            } else if (numberToCompare instanceof Float num) {
                return criteriaBuilder.greaterThanOrEqualTo(rootPath.as(Float.class), num);
            } else if (numberToCompare instanceof BigDecimal num) {
                return criteriaBuilder.greaterThanOrEqualTo(rootPath.as(BigDecimal.class), num);
            } else {
                return criteriaBuilder.greaterThanOrEqualTo(rootPath.as(Integer.class), numberToCompare.intValue());
            }
        } else {
            return criteriaBuilder.isTrue(criteriaBuilder.literal(Boolean.TRUE));
        }
    }

    /**
     * The criteria matcher to compare the column as being lesser than the compared value.
     *
     * @param rootPath        The {@link Expression} field of the object to search for.
     * @param criteriaBuilder The criteria builder to generate the custom query.
     * @param dateToCompare   The value of the date being compared to the one in the database.
     * @return The Predicate generated.
     */
    private static Predicate matchLesserThanDateCriteria(Path<?> rootPath, CriteriaBuilder criteriaBuilder,
                                                         Date dateToCompare) {
        if (dateToCompare != null) {
            return criteriaBuilder.lessThan(rootPath.as(Date.class), dateToCompare);
        } else {
            return criteriaBuilder.isTrue(criteriaBuilder.literal(Boolean.TRUE));
        }
    }

    /**
     * The criteria matcher to compare the column as being lesser than the compared value.
     *
     * @param rootPath        The {@link Expression} field of the object to search for.
     * @param criteriaBuilder The criteria builder to generate the custom query.
     * @param dateToCompare   The value of the date being compared to the one in the database.
     * @return The Predicate generated.
     */
    private static Predicate matchLesserThanDateCriteria(Path<?> rootPath, CriteriaBuilder criteriaBuilder,
                                                         LocalDate dateToCompare) {
        if (dateToCompare != null) {
            return criteriaBuilder.lessThan(rootPath.as(LocalDate.class), dateToCompare);
        } else {
            return criteriaBuilder.isTrue(criteriaBuilder.literal(Boolean.TRUE));
        }
    }

    /**
     * The criteria matcher to compare the column as being lesser than the compared value.
     *
     * @param rootPath        The {@link Expression} field of the object to search for.
     * @param criteriaBuilder The criteria builder to generate the custom query.
     * @param dateToCompare   The value of the date being compared to the one in the database.
     * @return The Predicate generated.
     */
    private static Predicate matchLesserThanDateCriteria(Path<?> rootPath, CriteriaBuilder criteriaBuilder,
                                                         LocalTime dateToCompare) {
        if (dateToCompare != null) {
            return criteriaBuilder.lessThan(rootPath.as(LocalTime.class), dateToCompare);
        } else {
            return criteriaBuilder.isTrue(criteriaBuilder.literal(Boolean.TRUE));
        }
    }

    /**
     * The criteria matcher to compare the column as being lesser than the compared value.
     *
     * @param rootPath        The {@link Expression} field of the object to search for.
     * @param criteriaBuilder The criteria builder to generate the custom query.
     * @param dateToCompare   The value of the date being compared to the one in the database.
     * @return The Predicate generated.
     */
    private static Predicate matchLesserThanDateCriteria(Path<?> rootPath, CriteriaBuilder criteriaBuilder,
                                                         LocalDateTime dateToCompare) {
        if (dateToCompare != null) {
            return criteriaBuilder.lessThan(rootPath.as(LocalDateTime.class), dateToCompare);
        } else {
            return criteriaBuilder.isTrue(criteriaBuilder.literal(Boolean.TRUE));
        }
    }

    /**
     * The criteria matcher to compare the column as being lesser than the compared value.
     *
     * @param rootPath        The {@link Expression} field of the object to search for.
     * @param criteriaBuilder The criteria builder to generate the custom query.
     * @param dateToCompare   The value of the date being compared to the one in the database.
     * @return The Predicate generated.
     */
    private static Predicate matchLesserThanDateCriteria(Path<?> rootPath, CriteriaBuilder criteriaBuilder,
                                                         OffsetTime dateToCompare) {
        if (dateToCompare != null) {
            return criteriaBuilder.lessThan(rootPath.as(OffsetTime.class), dateToCompare);
        } else {
            return criteriaBuilder.isTrue(criteriaBuilder.literal(Boolean.TRUE));
        }
    }

    /**
     * The criteria matcher to compare the column as being lesser than the compared value.
     *
     * @param rootPath        The {@link Expression} field of the object to search for.
     * @param criteriaBuilder The criteria builder to generate the custom query.
     * @param dateToCompare   The value of the date being compared to the one in the database.
     * @return The Predicate generated.
     */
    private static Predicate matchLesserThanDateCriteria(Path<?> rootPath, CriteriaBuilder criteriaBuilder,
                                                         OffsetDateTime dateToCompare) {
        if (dateToCompare != null) {
            return criteriaBuilder.lessThan(rootPath.as(OffsetDateTime.class), dateToCompare);
        } else {
            return criteriaBuilder.isTrue(criteriaBuilder.literal(Boolean.TRUE));
        }
    }

    /**
     * The criteria matcher to compare the column as being lesser than the compared value.
     *
     * @param rootPath        The {@link Expression} field of the object to search for.
     * @param criteriaBuilder The criteria builder to generate the custom query.
     * @param numberToCompare The value of the number being compared to the one in the database.
     * @return The Predicate generated.
     */
    private static Predicate matchLesserThanNumberCriteria(Path<?> rootPath, CriteriaBuilder criteriaBuilder,
                                                           Number numberToCompare) {
        if (numberToCompare != null) {
            if (numberToCompare instanceof Integer num) {
                return criteriaBuilder.lessThan(rootPath.as(Integer.class), num);
            } else if (numberToCompare instanceof Long num) {
                return criteriaBuilder.lessThan(rootPath.as(Long.class), num);
            } else if (numberToCompare instanceof Short num) {
                return criteriaBuilder.lessThan(rootPath.as(Short.class), num);
            } else if (numberToCompare instanceof Double num) {
                return criteriaBuilder.lessThan(rootPath.as(Double.class), num);
            } else if (numberToCompare instanceof Float num) {
                return criteriaBuilder.lessThan(rootPath.as(Float.class), num);
            } else if (numberToCompare instanceof BigDecimal num) {
                return criteriaBuilder.lessThan(rootPath.as(BigDecimal.class), num);
            } else {
                return criteriaBuilder.lessThan(rootPath.as(Integer.class), numberToCompare.intValue());
            }
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
    private static Predicate matchLesserOrEqualsThanDateCriteria(Path<?> rootPath, CriteriaBuilder criteriaBuilder,
                                                                 Date dateToCompare) {
        if (dateToCompare != null) {
            return criteriaBuilder.lessThanOrEqualTo(rootPath.as(Date.class), dateToCompare);
        } else {
            return criteriaBuilder.isTrue(criteriaBuilder.literal(Boolean.TRUE));
        }
    }

    /**
     * The criteria matcher to compare the column as being lesser or equal than the compared value.
     *
     * @param rootPath        The {@link Expression} field of the object to search for.
     * @param criteriaBuilder The criteria builder to generate the custom query.
     * @param dateToCompare   The value of the date being compared to the one in the database.
     * @return The Predicate generated.
     */
    private static Predicate matchLesserOrEqualsThanDateCriteria(Path<?> rootPath, CriteriaBuilder criteriaBuilder,
                                                                 LocalDate dateToCompare) {
        if (dateToCompare != null) {
            return criteriaBuilder.lessThanOrEqualTo(rootPath.as(LocalDate.class), dateToCompare);
        } else {
            return criteriaBuilder.isTrue(criteriaBuilder.literal(Boolean.TRUE));
        }
    }

    /**
     * The criteria matcher to compare the column as being lesser or equal than the compared value.
     *
     * @param rootPath        The {@link Expression} field of the object to search for.
     * @param criteriaBuilder The criteria builder to generate the custom query.
     * @param dateToCompare   The value of the date being compared to the one in the database.
     * @return The Predicate generated.
     */
    private static Predicate matchLesserOrEqualsThanDateCriteria(Path<?> rootPath, CriteriaBuilder criteriaBuilder,
                                                                 LocalTime dateToCompare) {
        if (dateToCompare != null) {
            return criteriaBuilder.lessThanOrEqualTo(rootPath.as(LocalTime.class), dateToCompare);
        } else {
            return criteriaBuilder.isTrue(criteriaBuilder.literal(Boolean.TRUE));
        }
    }

    /**
     * The criteria matcher to compare the column as being lesser or equal than the compared value.
     *
     * @param rootPath        The {@link Expression} field of the object to search for.
     * @param criteriaBuilder The criteria builder to generate the custom query.
     * @param dateToCompare   The value of the date being compared to the one in the database.
     * @return The Predicate generated.
     */
    private static Predicate matchLesserOrEqualsThanDateCriteria(Path<?> rootPath, CriteriaBuilder criteriaBuilder,
                                                                 LocalDateTime dateToCompare) {
        if (dateToCompare != null) {
            return criteriaBuilder.lessThanOrEqualTo(rootPath.as(LocalDateTime.class), dateToCompare);
        } else {
            return criteriaBuilder.isTrue(criteriaBuilder.literal(Boolean.TRUE));
        }
    }

    /**
     * The criteria matcher to compare the column as being lesser or equal than the compared value.
     *
     * @param rootPath        The {@link Expression} field of the object to search for.
     * @param criteriaBuilder The criteria builder to generate the custom query.
     * @param dateToCompare   The value of the date being compared to the one in the database.
     * @return The Predicate generated.
     */
    private static Predicate matchLesserOrEqualsThanDateCriteria(Path<?> rootPath, CriteriaBuilder criteriaBuilder,
                                                                 OffsetTime dateToCompare) {
        if (dateToCompare != null) {
            return criteriaBuilder.lessThanOrEqualTo(rootPath.as(OffsetTime.class), dateToCompare);
        } else {
            return criteriaBuilder.isTrue(criteriaBuilder.literal(Boolean.TRUE));
        }
    }

    /**
     * The criteria matcher to compare the column as being lesser or equal than the compared value.
     *
     * @param rootPath        The {@link Expression} field of the object to search for.
     * @param criteriaBuilder The criteria builder to generate the custom query.
     * @param dateToCompare   The value of the date being compared to the one in the database.
     * @return The Predicate generated.
     */
    private static Predicate matchLesserOrEqualsThanDateCriteria(Path<?> rootPath, CriteriaBuilder criteriaBuilder,
                                                                 OffsetDateTime dateToCompare) {
        if (dateToCompare != null) {
            return criteriaBuilder.lessThanOrEqualTo(rootPath.as(OffsetDateTime.class), dateToCompare);
        } else {
            return criteriaBuilder.isTrue(criteriaBuilder.literal(Boolean.TRUE));
        }
    }

    /**
     * The criteria to compare a number being lesser or equal than the compared one.
     *
     * @param rootPath        The {@link Expression} field of the object to search for.
     * @param criteriaBuilder The criteria builder to generate the custom query.
     * @param numberToCompare The value of the number being compared to the one in the database.
     * @return The Predicate generated.
     */
    private static Predicate matchLesserOrEqualsThanNumberCriteria(Path<?> rootPath, CriteriaBuilder criteriaBuilder,
                                                                   Number numberToCompare) {
        if (numberToCompare != null) {
            if (numberToCompare instanceof Integer num) {
                return criteriaBuilder.lessThanOrEqualTo(rootPath.as(Integer.class), num);
            } else if (numberToCompare instanceof Long num) {
                return criteriaBuilder.lessThanOrEqualTo(rootPath.as(Long.class), num);
            } else if (numberToCompare instanceof Short num) {
                return criteriaBuilder.lessThanOrEqualTo(rootPath.as(Short.class), num);
            } else if (numberToCompare instanceof Double num) {
                return criteriaBuilder.lessThanOrEqualTo(rootPath.as(Double.class), num);
            } else if (numberToCompare instanceof Float num) {
                return criteriaBuilder.lessThanOrEqualTo(rootPath.as(Float.class), num);
            } else if (numberToCompare instanceof BigDecimal num) {
                return criteriaBuilder.lessThanOrEqualTo(rootPath.as(BigDecimal.class), num);
            } else {
                return criteriaBuilder.lessThanOrEqualTo(rootPath.as(Integer.class), numberToCompare.intValue());
            }
        } else {
            return criteriaBuilder.isTrue(criteriaBuilder.literal(Boolean.TRUE));
        }
    }

    /**
     * The criteria matcher to match dates between a range of two dates.
     * @param rootPath The column to match.
     * @param criteriaBuilder The criteria builder to generate the custom query.
     * @param startDate The initial date.
     * @param endDate The end date.
     * @return The Predicate generated.
     */
    private static Predicate matchDateBetweenCriteria(Path<?> rootPath, CriteriaBuilder criteriaBuilder, Date startDate,
                                                      Date endDate) {
        if (startDate != null && endDate != null) {
            return criteriaBuilder.between(rootPath.as(Date.class), startDate, endDate);
        } else {
            return criteriaBuilder.isTrue(criteriaBuilder.literal(Boolean.TRUE));
        }
    }

    /**
     * The criteria matcher to match dates between a range of two dates.
     * @param rootPath The column to match.
     * @param criteriaBuilder The criteria builder to generate the custom query.
     * @param startDate The initial date.
     * @param endDate The end date.
     * @return The Predicate generated.
     */
    private static Predicate matchDateBetweenCriteria(Path<?> rootPath, CriteriaBuilder criteriaBuilder,
                                                      LocalDate startDate,
                                                      LocalDate endDate) {
        if (startDate != null && endDate != null) {
            return criteriaBuilder.between(rootPath.as(LocalDate.class), startDate, endDate);
        } else {
            return criteriaBuilder.isTrue(criteriaBuilder.literal(Boolean.TRUE));
        }
    }

    /**
     * The criteria matcher to match dates between a range of two dates.
     * @param rootPath The column to match.
     * @param criteriaBuilder The criteria builder to generate the custom query.
     * @param startDate The initial date.
     * @param endDate The end date.
     * @return The Predicate generated.
     */
    private static Predicate matchDateBetweenCriteria(Path<?> rootPath, CriteriaBuilder criteriaBuilder,
                                                      LocalTime startDate,
                                                      LocalTime endDate) {
        if (startDate != null && endDate != null) {
            return criteriaBuilder.between(rootPath.as(LocalTime.class), startDate, endDate);
        } else {
            return criteriaBuilder.isTrue(criteriaBuilder.literal(Boolean.TRUE));
        }
    }

    /**
     * The criteria matcher to match dates between a range of two dates.
     * @param rootPath The column to match.
     * @param criteriaBuilder The criteria builder to generate the custom query.
     * @param startDate The initial date.
     * @param endDate The end date.
     * @return The Predicate generated.
     */
    private static Predicate matchDateBetweenCriteria(Path<?> rootPath, CriteriaBuilder criteriaBuilder,
                                                      LocalDateTime startDate,
                                                      LocalDateTime endDate) {
        if (startDate != null && endDate != null) {
            return criteriaBuilder.between(rootPath.as(LocalDateTime.class), startDate, endDate);
        } else {
            return criteriaBuilder.isTrue(criteriaBuilder.literal(Boolean.TRUE));
        }
    }

    /**
     * The criteria matcher to match dates between a range of two dates.
     * @param rootPath The column to match.
     * @param criteriaBuilder The criteria builder to generate the custom query.
     * @param startDate The initial date.
     * @param endDate The end date.
     * @return The Predicate generated.
     */
    private static Predicate matchDateBetweenCriteria(Path<?> rootPath, CriteriaBuilder criteriaBuilder,
                                                      OffsetTime startDate,
                                                      OffsetTime endDate) {
        if (startDate != null && endDate != null) {
            return criteriaBuilder.between(rootPath.as(OffsetTime.class), startDate, endDate);
        } else {
            return criteriaBuilder.isTrue(criteriaBuilder.literal(Boolean.TRUE));
        }
    }

    /**
     * The criteria matcher to match dates between a range of two dates.
     * @param rootPath The column to match.
     * @param criteriaBuilder The criteria builder to generate the custom query.
     * @param startDate The initial date.
     * @param endDate The end date.
     * @return The Predicate generated.
     */
    private static Predicate matchDateBetweenCriteria(Path<?> rootPath, CriteriaBuilder criteriaBuilder,
                                                      OffsetDateTime startDate,
                                                      OffsetDateTime endDate) {
        if (startDate != null && endDate != null) {
            return criteriaBuilder.between(rootPath.as(OffsetDateTime.class), startDate, endDate);
        } else {
            return criteriaBuilder.isTrue(criteriaBuilder.literal(Boolean.TRUE));
        }
    }

    /**
     * The criteria matcher to match a number between a range of two numbers.
     * @param rootPath The column to match.
     * @param criteriaBuilder The criteria builder to generate the custom query.
     * @param lowestNumber The lowest number to compare.
     * @param highestNumber The highest number to compare.
     * @return The Predicate generated.
     */
    private static Predicate matchNumberBetweenCriteria(Path<?> rootPath, CriteriaBuilder criteriaBuilder,
                                                      Number lowestNumber,
                                                        Number highestNumber) {
        if (lowestNumber != null && highestNumber != null) {
            if (lowestNumber instanceof Integer num) {
                return criteriaBuilder.between(rootPath.as(Integer.class), num, highestNumber.intValue());
            } else if (lowestNumber instanceof Long num) {
                return criteriaBuilder.between(rootPath.as(Long.class), num, highestNumber.longValue());
            } else if (lowestNumber instanceof Short num) {
                return criteriaBuilder.between(rootPath.as(Short.class), num, highestNumber.shortValue());
            } else if (lowestNumber instanceof Double num) {
                return criteriaBuilder.between(rootPath.as(Double.class), num, highestNumber.doubleValue());
            } else if (lowestNumber instanceof Float num) {
                return criteriaBuilder.between(rootPath.as(Float.class), num, highestNumber.floatValue());
            } else if (lowestNumber instanceof BigDecimal num) {
                return criteriaBuilder.between(rootPath.as(BigDecimal.class), num, (BigDecimal) highestNumber);
            } else {
                return criteriaBuilder.between(rootPath.as(Integer.class), lowestNumber.intValue(),
                        highestNumber.intValue());
            }
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
    private static Predicate matchNullableValCriteria(Path<?> rootPath, CriteriaBuilder criteriaBuilder,
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
    private static Predicate matchExactValCriteria(Path<?> rootPath, CriteriaBuilder criteriaBuilder,
                                                   Object paramToSearch) {
        if (paramToSearch != null) {
            return criteriaBuilder.equal(rootPath, paramToSearch);
        } else {
            return criteriaBuilder.isNull(rootPath);
        }
    }

    /**
     * The criteria matcher to search for a boolean value in the database, matching true values, or null/false if it's
     * not true.
     *
     * @param rootPath        The {@link Expression} field of the object to search for.
     * @param criteriaBuilder The criteria builder to generate the custom query.
     * @param paramToSearch   The value of the parameter to search for.
     * @return The Predicate generated.
     */
    private static Predicate matchBooleanValCriteria(Path<?> rootPath, CriteriaBuilder criteriaBuilder,
                                                     Boolean paramToSearch) {
        if (Boolean.TRUE.equals(paramToSearch)) {
            return criteriaBuilder.isTrue(rootPath.as(Boolean.class));
        } else {
            return criteriaBuilder.or(criteriaBuilder.isNull(rootPath),
                    criteriaBuilder.isFalse(rootPath.as(Boolean.class)));
        }
    }

    /**
     * The criteria matcher to search using 'LIKE' for a string in the database.
     *
     * @param rootPath        The {@link Expression} field of the object to search for.
     * @param criteriaBuilder The criteria builder to generate the custom query.
     * @param paramToSearch   The value of the parameter to search for.
     * @return The Predicate generated.
     */
    private static Predicate matchStringLikeCriteria(Path<?> rootPath, CriteriaBuilder criteriaBuilder,
                                                     String paramToSearch) {
        if (paramToSearch != null) {
            return criteriaBuilder.like(rootPath.as(String.class), paramToSearch);
        } else {
            return criteriaBuilder.isTrue(criteriaBuilder.literal(Boolean.TRUE));
        }
    }

    /**
     * The criteria matcher to search using 'NOT LIKE' for a string in the database.
     *
     * @param rootPath        The {@link Expression} field of the object to search for.
     * @param criteriaBuilder The criteria builder to generate the custom query.
     * @param paramToSearch   The value of the parameter to search for.
     * @return The Predicate generated.
     */
    private static Predicate matchStringNotLikeCriteria(Path<?> rootPath, CriteriaBuilder criteriaBuilder,
                                                     String paramToSearch) {
        if (paramToSearch != null) {
            return criteriaBuilder.notLike(rootPath.as(String.class), paramToSearch);
        } else {
            return criteriaBuilder.isTrue(criteriaBuilder.literal(Boolean.TRUE));
        }
    }

    /**
     * The criteria matcher to search using 'LIKE' for a string in the database.
     *
     * @param rootPath        The {@link Expression} field of the object to search for.
     * @param criteriaBuilder The criteria builder to generate the custom query.
     * @param paramToSearch   The value of the parameter to search for.
     * @return The Predicate generated.
     */
    private static Predicate matchStringCaseInsensitiveLikeCriteria(Path<?> rootPath, CriteriaBuilder criteriaBuilder,
                                                                    String paramToSearch) {
        if (paramToSearch != null) {
            return criteriaBuilder.like(criteriaBuilder.lower(rootPath.as(String.class)),
                    paramToSearch.toLowerCase(Locale.getDefault()));
        } else {
            return criteriaBuilder.isTrue(criteriaBuilder.literal(Boolean.TRUE));
        }
    }

    /**
     * The criteria matcher to search using 'NOT LIKE' for a string in the database.
     *
     * @param rootPath        The {@link Expression} field of the object to search for.
     * @param criteriaBuilder The criteria builder to generate the custom query.
     * @param paramToSearch   The value of the parameter to search for.
     * @return The Predicate generated.
     */
    private static Predicate matchStringCaseInsensitiveNotLikeCriteria(Path<?> rootPath, CriteriaBuilder criteriaBuilder,
                                                                    String paramToSearch) {
        if (paramToSearch != null) {
            return criteriaBuilder.notLike(criteriaBuilder.lower(rootPath.as(String.class)),
                    "%" + paramToSearch.toLowerCase(Locale.getDefault()) + "%");
        } else {
            return criteriaBuilder.isTrue(criteriaBuilder.literal(Boolean.TRUE));
        }
    }

    /**
     * Match a value in a collection.
     * @param rootPath The {@link Expression} field of the object to search for.
     * @param criteriaBuilder The criteria builder to generate the custom query.
     * @param collectionToSearchIn The list of values of the parameter to search for.
     * @return The Predicate generated.
     */
    private static Predicate matchValueInCriteria(Path<?> rootPath, CriteriaBuilder criteriaBuilder,
                                                      Collection<?> collectionToSearchIn) {
        if (collectionToSearchIn != null && !collectionToSearchIn.isEmpty()) {
            return rootPath.in(collectionToSearchIn);
        } else {
            return criteriaBuilder.isTrue(criteriaBuilder.literal(Boolean.TRUE));
        }
    }

    /**
     * Divide root if embedded
     *
     * @param root Root to be mapped.
     * @param s    String to be divided.
     * @return The {@link Path} of the string.
     */
    private static <T> Path<T> splitRoot(Root<T> root, String s) {
        Path<T> solution;
        if (s.contains(".")) {
            String[] split = DOT_PATTERN.split(s);
            solution = root.get(split[split.length - 1]);
        } else {
            solution = root.get(s);
        }
        return solution;
    }
}

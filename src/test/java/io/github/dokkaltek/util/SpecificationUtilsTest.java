package io.github.dokkaltek.util;

import io.github.dokkaltek.TestIntegrationRunner;
import io.github.dokkaltek.model.TestEntity;
import io.github.dokkaltek.repository.TestRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZoneOffset;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for {@link SpecificationUtils} class.
 */
@SpringBootTest
@ContextConfiguration(classes = {TestIntegrationRunner.class})
@TestPropertySource("/application-test.properties")
@Transactional
class SpecificationUtilsTest {
    private static final ZoneOffset DEFAULT_ZONE_OFFSET = ZoneOffset.of("+01:00");

    @Autowired
    private TestRepository repository;

    /**
     * Tests {@link SpecificationUtils#matchNullableValue} method.
     */
    @Test
    @DisplayName("Test matching nullable values")
    void testMatchNullableValue() {
        Specification<TestEntity> spec = SpecificationUtils.matchNullableValue(34, "age");
        List<TestEntity> result = repository.findAll(spec);

        assertEquals(2, result.size());
        assertEquals("Manolo", result.get(0).getName());
        assertEquals("Angel", result.get(1).getName());

        // Test with null
        spec = SpecificationUtils.matchExactValue(null, "age");
        result = repository.findAll(spec);

        assertEquals(1, result.size());
        assertEquals("Angel", result.get(0).getName());
    }

    /**
     * Tests {@link SpecificationUtils#matchExactValue} method.
     */
    @Test
    @DisplayName("Test matching exact values")
    void testMatchExactValue() {
        Specification<TestEntity> spec = SpecificationUtils.matchExactValue(34, "age");
        List<TestEntity> result = repository.findAll(spec);

        assertEquals(1, result.size());
        assertEquals("Manolo", result.get(0).getName());

        // Test with null
        spec = SpecificationUtils.matchExactValue(null, "age");
        result = repository.findAll(spec);

        assertEquals(1, result.size());
        assertEquals("Angel", result.get(0).getName());
    }

    /**
     * Tests {@link SpecificationUtils#matchGreaterDateThan(Date, String)} method.
     */
    @Test
    @DisplayName("Test matching date greater than")
    void testMatchGreaterDateThanWithDate() {
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.set(2023, Calendar.JANUARY, 2);
        Specification<TestEntity> spec = SpecificationUtils.matchGreaterDateThan(
                calendar.getTime(), "createdAt");
        List<TestEntity> result = repository.findAll(spec);

        assertEquals(2, result.size());
        assertEquals("Manolo", result.get(0).getName());
        assertEquals("Juan", result.get(1).getName());

        // Test with null
        spec = SpecificationUtils.matchGreaterDateThan((Date) null, "createdAt");
        result = repository.findAll(spec);

        assertEquals(5, result.size());
    }

    /**
     * Tests {@link SpecificationUtils#matchGreaterDateThan(LocalDate, String)} method.
     */
    @Test
    @DisplayName("Test matching local date greater than")
    void testMatchGreaterDateThanWithLocalDate() {
        Specification<TestEntity> spec = SpecificationUtils.matchGreaterDateThan(
                LocalDate.of(2023, 1, 2), "createdAt");
        List<TestEntity> result = repository.findAll(spec);

        assertEquals(2, result.size());
        assertEquals("Manolo", result.get(0).getName());
        assertEquals("Juan", result.get(1).getName());

        // Test with null
        spec = SpecificationUtils.matchGreaterDateThan((LocalDate) null, "createdAt");
        result = repository.findAll(spec);

        assertEquals(5, result.size());
    }

    /**
     * Tests {@link SpecificationUtils#matchGreaterDateThan(LocalDateTime, String)} method.
     */
    @Test
    @DisplayName("Test matching local date time greater than")
    void testMatchGreaterDateThanWithLocalDateTime() {
        Specification<TestEntity> spec = SpecificationUtils.matchGreaterDateThan(
                LocalDateTime.of(2023, 1, 1, 19, 0), "createdAt");
        List<TestEntity> result = repository.findAll(spec);

        assertEquals(2, result.size());
        assertEquals("Manolo", result.get(0).getName());
        assertEquals("Juan", result.get(1).getName());

        // Test with null
        spec = SpecificationUtils.matchGreaterDateThan((LocalDateTime) null, "createdAt");
        result = repository.findAll(spec);

        assertEquals(5, result.size());
    }

    /**
     * Tests {@link SpecificationUtils#matchGreaterDateThan(LocalTime, String)} method.
     */
    @Test
    @DisplayName("Test matching local time greater than")
    void testMatchGreaterDateThanWithLocalTime() {
        Specification<TestEntity> spec = SpecificationUtils.matchGreaterDateThan(
                LocalTime.of(10, 0), "lastUsageTime");
        List<TestEntity> result = repository.findAll(spec);

        assertEquals(1, result.size());
        assertEquals("Juan", result.get(0).getName());

        // Test with null
        spec = SpecificationUtils.matchGreaterDateThan((LocalTime) null, "lastUsageTime");
        result = repository.findAll(spec);

        assertEquals(5, result.size());
    }

    /**
     * Tests {@link SpecificationUtils#matchGreaterDateThan(OffsetDateTime, String)} method.
     */
    @Test
    @DisplayName("Test matching offset date time greater than")
    void testMatchGreaterDateThanWithOffsetDateTime() {
        Specification<TestEntity> spec = SpecificationUtils.matchGreaterDateThan(
                OffsetDateTime.of(LocalDateTime.of(2023, 1, 1, 19, 32),
                        DEFAULT_ZONE_OFFSET), "createdAt");
        List<TestEntity> result = repository.findAll(spec);

        assertEquals(2, result.size());
        assertEquals("Manolo", result.get(0).getName());
        assertEquals("Juan", result.get(1).getName());

        // Test with null
        spec = SpecificationUtils.matchGreaterDateThan((OffsetDateTime) null, "createdAt");
        result = repository.findAll(spec);

        assertEquals(5, result.size());
    }

    /**
     * Tests {@link SpecificationUtils#matchGreaterDateThan(OffsetTime, String)} method.
     */
    @Test
    @DisplayName("Test matching offset time greater than")
    void testMatchGreaterDateThanWithOffsetTime() {
        Specification<TestEntity> spec = SpecificationUtils.matchGreaterDateThan(
                OffsetTime.of(LocalTime.of(11, 1), DEFAULT_ZONE_OFFSET),
                "lastUsageTime");
        List<TestEntity> result = repository.findAll(spec);

        assertEquals(1, result.size());
        assertEquals("Juan", result.get(0).getName());

        // Test with null
        spec = SpecificationUtils.matchGreaterDateThan((OffsetTime) null, "lastUsageTime");
        result = repository.findAll(spec);

        assertEquals(5, result.size());
    }

    /**
     * Tests {@link SpecificationUtils#matchGreaterNumberThan(Number, String)} method.
     */
    @Test
    @DisplayName("Test matching number greater than")
    void testMatchGreaterNumberThan() {
        // Test with int
        Specification<TestEntity> spec = SpecificationUtils.matchGreaterNumberThan(25, "age");
        List<TestEntity> result = repository.findAll(spec);

        assertEquals(2, result.size());
        assertEquals("Manolo", result.get(0).getName());
        assertEquals("Juan", result.get(1).getName());

        // Test with short
        spec = SpecificationUtils.matchGreaterNumberThan((short) 25, "age");
        List<TestEntity> resultToCompare = repository.findAll(spec);

        assertEquals(result, resultToCompare);

        // Test with long
        spec = SpecificationUtils.matchGreaterNumberThan(25L, "age");
        resultToCompare = repository.findAll(spec);

        assertEquals(result, resultToCompare);

        // Test with double
        spec = SpecificationUtils.matchGreaterNumberThan(25d, "age");
        resultToCompare = repository.findAll(spec);

        assertEquals(result, resultToCompare);

        // Test with float
        spec = SpecificationUtils.matchGreaterNumberThan(25f, "age");
        resultToCompare = repository.findAll(spec);

        assertEquals(result, resultToCompare);

        // Test with float
        spec = SpecificationUtils.matchGreaterNumberThan(new BigDecimal(25), "age");
        resultToCompare = repository.findAll(spec);

        assertEquals(result, resultToCompare);

        // Test with null
        spec = SpecificationUtils.matchGreaterNumberThan(null, "age");
        resultToCompare = repository.findAll(spec);

        assertEquals(5, resultToCompare.size());
    }

    /**
     * Tests {@link SpecificationUtils#matchGreaterOrEqualDateThan(Date, String)} method.
     */
    @Test
    @DisplayName("Test matching date greater or equal than")
    void testMatchGreaterOrEqualDateThanWithDate() {
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.set(2023, Calendar.JANUARY, 2);
        Specification<TestEntity> spec = SpecificationUtils.matchGreaterOrEqualDateThan(
                calendar.getTime(), "createdAt");
        List<TestEntity> result = repository.findAll(spec);

        assertEquals(2, result.size());
        assertEquals("Manolo", result.get(0).getName());
        assertEquals("Juan", result.get(1).getName());

        // Test with null
        spec = SpecificationUtils.matchGreaterOrEqualDateThan((Date) null, "createdAt");
        result = repository.findAll(spec);

        assertEquals(5, result.size());
    }

    /**
     * Tests {@link SpecificationUtils#matchGreaterOrEqualDateThan(LocalDate, String)} method.
     */
    @Test
    @DisplayName("Test matching local date greater or equal than")
    void testMatchGreaterOrEqualDateThanWithLocalDate() {
        Specification<TestEntity> spec = SpecificationUtils.matchGreaterOrEqualDateThan(
                LocalDate.of(2024, 1, 1), "createdAt");
        List<TestEntity> result = repository.findAll(spec);

        assertEquals(2, result.size());
        assertEquals("Manolo", result.get(0).getName());
        assertEquals("Juan", result.get(1).getName());

        // Test with null
        spec = SpecificationUtils.matchGreaterOrEqualDateThan((LocalDate) null, "createdAt");
        result = repository.findAll(spec);

        assertEquals(5, result.size());
    }

    /**
     * Tests {@link SpecificationUtils#matchGreaterOrEqualDateThan(LocalDateTime, String)} method.
     */
    @Test
    @DisplayName("Test matching local date time greater or equal than")
    void testMatchGreaterOrEqualDateThanWithLocalDateTime() {
        Specification<TestEntity> spec = SpecificationUtils.matchGreaterOrEqualDateThan(
                LocalDateTime.of(2024, 1, 1, 5, 11, 21), "createdAt");
        List<TestEntity> result = repository.findAll(spec);

        assertEquals(2, result.size());
        assertEquals("Manolo", result.get(0).getName());
        assertEquals("Juan", result.get(1).getName());

        // Test with null
        spec = SpecificationUtils.matchGreaterOrEqualDateThan((LocalDateTime) null, "createdAt");
        result = repository.findAll(spec);

        assertEquals(5, result.size());
    }

    /**
     * Tests {@link SpecificationUtils#matchGreaterDateThan(LocalTime, String)} method.
     */
    @Test
    @DisplayName("Test matching local time greater or equal than")
    void testMatchGreaterOrEqualDateThanWithLocalTime() {
        Specification<TestEntity> spec = SpecificationUtils.matchGreaterOrEqualDateThan(
                LocalTime.of(15, 5, 13), "lastUsageTime");
        List<TestEntity> result = repository.findAll(spec);

        assertEquals(1, result.size());
        assertEquals("Juan", result.get(0).getName());

        // Test with null
        spec = SpecificationUtils.matchGreaterOrEqualDateThan((LocalTime) null, "lastUsageTime");
        result = repository.findAll(spec);

        assertEquals(5, result.size());
    }

    /**
     * Tests {@link SpecificationUtils#matchGreaterDateThan(OffsetDateTime, String)} method.
     */
    @Test
    @DisplayName("Test matching offset date time greater or equal than")
    void testMatchGreaterOrEqualDateThanWithOffsetDateTime() {
        Specification<TestEntity> spec = SpecificationUtils.matchGreaterOrEqualDateThan(
                OffsetDateTime.of(LocalDateTime.of(2024, 1, 1, 6, 11, 21),
                        DEFAULT_ZONE_OFFSET), "createdAt");
        List<TestEntity> result = repository.findAll(spec);

        assertEquals(2, result.size());
        assertEquals("Manolo", result.get(0).getName());
        assertEquals("Juan", result.get(1).getName());

        // Test with null
        spec = SpecificationUtils.matchGreaterOrEqualDateThan((OffsetDateTime) null, "createdAt");
        result = repository.findAll(spec);

        assertEquals(5, result.size());
    }

    /**
     * Tests {@link SpecificationUtils#matchGreaterOrEqualDateThan(OffsetTime, String)} method.
     */
    @Test
    @DisplayName("Test matching offset time greater or equal than")
    void testMatchGreaterOrEqualDateThanWithOffsetTime() {
        Specification<TestEntity> spec = SpecificationUtils.matchGreaterOrEqualDateThan(
                OffsetTime.of(LocalTime.of(16, 5, 13), DEFAULT_ZONE_OFFSET),
                "lastUsageTime");
        List<TestEntity> result = repository.findAll(spec);

        assertEquals(1, result.size());
        assertEquals("Juan", result.get(0).getName());

        // Test with null
        spec = SpecificationUtils.matchGreaterOrEqualDateThan((OffsetTime) null, "lastUsageTime");
        result = repository.findAll(spec);

        assertEquals(5, result.size());
    }

    /**
     * Tests {@link SpecificationUtils#matchGreaterOrEqualNumberThan(Number, String)} method.
     */
    @Test
    @DisplayName("Test matching number greater or equal than")
    void testMatchGreaterOrEqualNumberThan() {
        // Test with int
        Specification<TestEntity> spec = SpecificationUtils.matchGreaterOrEqualNumberThan(25, "age");
        List<TestEntity> result = repository.findAll(spec);

        assertEquals(2, result.size());
        assertEquals("Manolo", result.get(0).getName());
        assertEquals("Juan", result.get(1).getName());

        // Test with short
        spec = SpecificationUtils.matchGreaterOrEqualNumberThan((short) 25, "age");
        List<TestEntity> resultToCompare = repository.findAll(spec);

        assertEquals(result, resultToCompare);

        // Test with long
        spec = SpecificationUtils.matchGreaterOrEqualNumberThan(25L, "age");
        resultToCompare = repository.findAll(spec);

        assertEquals(result, resultToCompare);

        // Test with double
        spec = SpecificationUtils.matchGreaterOrEqualNumberThan(25d, "age");
        resultToCompare = repository.findAll(spec);

        assertEquals(result, resultToCompare);

        // Test with float
        spec = SpecificationUtils.matchGreaterOrEqualNumberThan(25f, "age");
        resultToCompare = repository.findAll(spec);

        assertEquals(result, resultToCompare);

        // Test with float
        spec = SpecificationUtils.matchGreaterOrEqualNumberThan(new BigDecimal(25), "age");
        resultToCompare = repository.findAll(spec);

        assertEquals(result, resultToCompare);

        // Test with null
        spec = SpecificationUtils.matchGreaterOrEqualNumberThan(null, "age");
        resultToCompare = repository.findAll(spec);

        assertEquals(5, resultToCompare.size());
    }

    /**
     * Tests {@link SpecificationUtils#matchLesserDateThan(Date, String)} method.
     */
    @Test
    @DisplayName("Test matching date lesser than")
    void testMatchLesserDateThanWithDate() {
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.set(2023, Calendar.JANUARY, 2);
        Specification<TestEntity> spec = SpecificationUtils.matchLesserDateThan(
                calendar.getTime(), "createdAt");
        List<TestEntity> result = repository.findAll(spec);

        assertEquals(2, result.size());
        assertEquals("Eduardo", result.get(0).getName());
        assertEquals("Laura", result.get(1).getName());

        // Test with null
        spec = SpecificationUtils.matchLesserDateThan((Date) null, "createdAt");
        result = repository.findAll(spec);

        assertEquals(5, result.size());
    }

    /**
     * Tests {@link SpecificationUtils#matchLesserDateThan(LocalDate, String)} method.
     */
    @Test
    @DisplayName("Test matching local date lesser than")
    void testMatchLesserDateThanWithLocalDate() {
        Specification<TestEntity> spec = SpecificationUtils.matchLesserDateThan(
                LocalDate.of(2023, 1, 2), "createdAt");
        List<TestEntity> result = repository.findAll(spec);

        assertEquals(2, result.size());
        assertEquals("Eduardo", result.get(0).getName());
        assertEquals("Laura", result.get(1).getName());

        // Test with null
        spec = SpecificationUtils.matchLesserDateThan((LocalDate) null, "createdAt");
        result = repository.findAll(spec);

        assertEquals(5, result.size());
    }

    /**
     * Tests {@link SpecificationUtils#matchLesserDateThan(LocalDateTime, String)} method.
     */
    @Test
    @DisplayName("Test matching local date time lesser than")
    void testMatchLesserDateThanWithLocalDateTime() {
        Specification<TestEntity> spec = SpecificationUtils.matchLesserDateThan(
                LocalDateTime.of(2023, 1, 1, 19, 0), "createdAt");
        List<TestEntity> result = repository.findAll(spec);

        assertEquals(2, result.size());
        assertEquals("Eduardo", result.get(0).getName());
        assertEquals("Laura", result.get(1).getName());

        // Test with null
        spec = SpecificationUtils.matchLesserDateThan((LocalDateTime) null, "createdAt");
        result = repository.findAll(spec);

        assertEquals(5, result.size());
    }

    /**
     * Tests {@link SpecificationUtils#matchLesserDateThan(LocalTime, String)} method.
     */
    @Test
    @DisplayName("Test matching local time lesser than")
    void testMatchLesserDateThanWithLocalTime() {
        Specification<TestEntity> spec = SpecificationUtils.matchLesserDateThan(
                LocalTime.of(1, 0), "lastUsageTime");
        List<TestEntity> result = repository.findAll(spec);

        assertEquals(1, result.size());
        assertEquals("Eduardo", result.get(0).getName());

        // Test with null
        spec = SpecificationUtils.matchLesserDateThan((LocalTime) null, "lastUsageTime");
        result = repository.findAll(spec);

        assertEquals(5, result.size());
    }

    /**
     * Tests {@link SpecificationUtils#matchLesserDateThan(OffsetDateTime, String)} method.
     */
    @Test
    @DisplayName("Test matching offset date time lesser than")
    void testMatchLesserDateThanWithOffsetDateTime() {
        Specification<TestEntity> spec = SpecificationUtils.matchLesserDateThan(
                OffsetDateTime.of(LocalDateTime.of(2023, 1, 1, 19, 32),
                        DEFAULT_ZONE_OFFSET), "createdAt");
        List<TestEntity> result = repository.findAll(spec);

        assertEquals(2, result.size());
        assertEquals("Eduardo", result.get(0).getName());
        assertEquals("Laura", result.get(1).getName());

        // Test with null
        spec = SpecificationUtils.matchLesserDateThan((OffsetDateTime) null, "createdAt");
        result = repository.findAll(spec);

        assertEquals(5, result.size());
    }

    /**
     * Tests {@link SpecificationUtils#matchLesserDateThan(OffsetTime, String)} method.
     */
    @Test
    @DisplayName("Test matching offset time lesser than")
    void testMatchLesserDateThanWithOffsetTime() {
        Specification<TestEntity> spec = SpecificationUtils.matchLesserDateThan(
                OffsetTime.of(LocalTime.of(2, 11), DEFAULT_ZONE_OFFSET),
                "lastUsageTime");
        List<TestEntity> result = repository.findAll(spec);

        assertEquals(1, result.size());
        assertEquals("Eduardo", result.get(0).getName());

        // Test with null
        spec = SpecificationUtils.matchLesserDateThan((OffsetTime) null, "lastUsageTime");
        result = repository.findAll(spec);

        assertEquals(5, result.size());
    }

    /**
     * Tests {@link SpecificationUtils#matchLesserNumberThan(Number, String)} method.
     */
    @Test
    @DisplayName("Test matching number lesser than")
    void testMatchLesserNumberThan() {
        // Test with int
        Specification<TestEntity> spec = SpecificationUtils.matchLesserNumberThan(25, "age");
        List<TestEntity> result = repository.findAll(spec);

        assertEquals(2, result.size());
        assertEquals("Eduardo", result.get(0).getName());
        assertEquals("Laura", result.get(1).getName());

        // Test with short
        spec = SpecificationUtils.matchLesserNumberThan((short) 25, "age");
        List<TestEntity> resultToCompare = repository.findAll(spec);

        assertEquals(result, resultToCompare);

        // Test with long
        spec = SpecificationUtils.matchLesserNumberThan(25L, "age");
        resultToCompare = repository.findAll(spec);

        assertEquals(result, resultToCompare);

        // Test with double
        spec = SpecificationUtils.matchLesserNumberThan(25d, "age");
        resultToCompare = repository.findAll(spec);

        assertEquals(result, resultToCompare);

        // Test with float
        spec = SpecificationUtils.matchLesserNumberThan(25f, "age");
        resultToCompare = repository.findAll(spec);

        assertEquals(result, resultToCompare);

        // Test with float
        spec = SpecificationUtils.matchLesserNumberThan(new BigDecimal(25), "age");
        resultToCompare = repository.findAll(spec);

        assertEquals(result, resultToCompare);

        // Test with null
        spec = SpecificationUtils.matchLesserNumberThan(null, "age");
        resultToCompare = repository.findAll(spec);

        assertEquals(5, resultToCompare.size());
    }

    /**
     * Tests {@link SpecificationUtils#matchLesserOrEqualDateThan(Date, String)} method.
     */
    @Test
    @DisplayName("Test matching date lesser or equal than")
    void testMatchLesserOrEqualDateThanWithDate() {
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.set(2023, Calendar.JANUARY, 1);
        Specification<TestEntity> spec = SpecificationUtils.matchLesserOrEqualDateThan(
                calendar.getTime(), "createdAt");
        List<TestEntity> result = repository.findAll(spec);

        assertEquals(2, result.size());
        assertEquals("Eduardo", result.get(0).getName());
        assertEquals("Laura", result.get(1).getName());

        // Test with null
        spec = SpecificationUtils.matchLesserOrEqualDateThan((Date) null, "createdAt");
        result = repository.findAll(spec);

        assertEquals(5, result.size());
    }

    /**
     * Tests {@link SpecificationUtils#matchLesserOrEqualDateThan(LocalDate, String)} method.
     */
    @Test
    @DisplayName("Test matching local date lesser or equal than")
    void testMatchLesserOrEqualDateThanWithLocalDate() {
        Specification<TestEntity> spec = SpecificationUtils.matchLesserOrEqualDateThan(
                LocalDate.of(2023, 1, 2), "createdAt");
        List<TestEntity> result = repository.findAll(spec);

        assertEquals(2, result.size());
        assertEquals("Eduardo", result.get(0).getName());
        assertEquals("Laura", result.get(1).getName());

        // Test with null
        spec = SpecificationUtils.matchLesserOrEqualDateThan((LocalDate) null, "createdAt");
        result = repository.findAll(spec);

        assertEquals(5, result.size());
    }

    /**
     * Tests {@link SpecificationUtils#matchLesserOrEqualDateThan(LocalDateTime, String)} method.
     */
    @Test
    @DisplayName("Test matching local date time lesser or equal than")
    void testMatchLesserOrEqualDateThanWithLocalDateTime() {
        Specification<TestEntity> spec = SpecificationUtils.matchLesserOrEqualDateThan(
                LocalDateTime.of(2023, 1, 1, 18, 31, 20), "createdAt");
        List<TestEntity> result = repository.findAll(spec);

        assertEquals(2, result.size());
        assertEquals("Eduardo", result.get(0).getName());
        assertEquals("Laura", result.get(1).getName());

        // Test with null
        spec = SpecificationUtils.matchLesserOrEqualDateThan((LocalDateTime) null, "createdAt");
        result = repository.findAll(spec);

        assertEquals(5, result.size());
    }

    /**
     * Tests {@link SpecificationUtils#matchGreaterDateThan(LocalTime, String)} method.
     */
    @Test
    @DisplayName("Test matching local time lesser or equal than")
    void testMatchLesserOrEqualDateThanWithLocalTime() {
        Specification<TestEntity> spec = SpecificationUtils.matchLesserOrEqualDateThan(
                LocalTime.of(0, 10, 0), "lastUsageTime");
        List<TestEntity> result = repository.findAll(spec);

        assertEquals(1, result.size());
        assertEquals("Eduardo", result.get(0).getName());

        // Test with null
        spec = SpecificationUtils.matchLesserOrEqualDateThan((LocalTime) null, "lastUsageTime");
        result = repository.findAll(spec);

        assertEquals(5, result.size());
    }

    /**
     * Tests {@link SpecificationUtils#matchGreaterDateThan(OffsetDateTime, String)} method.
     */
    @Test
    @DisplayName("Test matching offset date time lesser or equal than")
    void testMatchLesserOrEqualDateThanWithOffsetDateTime() {
        Specification<TestEntity> spec = SpecificationUtils.matchLesserOrEqualDateThan(
                OffsetDateTime.of(LocalDateTime.of(2023, 1, 1, 19, 31, 20),
                        DEFAULT_ZONE_OFFSET), "createdAt");
        List<TestEntity> result = repository.findAll(spec);

        assertEquals(2, result.size());
        assertEquals("Eduardo", result.get(0).getName());
        assertEquals("Laura", result.get(1).getName());

        // Test with null
        spec = SpecificationUtils.matchLesserOrEqualDateThan((OffsetDateTime) null, "createdAt");
        result = repository.findAll(spec);

        assertEquals(5, result.size());
    }

    /**
     * Tests {@link SpecificationUtils#matchLesserOrEqualDateThan(OffsetTime, String)} method.
     */
    @Test
    @DisplayName("Test matching offset time lesser or equal than")
    void testMatchLesserOrEqualDateThanWithOffsetTime() {
        Specification<TestEntity> spec = SpecificationUtils.matchLesserOrEqualDateThan(
                OffsetTime.of(LocalTime.of(2, 10, 0), DEFAULT_ZONE_OFFSET),
                "lastUsageTime");
        List<TestEntity> result = repository.findAll(spec);

        assertEquals(1, result.size());
        assertEquals("Eduardo", result.get(0).getName());

        // Test with null
        spec = SpecificationUtils.matchLesserOrEqualDateThan((OffsetTime) null, "lastUsageTime");
        result = repository.findAll(spec);

        assertEquals(5, result.size());
    }

    /**
     * Tests {@link SpecificationUtils#matchLesserOrEqualNumberThan(Number, String)} method.
     */
    @Test
    @DisplayName("Test matching number lesser or equal than")
    void testMatchLesserOrEqualNumberThan() {
        // Test with int
        Specification<TestEntity> spec = SpecificationUtils.matchLesserOrEqualNumberThan(25, "age");
        List<TestEntity> result = repository.findAll(spec);

        assertEquals(2, result.size());
        assertEquals("Eduardo", result.get(0).getName());
        assertEquals("Laura", result.get(1).getName());

        // Test with short
        spec = SpecificationUtils.matchLesserOrEqualNumberThan((short) 25, "age");
        List<TestEntity> resultToCompare = repository.findAll(spec);

        assertEquals(result, resultToCompare);

        // Test with long
        spec = SpecificationUtils.matchLesserOrEqualNumberThan(25L, "age");
        resultToCompare = repository.findAll(spec);

        assertEquals(result, resultToCompare);

        // Test with double
        spec = SpecificationUtils.matchLesserOrEqualNumberThan(25d, "age");
        resultToCompare = repository.findAll(spec);

        assertEquals(result, resultToCompare);

        // Test with float
        spec = SpecificationUtils.matchLesserOrEqualNumberThan(25f, "age");
        resultToCompare = repository.findAll(spec);

        assertEquals(result, resultToCompare);

        // Test with float
        spec = SpecificationUtils.matchLesserOrEqualNumberThan(new BigDecimal(25), "age");
        resultToCompare = repository.findAll(spec);

        assertEquals(result, resultToCompare);

        // Test with null
        spec = SpecificationUtils.matchLesserOrEqualNumberThan(null, "age");
        resultToCompare = repository.findAll(spec);

        assertEquals(5, resultToCompare.size());
    }

    /**
     * Tests {@link SpecificationUtils#matchDateBetween(Date, Date, String)} method.
     */
    @Test
    @DisplayName("Test matching date between")
    void testMatchDateBetweenWithDate() {
        // Date of 2023-01-01 18:31:20
        Date beforeDate = new Date(1672594280000L);
        // Date of 2024-01-01 05:11:21
        Date afterDate = new Date(1704082281000L);
        Specification<TestEntity> spec = SpecificationUtils.matchDateBetween(
                beforeDate, afterDate, "createdAt");
        List<TestEntity> result = repository.findAll(spec);

        assertEquals(2, result.size());
        assertEquals("Laura", result.get(0).getName());
        assertEquals("Manolo", result.get(1).getName());

        // Test with null
        spec = SpecificationUtils.matchDateBetween(null, (Date) null, "createdAt");
        result = repository.findAll(spec);

        assertEquals(5, result.size());
    }

    /**
     * Tests {@link SpecificationUtils#matchDateBetween(LocalDate, LocalDate, String)} method.
     */
    @Test
    @DisplayName("Test matching local date between")
    void testMatchDateBetweenWithLocalDate() {
        Specification<TestEntity> spec = SpecificationUtils.matchDateBetween(
                LocalDate.of(2023, 1, 1),
                LocalDate.of(2024, 1, 2),
                "createdAt");
        List<TestEntity> result = repository.findAll(spec);

        assertEquals(2, result.size());
        assertEquals("Laura", result.get(0).getName());
        assertEquals("Manolo", result.get(1).getName());

        // Test with null
        spec = SpecificationUtils.matchDateBetween(null, (LocalDate) null, "createdAt");
        result = repository.findAll(spec);

        assertEquals(5, result.size());
    }

    /**
     * Tests {@link SpecificationUtils#matchDateBetween(LocalDateTime, LocalDateTime, String)} method.
     */
    @Test
    @DisplayName("Test matching local date time between")
    void testMatchDateBetweenWithLocalDateTime() {
        Specification<TestEntity> spec = SpecificationUtils.matchDateBetween(
                LocalDateTime.of(2023, 1, 1, 18, 31, 20),
                LocalDateTime.of(2024, 1, 1, 5, 11, 21), "createdAt");
        List<TestEntity> result = repository.findAll(spec);

        assertEquals(2, result.size());
        assertEquals("Laura", result.get(0).getName());
        assertEquals("Manolo", result.get(1).getName());

        // Test with null
        spec = SpecificationUtils.matchDateBetween(null, (LocalDateTime) null, "createdAt");
        result = repository.findAll(spec);

        assertEquals(5, result.size());
    }

    /**
     * Tests {@link SpecificationUtils#matchDateBetween(LocalTime, LocalTime, String)} method.
     */
    @Test
    @DisplayName("Test matching local time between")
    void testMatchDateBetweenWithLocalTime() {
        Specification<TestEntity> spec = SpecificationUtils.matchDateBetween(
                LocalTime.of(0, 10, 0),
                LocalTime.of(2, 52, 13),
                "lastUsageTime");
        List<TestEntity> result = repository.findAll(spec);

        assertEquals(2, result.size());
        assertEquals("Eduardo", result.get(0).getName());
        assertEquals("Laura", result.get(1).getName());

        // Test with null
        spec = SpecificationUtils.matchDateBetween(null, (LocalTime) null, "lastUsageTime");
        result = repository.findAll(spec);

        assertEquals(5, result.size());
    }

    /**
     * Tests {@link SpecificationUtils#matchDateBetween(OffsetDateTime, OffsetDateTime, String)} method.
     */
    @Test
    @DisplayName("Test matching offset date time between")
    void testMatchDateBetweenWithOffsetDateTime() {
        Specification<TestEntity> spec = SpecificationUtils.matchDateBetween(
                OffsetDateTime.of(LocalDateTime.of(2023, 1, 1, 19, 31, 20),
                        DEFAULT_ZONE_OFFSET),
                OffsetDateTime.of(LocalDateTime.of(2024, 1, 1, 6, 11, 21),
                        DEFAULT_ZONE_OFFSET), "createdAt");
        List<TestEntity> result = repository.findAll(spec);

        assertEquals(2, result.size());
        assertEquals("Laura", result.get(0).getName());
        assertEquals("Manolo", result.get(1).getName());

        // Test with null
        spec = SpecificationUtils.matchDateBetween(null, (OffsetDateTime) null, "createdAt");
        result = repository.findAll(spec);

        assertEquals(5, result.size());
    }

    /**
     * Tests {@link SpecificationUtils#matchDateBetween(OffsetTime, OffsetTime, String)} method.
     */
    @Test
    @DisplayName("Test matching offset time lesser or equal than")
    void testMatchDateBetweenWithOffsetTime() {
        Specification<TestEntity> spec = SpecificationUtils.matchDateBetween(
                OffsetTime.of(LocalTime.of(2, 10, 0), DEFAULT_ZONE_OFFSET),
                OffsetTime.of(LocalTime.of(4, 52, 13), DEFAULT_ZONE_OFFSET),
                "lastUsageTime");
        List<TestEntity> result = repository.findAll(spec);

        assertEquals(2, result.size());
        assertEquals("Eduardo", result.get(0).getName());
        assertEquals("Laura", result.get(1).getName());

        // Test with null
        spec = SpecificationUtils.matchDateBetween(null, (OffsetTime) null, "lastUsageTime");
        result = repository.findAll(spec);

        assertEquals(5, result.size());
    }

    /**
     * Tests {@link SpecificationUtils#matchNumberBetween(Number, Number, String)} method.
     */
    @Test
    @DisplayName("Test matching number lesser or equal than")
    void testMatchNumberBetween() {
        // Test with int
        Specification<TestEntity> spec = SpecificationUtils.matchNumberBetween(25, 40, "age");
        List<TestEntity> result = repository.findAll(spec);

        assertEquals(1, result.size());
        assertEquals("Manolo", result.get(0).getName());

        // Test with short
        spec = SpecificationUtils.matchNumberBetween((short) 25, (short) 40, "age");
        List<TestEntity> resultToCompare = repository.findAll(spec);

        assertEquals(result, resultToCompare);

        // Test with long
        spec = SpecificationUtils.matchNumberBetween(25L, 40L, "age");
        resultToCompare = repository.findAll(spec);

        assertEquals(result, resultToCompare);

        // Test with double
        spec = SpecificationUtils.matchNumberBetween(25d, 40d, "age");
        resultToCompare = repository.findAll(spec);

        assertEquals(result, resultToCompare);

        // Test with float
        spec = SpecificationUtils.matchNumberBetween(25f, 40f, "age");
        resultToCompare = repository.findAll(spec);

        assertEquals(result, resultToCompare);

        // Test with float
        spec = SpecificationUtils.matchNumberBetween(new BigDecimal(25), new BigDecimal(40), "age");
        resultToCompare = repository.findAll(spec);

        assertEquals(result, resultToCompare);

        // Test with null
        spec = SpecificationUtils.matchNumberBetween(null, null, "age");
        resultToCompare = repository.findAll(spec);

        assertEquals(5, resultToCompare.size());
    }

    /**
     * Tests {@link SpecificationUtils#matchIfTrue(String)} method.
     */
    @Test
    @DisplayName("Test matching boolean as true")
    void testMatchIfTrue() {
        Specification<TestEntity> spec = SpecificationUtils.matchIfTrue("active");
        List<TestEntity> result = repository.findAll(spec);

        assertEquals(3, result.size());
        assertEquals("Eduardo", result.get(0).getName());
        assertEquals("Laura", result.get(1).getName());
        assertEquals("Juan", result.get(2).getName());
    }

    /**
     * Tests {@link SpecificationUtils#matchIfFalse(String)} method.
     */
    @Test
    @DisplayName("Test matching boolean as false")
    void testMatchIfFalse() {
        Specification<TestEntity> spec = SpecificationUtils.matchIfFalse("active");
        List<TestEntity> result = repository.findAll(spec);

        assertEquals(1, result.size());
        assertEquals("Manolo", result.get(0).getName());
    }

    /**
     * Tests {@link SpecificationUtils#matchIfNullOrFalse(String)} method.
     */
    @Test
    @DisplayName("Test matching boolean as null or false")
    void testMatchIfNullOrFalse() {
        Specification<TestEntity> spec = SpecificationUtils.matchIfNullOrFalse("active");
        List<TestEntity> result = repository.findAll(spec);

        assertEquals(2, result.size());
        assertEquals("Manolo", result.get(0).getName());
        assertEquals("Angel", result.get(1).getName());
    }

    /**
     * Tests {@link SpecificationUtils#matchIfNull(String)} method.
     */
    @Test
    @DisplayName("Test matching null values")
    void testMatchIfNull() {
        Specification<TestEntity> spec = SpecificationUtils.matchIfNull("active");
        List<TestEntity> result = repository.findAll(spec);

        assertEquals(1, result.size());
        assertEquals("Angel", result.get(0).getName());
    }

    /**
     * Tests {@link SpecificationUtils#matchIfNotNull(String)} method.
     */
    @Test
    @DisplayName("Test matching not null values")
    void testMatchIfNotNull() {
        Specification<TestEntity> spec = SpecificationUtils.matchIfNotNull("description");
        List<TestEntity> result = repository.findAll(spec);

        assertEquals(1, result.size());
        assertEquals("Juan", result.get(0).getName());
    }

    /**
     * Tests {@link SpecificationUtils#matchBooleanValue(Boolean, String)} method.
     */
    @Test
    @DisplayName("Test matching boolean values")
    void testMatchBooleanValue() {
        String param = "active";

        // Test true value
        Specification<TestEntity> spec = SpecificationUtils.matchBooleanValue(true, param);
        List<TestEntity> result = repository.findAll(spec);

        assertEquals(3, result.size());
        assertEquals("Eduardo", result.get(0).getName());
        assertEquals("Laura", result.get(1).getName());
        assertEquals("Juan", result.get(2).getName());

        // Test false value
        spec = SpecificationUtils.matchBooleanValue(false, param);
        result = repository.findAll(spec);

        assertEquals(2, result.size());
        assertEquals("Manolo", result.get(0).getName());
        assertEquals("Angel", result.get(1).getName());

        // Test null value
        spec = SpecificationUtils.matchBooleanValue(null, param);
        result = repository.findAll(spec);

        assertEquals(2, result.size());
        assertEquals("Manolo", result.get(0).getName());
        assertEquals("Angel", result.get(1).getName());
    }

    /**
     * Tests {@link SpecificationUtils#matchStringRawLike(String, String)} method.
     */
    @Test
    @DisplayName("Test matching string with raw like")
    void testMatchStringRawLike() {
        Specification<TestEntity> spec = SpecificationUtils.matchStringRawLike("SOME%", "description");
        List<TestEntity> result = repository.findAll(spec);

        assertEquals(1, result.size());
        assertEquals("Juan", result.get(0).getName());

        // Match lowercase, which shouldn't match
        spec = SpecificationUtils.matchStringRawLike("some%", "description");
        result = repository.findAll(spec);

        assertEquals(0, result.size());
    }

    /**
     * Tests {@link SpecificationUtils#matchStringLike(String, String)} method.
     */
    @Test
    @DisplayName("Test matching string with like with built-in percentages")
    void testMatchStringLike() {
        Specification<TestEntity> spec = SpecificationUtils.matchStringLike("SOME", "description");
        List<TestEntity> result = repository.findAll(spec);

        assertEquals(1, result.size());
        assertEquals("Juan", result.get(0).getName());

        // Match lowercase, which shouldn't match
        spec = SpecificationUtils.matchStringLike("some", "description");
        result = repository.findAll(spec);

        assertEquals(0, result.size());
    }

    /**
     * Tests {@link SpecificationUtils#matchStringRawNotLike(String, String)} method.
     */
    @Test
    @DisplayName("Test matching string with raw NOT like")
    void testMatchStringRawNotLike() {
        Specification<TestEntity> spec = SpecificationUtils.matchStringRawNotLike("SOME%", "description");
        List<TestEntity> result = repository.findAll(spec);

        assertEquals(0, result.size());

        // Match lowercase, which should match
        spec = SpecificationUtils.matchStringRawNotLike("some%", "description");
        result = repository.findAll(spec);

        assertEquals(1, result.size());
        assertEquals("Juan", result.get(0).getName());
    }

    /**
     * Tests {@link SpecificationUtils#matchStringNotLike(String, String)} method.
     */
    @Test
    @DisplayName("Test matching string with NOT like with built-in percentages")
    void testMatchStringNotLike() {
        Specification<TestEntity> spec = SpecificationUtils.matchStringNotLike("SOME", "description");
        List<TestEntity> result = repository.findAll(spec);

        assertEquals(0, result.size());

        // Match lowercase, which should match
        spec = SpecificationUtils.matchStringNotLike("some", "description");
        result = repository.findAll(spec);

        assertEquals(1, result.size());
        assertEquals("Juan", result.get(0).getName());
    }

    /**
     * Tests {@link SpecificationUtils#matchStringRawLikeCaseInsensitive(String, String)} method.
     */
    @Test
    @DisplayName("Test matching string with raw like as case insensitive")
    void testMatchStringRawLikeCaseInsensitive() {
        Specification<TestEntity> spec = SpecificationUtils.matchStringRawLikeCaseInsensitive("SOME%",
                "description");
        List<TestEntity> result = repository.findAll(spec);

        assertEquals(1, result.size());
        assertEquals("Juan", result.get(0).getName());

        // Match lowercase, which shouldn't match
        spec = SpecificationUtils.matchStringRawLikeCaseInsensitive("some%", "description");
        result = repository.findAll(spec);

        assertEquals(1, result.size());
        assertEquals("Juan", result.get(0).getName());
    }

    /**
     * Tests {@link SpecificationUtils#matchStringLikeCaseInsensitive(String, String)} method.
     */
    @Test
    @DisplayName("Test matching string with like with built-in percentages as case insensitive")
    void testMatchStringLikeCaseInsensitive() {
        Specification<TestEntity> spec = SpecificationUtils.matchStringLikeCaseInsensitive("SOME",
                "description");
        List<TestEntity> result = repository.findAll(spec);

        assertEquals(1, result.size());
        assertEquals("Juan", result.get(0).getName());

        // Match lowercase, which shouldn't match
        spec = SpecificationUtils.matchStringLikeCaseInsensitive("some", "description");
        result = repository.findAll(spec);

        assertEquals(1, result.size());
        assertEquals("Juan", result.get(0).getName());
    }

    /**
     * Tests {@link SpecificationUtils#matchStringRawNotLikeCaseInsensitive(String, String)} method.
     */
    @Test
    @DisplayName("Test matching string with raw NOT like as case insensitive")
    void testMatchStringRawNotLikeCaseInsensitive() {
        Specification<TestEntity> spec = SpecificationUtils.matchStringRawNotLikeCaseInsensitive("SOME%",
                "description");
        List<TestEntity> result = repository.findAll(spec);

        assertEquals(0, result.size());

        // Match lowercase, which should match
        spec = SpecificationUtils.matchStringRawNotLikeCaseInsensitive("some%",
                "description");
        result = repository.findAll(spec);

        assertEquals(0, result.size());

        // Test matching value
        spec = SpecificationUtils.matchStringRawNotLikeCaseInsensitive("other%",
                "description");
        result = repository.findAll(spec);

        assertEquals(1, result.size());
        assertEquals("Juan", result.get(0).getName());
    }

    /**
     * Tests {@link SpecificationUtils#matchStringNotLikeCaseInsensitive(String, String)} method.
     */
    @Test
    @DisplayName("Test matching string with NOT like with built-in percentages as case insensitive")
    void testMatchStringNotLikeCaseInsensitive() {
        Specification<TestEntity> spec = SpecificationUtils.matchStringNotLikeCaseInsensitive("SOME",
                "description");
        List<TestEntity> result = repository.findAll(spec);

        assertEquals(0, result.size());

        // Match lowercase, which should match
        spec = SpecificationUtils.matchStringNotLikeCaseInsensitive("some",
                "description");
        result = repository.findAll(spec);

        assertEquals(0, result.size());

        // Test matching value
        spec = SpecificationUtils.matchStringRawNotLikeCaseInsensitive("other",
                "description");
        result = repository.findAll(spec);

        assertEquals(1, result.size());
        assertEquals("Juan", result.get(0).getName());
    }

    /**
     * Tests {@link SpecificationUtils#matchValueIn} method.
     */
    @Test
    @DisplayName("Test matching a value from a collection")
    void testMatchValueIn() {
        Specification<TestEntity> spec = SpecificationUtils.matchValueIn(List.of(18, 22),
                "age");
        List<TestEntity> result = repository.findAll(spec);

        assertEquals(2, result.size());
        assertEquals("Eduardo", result.get(0).getName());
        assertEquals("Laura", result.get(1).getName());

        // Match lowercase, which shouldn't match
        spec = SpecificationUtils.matchValueIn(Collections.emptyList(), "description");
        result = repository.findAll(spec);

        assertEquals(5, result.size());
    }
}

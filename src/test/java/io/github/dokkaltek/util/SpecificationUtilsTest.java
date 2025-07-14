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
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for {@link SpecificationUtils} class.
 */
@SpringBootTest
@ContextConfiguration(classes = {TestIntegrationRunner.class})
@TestPropertySource("/application.properties")
@Transactional
class SpecificationUtilsTest {

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
                        ZoneOffset.of("+01:00")), "createdAt");
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
                OffsetTime.of(LocalTime.of(11, 1), ZoneOffset.of("+01:00")),
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
                        ZoneOffset.of("+01:00")), "createdAt");
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
                OffsetTime.of(LocalTime.of(16, 5,13), ZoneOffset.of("+01:00")),
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


}

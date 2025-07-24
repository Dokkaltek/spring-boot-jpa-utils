package io.github.dokkaltek.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Test entity for database operations.
 */
@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "oracle_test_entities")
@SequenceGenerator(name = "test_entity_seq", sequenceName = "test_sequence", allocationSize = 1)
public class OracleTestEntity implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "test_entity_seq")
    private Long id;
    @Column(name = "name")
    private String name;
    @Column(name = "description")
    private String description;
    private Integer age;
    @Column(name = "\"active\"")
    private Boolean active;
    @Column(name = "created_at")
    private LocalDateTime createdAt;
}

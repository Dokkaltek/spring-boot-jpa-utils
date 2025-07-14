package io.github.dokkaltek.samples;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@IdClass(SampleEmbeddedId.class)
@Table(name = "sample_entity")
@SequenceGenerator(name = "sample_seq", sequenceName = "sample_seq", allocationSize = 1)
public class SampleIdClassEntity implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    private Long id;

    @Id
    @Column(name = "entity2")
    private String entity;

    private String name;

    @Column(name = "desc")
    private String description;
}

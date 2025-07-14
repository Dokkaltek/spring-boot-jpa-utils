package io.github.dokkaltek.samples;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.SequenceGenerator;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@Data
@Embeddable
@NoArgsConstructor
@AllArgsConstructor
public class SampleEmbeddedId implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @SequenceGenerator(name = "sample_seq", sequenceName = "sample_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sample_seq")
    private Long id;

    @Column(name = "entity2")
    private String entity;
}

package io.github.dokkaltek.samples;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
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
@Table(name = "sample_entity")
@SequenceGenerator(name = "sample_seq", sequenceName = "sample_seq", allocationSize = 1)
public class SampleEntity implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @EmbeddedId
    private SampleEmbeddedId embeddedId;
    private String name;

    @Column(name = "desc")
    private String description;
}

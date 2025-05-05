package io.github.dokkaltek.helper;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collection;

/**
 * Wrapper for collections that must be inserted in a database using a sequence.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EntriesWithSequence {
    private String sequenceName;
    @NotNull
    @NotBlank
    private String sequenceField;
    private int allocationSize;
    @NotNull
    private Collection<?> entries;
}

package io.github.dokkaltek.helper;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Wrapper for primary key entries.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PrimaryKeyEntries<S, I> {
    private List<I> ids;
    private Class<S> entityClass;
}

package io.github.dokkaltek.helper;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * A query object to hold the query and the bindings of a batch.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BatchData {
    private String query;
    private List<Map<Integer, Object>> queriesBindings;
}

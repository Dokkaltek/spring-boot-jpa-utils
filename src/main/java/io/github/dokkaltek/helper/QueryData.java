package io.github.dokkaltek.helper;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * A query object to hold the query and the bindings.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class QueryData {
    private String query;
    private Map<Integer, Object> positionBindings;
    private Map<String, Object> namedBindings;

    /**
     * Constructor that accepts both a query and a map of bindings (Either Integer key or String key map)
     * @param query The query.
     * @param bindings The bindings.
     */
    public QueryData(String query, Map<?, Object> bindings) {
        this.query = query;

        if (bindings == null) {
            return;
        }

        if (bindings.keySet().stream().anyMatch(Integer.class::isInstance)) {
            this.positionBindings = (Map<Integer, Object>) bindings;
        } else {
            this.namedBindings = (Map<String, Object>) bindings;
        }
    }
}

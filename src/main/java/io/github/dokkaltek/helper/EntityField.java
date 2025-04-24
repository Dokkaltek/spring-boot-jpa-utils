package io.github.dokkaltek.helper;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Representation of an entity field.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EntityField {
    private String fieldName;
    private String columnName;
    private Object value;
}

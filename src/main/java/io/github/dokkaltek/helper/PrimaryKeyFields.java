package io.github.dokkaltek.helper;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.lang.reflect.Field;
import java.util.List;

/**
 * Wrapper for primary key fields.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PrimaryKeyFields {
    private List<Field> fields;
    private boolean isEmbeddedId;
    private Class<?> embeddedIdClass;
    private String embeddedIdFieldName;
}

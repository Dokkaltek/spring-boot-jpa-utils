package io.github.dokkaltek.samples;

import jakarta.persistence.Column;
import jakarta.persistence.Table;

/**
 * Sample POJO class to test reflection.
 */
@Table
public class SamplePojoParent {
    @Column(name = "desc")
    private String description;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}

package vnikolaenko.github.jarch.generator.auxiliary;

import lombok.Data;

@Data
public class Field {
    private String fieldName;
    private String fieldType;
    private Relation relation;
}

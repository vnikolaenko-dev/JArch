package github.vnikolaenko.jarch.generator.config;

import lombok.Data;

import java.util.List;

@Data
public class EntityConfig {
    private List<EntityDefinition> entities;

    @Data
    public static class EntityDefinition {
        private String name;
        private String description;
        private List<FieldDefinition> fields;
    }

    @Data
    public static class FieldDefinition {
        private String name;
        private String type;
        private String description;
        private boolean required;
        private RelationDefinition relation;
    }

    @Data
    public static class RelationDefinition {
        private String type;
        private String targetEntity;
        private String fetchType;
        private String cascadeType;
    }
}
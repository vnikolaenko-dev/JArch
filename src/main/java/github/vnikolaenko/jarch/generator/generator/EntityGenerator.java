package github.vnikolaenko.jarch.generator.generator;

import github.vnikolaenko.jarch.generator.auxiliary.Field;
import github.vnikolaenko.jarch.generator.auxiliary.Relation;
import github.vnikolaenko.jarch.generator.auxiliary.TypeOfRelation;
import github.vnikolaenko.jarch.generator.config.ApplicationConfig;
import github.vnikolaenko.jarch.generator.config.EntityConfig;
import github.vnikolaenko.jarch.generator.generator.included.*;
import github.vnikolaenko.jarch.utils.LogCollector;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class EntityGenerator {

    private final LogCollector logCollector;

    public void generateAllEntities(ApplicationConfig appConfig, EntityConfig entityConfig, Path targetDir) throws Exception {
        var entityDefinitions = entityConfig.getEntities().stream()
                .collect(Collectors.toMap(
                        EntityConfig.EntityDefinition::getName,
                        entityDef -> convertFields(entityDef, entityConfig)
                ));

        for (var entry : entityDefinitions.entrySet()) {
            String entityName = entry.getKey();
            var fields = entry.getValue();

            logCollector.info("Generating entity: " + entityName + " with " + fields.size() + " fields");
            fields.forEach(field -> {
                logCollector.info("  Field: " + field.getFieldName() + " type: " + field.getFieldType() +
                        " relation: " + (field.getRelation() != null ? field.getRelation().getTypeOfRelation() : "none"));
            });

            System.out.println(targetDir);
            ModelGenerator.generateEntity(appConfig.getBasePackage(), entityName, fields, targetDir);
            DtoGenerator.generateDTO(appConfig.getBasePackage(), entityName, fields, targetDir);
            RepositoryGenerator.generateRepository(appConfig.getBasePackage(), entityName, fields, targetDir);
            ServiceGenerator.generateService(appConfig.getBasePackage(), entityName, targetDir);
            ControllerGenerator.generateController(appConfig.getBasePackage(), entityName, targetDir);
            ConfigGenerator.generateModelMapperConfig(appConfig.getBasePackage(), targetDir);
        }
    }

    private List<Field> convertFields(EntityConfig.EntityDefinition entityDef, EntityConfig entityConfig) {
        List<Field> fields = new ArrayList<>();

        if (entityDef.getFields() != null) {
            for (EntityConfig.FieldDefinition fieldDef : entityDef.getFields()) {
                Field field = new Field();
                field.setFieldName(fieldDef.getName());
                field.setFieldType(fieldDef.getType());

                // Конвертация отношений
                if (fieldDef.getRelation() != null) {
                    Relation relation = new Relation();
                    try {
                        relation.setTypeOfRelation(TypeOfRelation.valueOf(fieldDef.getRelation().getType()));
                        field.setRelation(relation);
                        logCollector.info("Added relation: " + fieldDef.getRelation().getType() +
                                " for field: " + fieldDef.getName());
                    } catch (IllegalArgumentException e) {
                        logCollector.info("Unknown relation type: " + fieldDef.getRelation().getType() +
                                " for field: " + fieldDef.getName());
                    }
                }

                fields.add(field);
            }
        }

        return fields;
    }
}
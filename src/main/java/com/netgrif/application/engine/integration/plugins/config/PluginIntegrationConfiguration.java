package com.netgrif.application.engine.integration.plugins.config;

import com.netgrif.application.engine.petrinet.domain.dataset.*;
import com.netgrif.application.engine.workflow.web.responsebodies.*;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.pf4j.JarPluginManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import petrinet.domain.dataset.*;
import workflow.web.responsebodies.*;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Configuration class for plugin management
 * */
@Slf4j
@Configuration
public class PluginIntegrationConfiguration {

    private static final Class<?>[] fieldClasses = new Class[] {
            BooleanField.class,
            ButtonField.class,
            CaseField.class,
            DateField.class,
            DateTimeField.class,
            EnumerationField.class,
            EnumerationMapField.class,
            FileField.class,
            FileListField.class,
            FilterField.class,
            I18nField.class,
            MultichoiceField.class,
            MultichoiceMapField.class,
            NumberField.class,
            TaskField.class,
            TextField.class,
            UserField.class
    };

    private static final Class<?>[] localisedFieldClasses = new Class[] {
            LocalisedBooleanField.class,
            LocalisedCaseField.class,
            LocalisedDateField.class,
            LocalisedDateTimeField.class,
            LocalisedEnumerationField.class,
            LocalisedEnumerationMapField.class,
            LocalisedFileListField.class,
            LocalisedFilterField.class,
            LocalisedI18nField.class,
            LocalisedMultichoiceField.class,
            LocalisedMultichoiceMapField.class,
            LocalisedNumberField.class,
            LocalisedTextField.class,
            LocalisedUserField.class
    };

    private static final Map<FieldType, List<Class<?>>> fieldTypeMapping = new HashMap<>() {{
        put(FieldType.BOOLEAN, List.of(BooleanFieldDto.class, LocalisedBooleanFieldDto.class));
        put(FieldType.BUTTON, List.of(ButtonFieldDto.class));
        put(FieldType.CASE_REF, List.of(CaseFieldDto.class, LocalisedCaseFieldDto.class));
        put(FieldType.DATE, List.of(DateFieldDto.class, LocalisedDateFieldDto.class));
        put(FieldType.DATETIME, List.of(DateTimeFieldDto.class, LocalisedDateFieldDto.class));
        put(FieldType.ENUMERATION, List.of(EnumerationFieldDto.class, LocalisedEnumerationFieldDto.class));
        put(FieldType.ENUMERATION_MAP, List.of(EnumerationMapFieldDto.class, LocalisedEnumerationMapFieldDto.class));
        put(FieldType.FILE, List.of(FileFieldDto.class));
        put(FieldType.FILELIST, List.of(FileListFieldDto.class, LocalisedFileListFieldDto.class));
        put(FieldType.FILTER, List.of(FilterFieldDto.class, LocalisedFilterFieldDto.class));
        put(FieldType.I18N, List.of(I18nFieldDto.class, LocalisedI18nFieldDto.class));
        put(FieldType.MULTICHOICE, List.of(MultichoiceFieldDto.class, LocalisedMultichoiceFieldDto.class));
        put(FieldType.MULTICHOICE_MAP, List.of(MultichoiceMapFieldDto.class, LocalisedMultichoiceMapFieldDto.class));
        put(FieldType.NUMBER, List.of(NumberFieldDto.class, LocalisedNumberFieldDto.class));
        put(FieldType.TASK_REF, List.of(TaskFieldDto.class));
        put(FieldType.TEXT, List.of(TextFieldDto.class, LocalisedTextFieldDto.class));
        put(FieldType.USER, List.of(UserFieldDto.class, LocalisedUserFieldDto.class));
        put(FieldType.USERLIST, List.of(UserListFieldDto.class));
    }};

    /**
     * The plugin manager bean to load plugins as JAR files
     * */
    @Bean
    public JarPluginManager pluginManager() {
        return new JarPluginManager();
    }


    /**
     * Model mapper bean to convert domain objects to DTOs
     * */
    @Bean
    public ModelMapper modelMapper() {
        ModelMapper mapper = new ModelMapper();

        Arrays.stream(fieldClasses).forEach(c ->
                mapper.createTypeMap(c, FieldDto.class).setConverter(ctx -> fieldToDto((Field<?>) ctx.getSource())));

        Arrays.stream(localisedFieldClasses).forEach(c ->
                mapper.createTypeMap(c, LocalisedFieldDto.class).setConverter(ctx -> localisedFieldToDto((LocalisedField) ctx.getSource())));

        return mapper;
    }

    private FieldDto<?> fieldToDto(Field<?> field) {
        final ModelMapper innerMapper = new ModelMapper();
        return innerMapper.map(field, (Type) fieldTypeMapping.get(field.getType()).get(0));
    }

    private LocalisedFieldDto localisedFieldToDto(LocalisedField field) {
        final ModelMapper innerMapper = new ModelMapper();
        return innerMapper.map(field, (Type) fieldTypeMapping.get(field.getType()).get(1));
    }
}

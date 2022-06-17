package com.netgrif.application.engine.petrinet.domain.dataset;

import com.netgrif.application.engine.importer.model.DataType;
import com.netgrif.application.engine.petrinet.domain.I18nString;
import lombok.Data;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Data
public class MultichoiceMapField extends MapOptionsField<I18nString, Set<String>> {

    public MultichoiceMapField() {
        super();
        this.defaultValue = new HashSet<>();
    }

    public MultichoiceMapField(Map<String, I18nString> choices) {
        super(choices);
        this.defaultValue = new HashSet<>();
    }

    public MultichoiceMapField(Map<String, I18nString> choices, Set<String> defaultValues) {
        this(choices);
        this.defaultValue = defaultValues;
    }

    @Override
    public DataType getType() {
        return DataType.MULTICHOICE_MAP;
    }

    public List<I18nString> getSelectedOptions() {
        if (this.getValue() == null || this.getOptions() == null) {
            return null;
        }
        return this.getValue().stream().map(v -> this.getOptions().get(v)).collect(Collectors.toList());
    }

    @Override
    public MultichoiceMapField clone() {
        MultichoiceMapField clone = new MultichoiceMapField();
        super.clone(clone);
        clone.options = options;
        clone.optionsExpression = optionsExpression;
        return clone;
    }
}

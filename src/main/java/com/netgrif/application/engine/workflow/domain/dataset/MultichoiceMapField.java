package com.netgrif.application.engine.workflow.domain.dataset;

import com.netgrif.application.engine.importer.model.DataType;
import com.netgrif.application.engine.workflow.domain.I18nString;
import com.querydsl.core.annotations.PropertyType;
import com.querydsl.core.annotations.QueryType;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
public class MultichoiceMapField extends MapOptionsField<I18nString, Set<String>> {

    @Override
    @QueryType(PropertyType.NONE)
    public DataType getType() {
        return DataType.MULTICHOICE_MAP;
    }

    public List<I18nString> getSelectedOptions() {
        if (this.getValue() == null || this.getOptions() == null) {
            return null;
        }
        return this.getValue().getValue().stream().map(v -> this.getOptions().get(v)).collect(Collectors.toList());
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

package com.netgrif.application.engine.importer.service.builder;

import com.netgrif.application.engine.importer.model.Data;
import com.netgrif.application.engine.importer.model.DataType;
import com.netgrif.application.engine.importer.service.Importer;
import com.netgrif.application.engine.petrinet.domain.I18nString;
import com.netgrif.application.engine.petrinet.domain.dataset.I18nField;
import com.netgrif.application.engine.petrinet.domain.dataset.logic.action.runner.Expression;
import org.springframework.stereotype.Component;

@Component
public class I18bFieldBuilder extends FieldBuilder<I18nField> {
    @Override
    public I18nField build(Data data, Importer importer) {
        I18nField i18nField = new I18nField();
        String initExpression = getInitExpression(data);
        if (initExpression != null) {
            i18nField.setInitExpression(new Expression(initExpression));
        } else {
            if (data.getInits() != null && data.getInits().getInit() != null && !data.getInits().getInit().isEmpty()) {
                i18nField.setDefaultValue(new I18nString(data.getInits().getInit().get(0).getValue()));
            } else if (data.getInit() != null && (data.getInit().getName() == null || data.getInit().getName().equals(""))) {
                i18nField.setDefaultValue(new I18nString(data.getInit().getValue()));
            } else if (data.getInit() != null && data.getInit().getName() != null && !data.getInit().getName().equals("")) {
                i18nField.setDefaultValue(importer.toI18NString(data.getInit()));
            } else {
                i18nField.setDefaultValue(new I18nString(""));
            }
        }
        return i18nField;
    }

    @Override
    public DataType getType() {
        return DataType.I_18_N;
    }
}

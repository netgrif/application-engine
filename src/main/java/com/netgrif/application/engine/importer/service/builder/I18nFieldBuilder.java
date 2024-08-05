package com.netgrif.application.engine.importer.service.builder;

import com.netgrif.application.engine.importer.model.Data;
import com.netgrif.application.engine.importer.model.DataType;
import com.netgrif.application.engine.importer.service.Importer;
import com.netgrif.application.engine.petrinet.domain.I18nString;
import com.netgrif.application.engine.petrinet.domain.dataset.I18nField;
import com.netgrif.application.engine.petrinet.domain.dataset.logic.Expression;
import org.springframework.stereotype.Component;

@Component
public class I18nFieldBuilder extends FieldBuilder<I18nField> {

    @Override
    public I18nField build(Data data, Importer importer) {
        I18nField field = new I18nField();
        initialize(field);
        // TODO: release/8.0.0
        String initExpression = getInitExpression(data);
//        if (initExpression != null) {
//            field.setDynamicDefaultValue(new Expression(initExpression, true));
//        } else {
            // TODO: release/8.0.0 simplify
//            if (data.getInits() != null && data.getInits().getInit() != null && !data.getInits().getInit().isEmpty()) {
//                field.setDefaultValue(new I18nString(data.getInits().getInit().get(0).getValue()));
//            } else if (data.getInit() != null && (data.getInit().getName() == null || data.getInit().getName().equals(""))) {
//                field.setDefaultValue(new I18nString(data.getInit().getValue()));
//            } else if (data.getInit() != null && data.getInit().getName() != null && !data.getInit().getName().equals("")) {
//                field.setDefaultValue(importer.toI18NString(data.getInit()));
//            } else {
//                field.setDefaultValue(new I18nString(""));
//            }
//        }
        return field;
    }

    @Override
    public DataType getType() {
        return DataType.I_18_N;
    }
}

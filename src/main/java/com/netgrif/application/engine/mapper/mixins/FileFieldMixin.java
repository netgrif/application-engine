package com.netgrif.application.engine.mapper.mixins;

import com.fasterxml.jackson.annotation.JsonView;
import com.netgrif.application.engine.mapper.annotation.NaeMixin;
import com.netgrif.application.engine.mapper.views.Views;
import com.netgrif.application.engine.petrinet.domain.dataset.FileField;
import com.netgrif.application.engine.petrinet.domain.dataset.FileFieldValue;
import org.springframework.beans.factory.annotation.Lookup;

@NaeMixin
public abstract class FileFieldMixin extends FieldMixin<FileFieldValue> {

    @Lookup
    public static Class<?> getOriginalType() {
        return FileField.class;
    }

    @JsonView(Views.GetData.class)
    public abstract Boolean isRemote();

}

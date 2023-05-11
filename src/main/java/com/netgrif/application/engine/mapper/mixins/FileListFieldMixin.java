package com.netgrif.application.engine.mapper.mixins;

import com.fasterxml.jackson.annotation.JsonView;
import com.netgrif.application.engine.mapper.annotation.NaeMixin;
import com.netgrif.application.engine.mapper.views.Views;
import com.netgrif.application.engine.petrinet.domain.dataset.FileListField;
import com.netgrif.application.engine.petrinet.domain.dataset.FileListFieldValue;
import org.springframework.beans.factory.annotation.Lookup;

@NaeMixin
public abstract class FileListFieldMixin extends FieldMixin<FileListFieldValue> {

    @Lookup
    public static Class<?> getOriginalType() {
        return FileListField.class;
    }

    @JsonView(Views.GetData.class)
    public abstract Boolean isRemote();
}

package com.netgrif.workflow.importer.service;

import com.netgrif.workflow.importer.model.Data;
import com.netgrif.workflow.importer.model.DataType;
import com.netgrif.workflow.petrinet.domain.views.BooleanImageView;
import com.netgrif.workflow.petrinet.domain.views.View;
import org.springframework.stereotype.Component;

@Component
public class ViewFactory {

    public View buildView(Data data) {
        if (data.getView().getImage() != null) {
            return buildImageView(data);
        } else {
            throw new UnsupportedViewException();
        }
    }

    public View buildImageView(Data data) {
        if (data.getType() == DataType.BOOLEAN) {
            BooleanImageView view = new BooleanImageView();

            view.setFalseImage(data.getView().getImage().getFalse());
            view.setTrueImage(data.getView().getImage().getTrue());

            return view;
        } else {
            throw new UnsupportedViewException("image", data.getType());
        }
    }

    class UnsupportedViewException extends RuntimeException {

        public UnsupportedViewException() {
            super("Unsupported view type");
        }

        UnsupportedViewException(String view, DataType type) {
            super("Unsupported view \"" + view + "\" for data type " + type.value());
        }
    }
}
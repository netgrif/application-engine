package com.netgrif.application.engine.importer.service;

import com.netgrif.application.engine.importer.model.Data;
import com.netgrif.application.engine.importer.model.DataType;
import com.netgrif.application.engine.petrinet.domain.views.*;
import org.springframework.stereotype.Component;

@Component
public class ViewFactory {

    public View buildView(Data data) {
        if (data.getView().getImage() != null) {
            return buildImageView(data);
        } else if (data.getView().getList() != null) {
            return buildListView(data);
        } else if (data.getView().getEditor() != null) {
            return new EditorView();
        } else if (data.getView().getHtmlEditor() != null) {
            return new HtmlEditorView();
        } else if (data.getView().getArea() != null) {
            return new View("area");
        } else if (data.getView().getAutocomplete() != null) {
            return new View("autocomplete");
        } else if (data.getView().getTree() != null) {
            return new TreeView();
        } else if (data.getView().getTable() != null) {
            return new TableView();
        } else if (data.getView().getButtonType() != null) {
            return new View(data.getView().getButtonType());
        } else {
            throw new UnsupportedViewException();
        }
    }

    public View buildListView(Data data) {
        if (data.getView().getList().length() > 0) {
            return new ListView(Integer.parseInt(data.getView().getList()));
        }
        return new ListView();
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
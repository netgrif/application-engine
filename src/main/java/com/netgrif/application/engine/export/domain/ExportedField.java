package com.netgrif.application.engine.export.domain;

import com.netgrif.application.engine.petrinet.domain.Imported;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


@Data
public class ExportedField extends Imported {

    public static final ExportedField STRING_ID = new ExportedField("meta-stringId", "ID Prípadu", true);
    public static final ExportedField AUTHOR = new ExportedField("meta-author", "Autor", true);
    public static final ExportedField CREATION_DATE = new ExportedField("meta-creationDate", "Dátum vytvorenia", true);
    public static final ExportedField TITLE = new ExportedField("meta-title", "Názov", true);
    public static final ExportedField VISUAL_ID = new ExportedField("meta-visualId", "Vizuálne ID", true);

    private String name;
    private boolean meta;

    public ExportedField(String id, String name) {
        this(id, name, false);
    }

    public ExportedField(String id, String name, boolean meta) {
        super();
        setImportId(id);
        this.name = name;
        this.meta = meta;
    }

    public String getId() {
        return getImportId();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExportedField that = (ExportedField) o;
        return Objects.equals(getId(), that.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    public static List<ExportedField> convert(List<String> fieldIds, List<String> fieldNames) {
        if (fieldIds == null || fieldNames == null) return new ArrayList<>();
        if (fieldIds.size() != fieldNames.size())
            throw new IllegalArgumentException("Provided fields IDs does not match to every fields name");
        List<ExportedField> list = new ArrayList<>(fieldIds.size());
        for (int i = 0; i < fieldIds.size(); i++) {
            list.add(new ExportedField(fieldIds.get(i), fieldNames.get(i)));
        }
        return list;
    }

}

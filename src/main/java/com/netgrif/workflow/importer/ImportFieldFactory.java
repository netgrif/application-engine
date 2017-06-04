package com.netgrif.workflow.importer;

import com.netgrif.workflow.importer.model.ImportData;
import com.netgrif.workflow.petrinet.domain.dataset.*;

import java.util.Arrays;

public final class ImportFieldFactory {
    private Importer importer;

    public ImportFieldFactory(Importer importer) {
        this.importer = importer;
    }

    public Field getField(ImportData data) throws IllegalArgumentException {
        switch (FieldType.fromString(data.getType())) {
            case TEXT:
                return new TextField(data.getValues());
            case BOOLEAN:
                return new BooleanField();
            case DATE:
                return new DateField();
            case FILE:
                return new FileField();
            case ENUMERATION:
                return new EnumerationField(data.getValues());
            case MULTICHOICE:
                return new MultichoiceField(data.getValues());
            case NUMBER:
                return new NumberField();
            case USER:
                return buildUserField(data);
            case TABULAR:
                return buildTabularField(data);
            default:
                throw new IllegalArgumentException(data.getType() + " is not a valid Field type");
        }
    }

    private TabularField buildTabularField(ImportData data) {
        TabularField field = new TabularField();
        Arrays.stream(data.getColumns().getData()).forEach(dataField -> {
            field.addField(getField(dataField));
        });
        return field;
    }

    private UserField buildUserField(ImportData data) {
        String[] roles = Arrays.stream(data.getValues())
                .map(value -> importer.getRoles().get(Long.parseLong(value)).getObjectId())
                .toArray(String[]::new);
        return new UserField(roles);
    }
}

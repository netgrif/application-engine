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
        Field field;
        FieldType type = FieldType.fromString(data.getType());
        switch (type) {
            case TEXT:
                field = new TextField(data.getValues());
                break;
            case BOOLEAN:
                field = new BooleanField();
                break;
            case DATE:
                field = new DateField();
                break;
            case FILE:
                field = new FileField();
                break;
            case ENUMERATION:
                field = new EnumerationField(data.getValues());
                break;
            case MULTICHOICE:
                field = new MultichoiceField(data.getValues());
                break;
            case NUMBER:
                field = new NumberField();
                break;
            case USER:
                field = buildUserField(data);
                break;
            case TABULAR:
                field = buildTabularField(data);
                break;
            default:
                throw new IllegalArgumentException(data.getType() + " is not a valid Field type");
        }
        field.setName(data.getTitle());
        field.setType(type);

        return field;
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

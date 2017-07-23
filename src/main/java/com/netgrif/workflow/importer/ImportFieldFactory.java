package com.netgrif.workflow.importer;

import com.netgrif.workflow.importer.model.ImportData;
import com.netgrif.workflow.petrinet.domain.dataset.*;

import java.util.*;

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
            case CASEREF:
                field = buildCaseField(data);
                break;
            default:
                throw new IllegalArgumentException(data.getType() + " is not a valid Field type");
        }
        field.setName(data.getTitle());
        field.setImportId(data.getId());
        field.setType(type);
        field.setImmediate(data.isImmediate());
        if(data.getDesc() != null)
            field.setDescription(data.getDesc());
        if(data.getPlaceholder() != null)
            field.setPlaceholder(data.getPlaceholder());
        if(data.getValid() != null && field instanceof ValidableField)
            ((ValidableField)field).setValidationRules(data.getValid());
        if(data.getInit() != null && field instanceof FieldWithDefault)
            ((FieldWithDefault)field).setDefaultValue(data.getInit());
        if(data.getAction() != null && data.getAction().length != 0){
            Arrays.stream(data.getAction()).forEach(action -> field.addAction(action.getDefinition(),action.getTrigger()));
        }
        return field;
    }

    private CaseField buildCaseField(ImportData data) {
        Map<Long, LinkedHashSet<Long>> netIds = new HashMap<>();
        Arrays.stream(data.getDocumentRefs())
                .forEach(documentRef -> netIds.put(documentRef.getId(), new LinkedHashSet<>(Arrays.asList(documentRef.getFields()))));
        return new CaseField(netIds);
    }

    private TabularField buildTabularField(ImportData data) {
        TabularField field = new TabularField();
        Arrays.stream(data.getColumns().getData()).forEach(dataField -> field.addField(getField(dataField)));
        return field;
    }

    private UserField buildUserField(ImportData data) {
        String[] roles = Arrays.stream(data.getValues())
                .map(value -> importer.getRoles().get(Long.parseLong(value)).getObjectId())
                .toArray(String[]::new);
        return new UserField(roles);
    }
}
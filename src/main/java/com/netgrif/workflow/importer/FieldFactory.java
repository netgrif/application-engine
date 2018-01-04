package com.netgrif.workflow.importer;

import com.netgrif.workflow.importer.model.Data;
import com.netgrif.workflow.importer.model.DocumentRef;
import com.netgrif.workflow.petrinet.domain.PetriNet;
import com.netgrif.workflow.petrinet.domain.dataset.*;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;

@Component
public final class FieldFactory {

    public Field getField(Data data, Importer importer) throws IllegalArgumentException {
        Field field;
        switch (data.getType()) {
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
                field = buildUserField(data, importer);
                break;
            case CASEREF:
                field = buildCaseField(data, importer);
                break;
            default:
                throw new IllegalArgumentException(data.getType() + " is not a valid Field type");
        }
        field.setName(data.getTitle());
        field.setImportId(data.getId());
        field.setImmediate(data.isImmediate());
        if (data.getDesc() != null)
            field.setDescription(data.getDesc());
        if (data.getPlaceholder() != null)
            field.setPlaceholder(data.getPlaceholder());
        if (data.getValid() != null && field instanceof ValidableField)
            ((ValidableField) field).setValidationRules(data.getValid());
        if (data.getInit() != null && field instanceof FieldWithDefault)
            ((FieldWithDefault) field).setDefaultValue(data.getInit());
        setActions(field, data);
        setEncryption(field, data);

        return field;
    }

    private void setActions(Field field, Data data) {
        if (data.getAction() != null && data.getAction().size() != 0) {
            data.getAction().forEach(action -> field.addAction(action.getValue(), action.getTrigger()));
        }
    }

    private void setEncryption(Field field, Data data) {
        if (data.getEncryption() != null && data.getEncryption().isValue()) {
            String encryption = data.getEncryption().getAlgorithm();
            if (encryption == null)
                encryption = "PBEWITHSHA256AND256BITAES-CBC-BC";
            field.setEncryption(encryption);
        }
    }

    private CaseField buildCaseField(Data data, Importer importer) {
        Map<String, LinkedHashSet<String>> netIds = new HashMap<>();
        DocumentRef documentRef = data.getDocumentRef();

        PetriNet net = importer.getNetByImportId(documentRef.getId());
        LinkedHashSet<String> fieldIds = new LinkedHashSet<>();

        net.getDataSet().values().forEach(field -> {
            if (documentRef.getFields().contains(field.getImportId())) {
                fieldIds.add(field.getStringId());
            }
        });

        netIds.put(net.getStringId(), fieldIds);
        return new CaseField(netIds);
    }

    private UserField buildUserField(Data data, Importer importer) {
        String[] roles = data.getValues().stream()
                .map(value -> importer.getRoles().get(Long.parseLong(value)).getStringId())
                .toArray(String[]::new);
        return new UserField(roles);
    }
}
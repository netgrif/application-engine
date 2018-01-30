package com.netgrif.workflow.importer;

import com.netgrif.workflow.importer.model.Data;
import com.netgrif.workflow.importer.model.DocumentRef;
import com.netgrif.workflow.importer.model.I18NStringType;
import com.netgrif.workflow.petrinet.domain.PetriNet;
import com.netgrif.workflow.petrinet.domain.dataset.*;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

@Component
public final class FieldFactory {

    public Field getField(Data data, Importer importer) throws IllegalArgumentException {
        Field field;
        switch (data.getType()) {
            case TEXT:
                field = buildTextField(data.getValues());
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
                field = buildEnumerationField(data.getValues(), importer);
                break;
            case MULTICHOICE:
                field = buildMultichoiceField(data.getValues(), importer);
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
        field.setName(importer.toI18NString(data.getTitle()));
        field.setImportId(data.getId());
        field.setImmediate(data.isImmediate());
        if (data.getDesc() != null)
            field.setDescription(importer.toI18NString(data.getDesc()));
        if (data.getPlaceholder() != null)
            field.setPlaceholder(importer.toI18NString(data.getPlaceholder()));
        if (data.getValid() != null && field instanceof ValidableField)
            ((ValidableField) field).setValidationRules(data.getValid());
        if (data.getInit() != null && field instanceof FieldWithDefault)
            ((FieldWithDefault) field).setDefaultValue(data.getInit());
        setActions(field, data);
        setEncryption(field, data);

        return field;
    }

    private MultichoiceField buildMultichoiceField(List<I18NStringType> values, Importer importer) {
        // TODO: 1/6/18
        return new MultichoiceField();
    }

    private EnumerationField buildEnumerationField(List<I18NStringType> values, Importer importer) {
        // TODO: 1/6/18
        return new EnumerationField();
    }

    private TextField buildTextField(List<I18NStringType> values) {
        String value = null;
        if (values != null && !values.isEmpty())
            value = values.get(0).getValue();

        return new TextField(value);
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
                .map(value -> importer.getRoles().get(Long.parseLong(value.getValue())).getStringId())
                .toArray(String[]::new);
        return new UserField(roles);
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
}
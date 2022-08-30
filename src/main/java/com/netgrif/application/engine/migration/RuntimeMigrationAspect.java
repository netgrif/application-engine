package com.netgrif.application.engine.migration;

import com.netgrif.application.engine.petrinet.domain.PetriNet;
import com.netgrif.application.engine.petrinet.domain.dataset.FieldType;
import com.netgrif.application.engine.petrinet.domain.dataset.UserFieldValue;
import com.netgrif.application.engine.petrinet.domain.dataset.UserListFieldValue;
import org.aspectj.lang.annotation.Aspect;
import org.bson.Document;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Order
@Aspect
@Component
public class RuntimeMigrationAspect extends AbstractRuntimeMigrationAspect {

    @Override
    public void callProcessMigrations(Document bson) {

    }

    @Override
    public void callCaseMigrations(Document bson) {
        migrateUserListFieldValueType(bson);
    }

    @Override
    public void callTaskMigrations(Document bson) {

    }

    protected final void migrateUserListFieldValueType(Document document) {
        PetriNet net = retrievePetriNetOfCase(document);
        if (net == null) {
            return;
        }
        List<String> userListFields = net.getDataSet().entrySet()
                .stream()
                .filter(entry -> entry.getValue().getType().equals(FieldType.USERLIST))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        if(document.get("dataSet") != null) {
            userListFields.forEach(field -> {
                Document userListField = document.get("dataSet", Document.class).get(field, Document.class);
                if (userListField.get("value") != null && userListField.get("value") instanceof ArrayList) {
                    List<UserFieldValue> userFieldValues = new ArrayList<>();
                    ((ArrayList<String>) userListField.get("value")).forEach(v -> {
                        userFieldValues.add(getDataService().makeUserFieldValue(v));
                    });
                    UserListFieldValue userListFieldValue = new UserListFieldValue(userFieldValues);
                    userListField.remove("value");
                    userListField.append("value", userListFieldValue);
                }
            });
        }
    }

    protected PetriNet retrievePetriNetOfCase(Document aCase) {
        if (aCase.get("processIdentifier") == null) {
            return null;
        }
        return getPetriNetService().getNewestVersionByIdentifier(aCase.get("processIdentifier", String.class));
    }
}

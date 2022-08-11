package com.netgrif.application.engine.migration;

import com.netgrif.application.engine.auth.domain.IUser;
import com.netgrif.application.engine.auth.service.interfaces.IUserService;
import com.netgrif.application.engine.migration.repository.IRuntimeMigrationServiceAdvice;
import com.netgrif.application.engine.petrinet.domain.PetriNet;
import com.netgrif.application.engine.petrinet.domain.dataset.FieldType;
import com.netgrif.application.engine.petrinet.domain.dataset.UserFieldValue;
import com.netgrif.application.engine.petrinet.domain.dataset.UserListFieldValue;
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService;
import com.netgrif.application.engine.workflow.domain.Case;
import com.netgrif.application.engine.workflow.domain.Task;
import com.netgrif.application.engine.workflow.service.interfaces.IDataService;
import com.netgrif.application.engine.workflow.service.interfaces.ITaskService;
import com.netgrif.application.engine.workflow.service.interfaces.IWorkflowService;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.swing.event.DocumentEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Aspect service to manage migration for processes, cases and tasks. This
 * class provides functions to migrate collection data.
 * */
@Slf4j
@Aspect
@Service
public class RuntimeMigrationServiceAspect implements IRuntimeMigrationServiceAdvice {

    @Autowired
    private IPetriNetService petriNetService;

    @Autowired
    private IWorkflowService workflowService;

    @Autowired
    private ITaskService taskService;

    @Autowired
    private IUserService userService;

    @Autowired
    private IDataService dataService;

    @Pointcut(value = "execution(* org.springframework.data.mongodb.core.convert.MappingMongoConverter.read(..)) && args(clazz, bson)", argNames = "clazz,bson")
    public void checkData(Class<?> clazz, Document bson) {}

    @Before(value = "checkData(clazz,bson)", argNames = "clazz,bson")
    public Document checkDocument(Class<?> clazz, Document bson) {
        log.info("Hello world!");

        if (PetriNet.class.equals(clazz)) {

        } else if (Case.class.equals(clazz)) {
            migrateUserListFieldValueType(bson);
        } else if (Task.class.equals(clazz)) {

        }
        return bson;
    }

    protected void migrateUserListFieldValueType(Document document) {
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
            // iterate through userListFields
            userListFields.forEach(field -> {
                //retrieve field document from document
                Document userListField = document.get("dataSet", Document.class).get(field, Document.class);

                //check if value is type of string or userFieldValue
                if (userListField.get("value") != null && userListField.get("value") instanceof ArrayList) {
                    List<UserFieldValue> userFieldValues = new ArrayList<>();
                    ((ArrayList<String>) userListField.get("value")).forEach(v -> {
                       userFieldValues.add(dataService.makeUserFieldValue(v));
                    });
                    UserListFieldValue userListFieldValue = new UserListFieldValue(userFieldValues);

                    //modify value for field from string to list of userFieldValue
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
        return petriNetService.getNewestVersionByIdentifier(aCase.get("processIdentifier", String.class));
    }

}

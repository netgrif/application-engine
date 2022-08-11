package com.netgrif.application.engine.migration;

import com.netgrif.application.engine.auth.service.interfaces.IUserService;
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
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Aspect service to manage migration for processes, cases and tasks. This
 * class provides functions to migrate collection data. This class can be overriden
 * to add additional migration functions.
 * */
@Slf4j
@Order
@Aspect
@Component
public class RuntimeMigrationAspect {

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

    protected IPetriNetService getPetriNetService() {
        return petriNetService;
    }

    protected IWorkflowService getWorkflowService() {
        return workflowService;
    }

    protected ITaskService getTaskService() {
        return taskService;
    }

    protected IUserService getUserService() {
        return userService;
    }

    protected IDataService getDataService() {
        return dataService;
    }

    /**
     * Pointcut expression to make join point to @link{MappingMongoConverter}
     * */
    @Pointcut(value = "execution(* org.springframework.data.mongodb.core.convert.MappingMongoConverter.read(..)) && args(clazz, bson)", argNames = "clazz,bson")
    protected final void checkData(Class<?> clazz, Document bson) {}

    @Before(value = "checkData(clazz,bson)", argNames = "clazz,bson")
    protected final Document checkDocument(Class<?> clazz, Document bson) {
        if (PetriNet.class.equals(clazz)) {
            callProcessMigrations(bson);
        } else if (Case.class.equals(clazz)) {
            callCaseMigrations(bson);
        } else if (Task.class.equals(clazz)) {
            callTaskMigrations(bson);
        }
        return bson;
    }

    /**
     * Function for call the migration functions for processes, this can be overriden and provide
     * custom order and functions.
     * @param bson document to be migrated
     * */
    public void callProcessMigrations(Document bson) {

    }

    /**
     * Function for call the migration functions for cases, this can be overriden and provide
     * custom order and functions.
     * @param bson document to be migrated
     * */
    public void callCaseMigrations(Document bson) {
        migrateUserListFieldValueType(bson);
    }

    /**
     * Function for call the migration functions for tasks, this can be overriden and provide
     * custom order and functions.
     * @param bson document to be migrated
     * */
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
                       userFieldValues.add(dataService.makeUserFieldValue(v));
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
        return petriNetService.getNewestVersionByIdentifier(aCase.get("processIdentifier", String.class));
    }
}

package com.netgrif.workflow.history.domain;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.Map;

@Document
public class UserTaskEventLog extends UserEventLog {

    @Getter @Setter
    private String taskId;

    @Getter @Setter
    private String taskName;

    @Getter @Setter
    private String caseId;

    @Getter @Setter
    private String caseName;

    @Getter @Setter
    private String transitionId;

    @Field("activePlaces")
    @Getter @Setter
    private Map<String, Integer> activePlaces;

    @Field("dataSetValues")
    @Getter @Setter
    private Map<String, Object> dataSetValues;

}

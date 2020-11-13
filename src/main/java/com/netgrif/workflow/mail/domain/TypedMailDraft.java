package com.netgrif.workflow.mail.domain;

import com.netgrif.workflow.mail.EmailType;
import lombok.Builder;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Builder(builderMethodName = "typedMailDraftBuilder")
public class TypedMailDraft {
    List<String> recipients;

    EmailType type;

    Map<String, Object> model;

    Map<String, File> attachments;

    public static TypedMailDraftBuilder builder(List<String> recipients){
        return typedMailDraftBuilder().recipients(recipients).model(new HashMap<>()).attachments(new HashMap<>());
    }

}

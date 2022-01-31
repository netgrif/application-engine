package com.netgrif.workflow.mail.domain;

import com.netgrif.workflow.mail.EmailType;
import lombok.Builder;
import lombok.Data;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Builder(builderMethodName = "typedMailDraftBuilder")
@Data
public class TypedMailDraft {
    private List<String> recipients;

    private EmailType type;

    private Map<String, Object> model;

    private Map<String, File> attachments;

    public static TypedMailDraftBuilder builder(List<String> recipients){
        return typedMailDraftBuilder().recipients(recipients).model(new HashMap<>()).attachments(new HashMap<>());
    }

}

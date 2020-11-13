package com.netgrif.workflow.mail.domain;

import lombok.Builder;
import lombok.Data;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Builder(builderMethodName = "simpleMailDraftBuilder")
@Data
public class SimpleMailDraft {
    List<String> recipients;

    String subject;

    String body;

    boolean isHtml;

    Map<String, File> attachments;

    public static SimpleMailDraftBuilder builder(List<String> recipients){
        return simpleMailDraftBuilder().recipients(recipients).subject("").body("").isHtml(false).attachments(new HashMap<>());
    }

}

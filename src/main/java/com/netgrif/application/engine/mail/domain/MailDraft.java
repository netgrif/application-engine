package com.netgrif.application.engine.mail.domain;

import lombok.Builder;
import lombok.Data;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Builder(builderMethodName = "mailDraftBuilder")
@Data
public class MailDraft {

    String from;

    @Builder.Default
    List<String> to = new ArrayList<>();

    @Builder.Default
    List<String> cc = new ArrayList<>();

    @Builder.Default
    List<String> bcc = new ArrayList<>();

    String subject;

    String body;

    boolean isHtml;

    @Builder.Default
    Map<String, Object> model = new HashMap<>();

    @Builder.Default
    Map<String, File> attachments = new HashMap<>();

    public static MailDraftBuilder builder(String from, List<String> to) {
        return mailDraftBuilder().from(from).to(to).subject("").body("").isHtml(false).attachments(new HashMap<>());
    }

    public static class MailDraftBuilder {
    }

}

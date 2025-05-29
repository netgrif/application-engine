package com.netgrif.application.engine.workflow.domain.params;

import com.netgrif.application.engine.petrinet.domain.Process;
import com.netgrif.application.engine.workflow.domain.Case;
import lombok.Builder;
import lombok.Data;
import org.springframework.context.i18n.LocaleContextHolder;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;


@Data
@Builder(builderMethodName = "with")
public class CreateCaseParams {

    private String processId;
    private String processIdentifier;
    private Process process;
    private String title;
    private Function<Case, String> makeTitle;
    private String authorId;
    @Builder.Default
    private Locale locale = LocaleContextHolder.getLocale();
    private Boolean isTransactional;
    @Builder.Default
    private Map<String, String> params = new HashMap<>();

    public static class CreateCaseParamsBuilder {
        /**
         * Sets the {@link #title} and {@link #makeTitle} as well
         * */
        public CreateCaseParamsBuilder title(String title) {
            this.title = title;
            if (title != null) {
                this.makeTitle = (u) -> title;
            } else {
                this.makeTitle = null;
            }
            return this;
        }

        /**
         * Sets the {@link #process} as clone, {@link #processIdentifier} and {@link #processId}
         * */
        public CreateCaseParamsBuilder process(Process process) {
            this.process = process.clone();
            this.processIdentifier = process.getIdentifier();
            this.processId = process.getStringId();
            return this;
        }
    }
}

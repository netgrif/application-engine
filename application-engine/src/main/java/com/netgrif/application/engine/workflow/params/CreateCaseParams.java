package com.netgrif.application.engine.workflow.params;

import com.netgrif.application.engine.objects.auth.domain.LoggedUser;
import com.netgrif.application.engine.objects.petrinet.domain.PetriNet;
import com.netgrif.application.engine.objects.workflow.domain.Case;
import com.netgrif.application.engine.workflow.service.WorkflowService;
import lombok.Builder;
import lombok.Data;
import org.springframework.context.i18n.LocaleContextHolder;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;

/**
 * A parameter class for the {@link WorkflowService#createCase(CreateCaseParams)} method
 */
@Data
@Builder(builderMethodName = "with")
public class CreateCaseParams {

    /// String id of the process to be used for creation
    private String processId;

    /// Identifier of the process to be used for creation
    private String processIdentifier;

    /// Process object to be used for creation
    private PetriNet process;

    /// Title of the new Case
    private String title;

    /// Function for the title initialization
    private Function<Case, String> makeTitle;

    /// Color of the new Case
    private String color;

    /// Author of the Case
    private LoggedUser author;

    @Builder.Default
    private Locale locale = LocaleContextHolder.getLocale();

    @Builder.Default
    private Map<String, String> params = new HashMap<>();

    public static class CreateCaseParamsBuilder {
        /// Sets the {@link #title} and {@link #makeTitle} as well
        public CreateCaseParams.CreateCaseParamsBuilder title(String title) {
            this.title = title;
            if (title != null) {
                this.makeTitle = (u) -> title;
            } else {
                this.makeTitle = null;
            }
            return this;
        }

        /// Sets the {@link #process} as clone, {@link #processIdentifier} and {@link #processId}
        public CreateCaseParams.CreateCaseParamsBuilder process(PetriNet petriNet) {
            this.process = new com.netgrif.application.engine.adapter.spring.petrinet.domain.PetriNet((com.netgrif.application.engine.adapter.spring.petrinet.domain.PetriNet) petriNet);
            this.processIdentifier = petriNet.getIdentifier();
            this.processId = petriNet.getStringId();
            return this;
        }
    }
}

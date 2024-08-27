package com.netgrif.application.engine.workflow.domain.params;

import com.netgrif.application.engine.auth.domain.LoggedUser;
import com.netgrif.application.engine.petrinet.domain.PetriNet;
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

    private String petriNetId;
    private String petriNetIdentifier;
    private PetriNet petriNet;
    private String title;
    private Function<Case, String> makeTitle;
    private String color;
    private LoggedUser loggedUser;
    @Builder.Default
    private Locale locale = LocaleContextHolder.getLocale();
    @Builder.Default
    private boolean isTransactional = false;
    @Builder.Default
    private Map<String, String> params = new HashMap<>();

    public static class CreateCaseParamsBuilder {
        /**
         * Sets the {@link #title} and {@link #makeTitle} as well
         * */
        public CreateCaseParams.CreateCaseParamsBuilder title(String title) {
            this.title = title;
            if (title != null) {
                this.makeTitle = (u) -> title;
            } else {
                this.makeTitle = null;
            }
            return this;
        }

        /**
         * Sets the {@link #petriNet} as clone, {@link #petriNetIdentifier} and {@link #petriNetId}
         * */
        public CreateCaseParams.CreateCaseParamsBuilder petriNet(PetriNet petriNet) {
            this.petriNet = petriNet.clone();
            this.petriNetIdentifier = petriNet.getIdentifier();
            this.petriNetId = petriNet.getStringId();
            return this;
        }
    }
}

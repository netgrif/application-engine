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

/**
 * todo javadoc
 * */
@Data
@Builder
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
    private Map<String, String> params = new HashMap<>();

    /**
     * todo javadoc
     * Builder extension of the {@link Builder} implementation for {@link }. Containing additional logic over the native builder
     * implementation
     * */
    public static class CreateCaseParamsBuilder {
        /**
         * todo javadoc
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
         * todo javadoc
         * */
        public CreateCaseParams.CreateCaseParamsBuilder petriNet(PetriNet petriNet) {
            this.petriNet = petriNet.clone();
            this.petriNetIdentifier = petriNet.getIdentifier();
            this.petriNetId = petriNet.getStringId();
            return this;
        }
    }
}

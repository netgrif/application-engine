package com.netgrif.application.engine.serialization;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

/**
 * Request-scoped holder component for managing {@link FieldSelector} instances during HTTP request processing.
 * <p>
 * This component is used in conjunction with {@link FieldSelectorInterceptor} to parse and store field selection
 * criteria from HTTP request parameters (typically the "fields" query parameter). The held {@link FieldSelector}
 * is then used by custom serializers like {@link LocalizedEventOutcomeSerializer} to control which fields
 * are included in JSON response bodies.
 * </p>
 * <p>
 * Being request-scoped ensures that each HTTP request has its own isolated instance of this holder,
 * preventing thread-safety issues in concurrent request processing.
 * </p>
 *
 * @see FieldSelector
 * @see FieldSelectorInterceptor
 * @see LocalizedEventOutcomeSerializer
 */
@Setter
@Getter
@Component
@RequestScope
public class FieldSelectorHolder {

    /**
     * The field selector that determines which fields should be included during serialization.
     * Initialized with a default selector that includes all fields (parsed from null).
     */
    private FieldSelector selector = FieldSelector.parse(null);
}

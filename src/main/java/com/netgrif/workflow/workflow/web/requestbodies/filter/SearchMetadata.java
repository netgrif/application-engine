package com.netgrif.workflow.workflow.web.requestbodies.filter;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Data required for the reconstruction of the advanced search GUI.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SearchMetadata {
    /**
     * Identifiers of the Petri Nets that are used to populate the autocomplete search categories.
     */
    @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
    private List<String> allowedNets;
    /**
     * Data required for the reconstruction of the advanced search GUI predicates.
     */
    private List<List<Map<String, Object>>> generatorMetadata;
}

package com.netgrif.application.engine.petrinet.domain.params;

import com.netgrif.application.engine.auth.domain.LoggedUser;
import com.netgrif.application.engine.petrinet.domain.VersionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

@Data
@AllArgsConstructor
@Builder(builderMethodName = "with")
public class ImportPetriNetParams {

    private InputStream xmlFile;
    private String uriNodeId;
    private VersionType releaseType;
    private LoggedUser author;
    @Builder.Default
    private boolean isTransactional = false;
    @Builder.Default
    private Map<String, String> params = new HashMap<>();

    public ImportPetriNetParams(InputStream xmlFile, VersionType releaseType, LoggedUser author, String uriNodeId) {
        this.xmlFile = xmlFile;
        this.releaseType = releaseType;
        this.author = author;
        this.uriNodeId = uriNodeId;
    }

    public ImportPetriNetParams(InputStream xmlFile, VersionType releaseType, LoggedUser author) {
        this.xmlFile = xmlFile;
        this.releaseType = releaseType;
        this.author = author;
    }
}

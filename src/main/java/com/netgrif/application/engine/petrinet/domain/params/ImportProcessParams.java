package com.netgrif.application.engine.petrinet.domain.params;

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
public class ImportProcessParams {

    private InputStream xmlFile;
    private String uriNodeId;
    private VersionType releaseType;
    private String authorId;
    @Builder.Default
    private boolean isTransactional = false;
    @Builder.Default
    private Map<String, String> params = new HashMap<>();

    public ImportProcessParams(InputStream xmlFile, VersionType releaseType, String authorId, String uriNodeId) {
        this.xmlFile = xmlFile;
        this.releaseType = releaseType;
        this.authorId = authorId;
        this.uriNodeId = uriNodeId;
    }

    public ImportProcessParams(InputStream xmlFile, VersionType releaseType, String authorId) {
        this.xmlFile = xmlFile;
        this.releaseType = releaseType;
        this.authorId = authorId;
    }
}

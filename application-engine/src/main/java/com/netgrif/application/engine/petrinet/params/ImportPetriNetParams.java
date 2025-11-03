package com.netgrif.application.engine.petrinet.params;

import com.netgrif.application.engine.objects.auth.domain.AbstractUser;
import com.netgrif.application.engine.objects.auth.domain.LoggedUser;
import com.netgrif.application.engine.objects.petrinet.domain.VersionType;
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

    // todo javadoc

    private InputStream xmlFile;
    private String uriNodeId;
    private VersionType releaseType;
    private AbstractUser author;
    @Builder.Default
    private Map<String, String> params = new HashMap<>();

    public ImportPetriNetParams(InputStream xmlFile, VersionType releaseType, AbstractUser author, String uriNodeId) {
        this.xmlFile = xmlFile;
        this.releaseType = releaseType;
        this.author = author;
        this.uriNodeId = uriNodeId;
    }

    public ImportPetriNetParams(InputStream xmlFile, VersionType releaseType, AbstractUser author) {
        this(xmlFile, releaseType, author, null);
    }
}

package com.netgrif.application.engine.petrinet.params;

import com.netgrif.application.engine.objects.auth.domain.AbstractUser;
import com.netgrif.application.engine.objects.petrinet.domain.VersionType;
import com.netgrif.application.engine.petrinet.service.PetriNetService;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * A parameter class for the {@link PetriNetService#importPetriNet(ImportPetriNetParams)} method.
 */
@Data
@AllArgsConstructor
@Builder(builderMethodName = "with")
public class ImportPetriNetParams {

    /// Input stream of the process XML file.
    private InputStream xmlFile;

    /// uri node id for the process
    private String uriNodeId;

    /**
     * Release type of the process
     * @see VersionType
     */
    private VersionType releaseType;

    /// Author of the process
    private AbstractUser author;

    /// Identifier of the workspace
    private String workspaceId;

    @Builder.Default
    private Map<String, String> params = new HashMap<>();

    public ImportPetriNetParams(InputStream xmlFile, VersionType releaseType, AbstractUser author, String uriNodeId) {
        this.xmlFile = xmlFile;
        this.releaseType = releaseType;
        this.author = author;
        this.uriNodeId = uriNodeId;
        this.params = new HashMap<>();
    }

    public ImportPetriNetParams(InputStream xmlFile, VersionType releaseType, AbstractUser author) {
        this(xmlFile, releaseType, author, null);
    }
}

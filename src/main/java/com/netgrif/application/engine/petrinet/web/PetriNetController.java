package com.netgrif.application.engine.petrinet.web;

import com.netgrif.application.engine.auth.service.interfaces.IUserService;
import com.netgrif.application.engine.petrinet.domain.version.StringToVersionConverter;
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService;
import com.netgrif.application.engine.petrinet.service.interfaces.IProcessRoleService;
import com.netgrif.application.engine.petrinet.web.interfaces.IPetriNetController;
import com.netgrif.application.engine.workflow.domain.FileStorageConfiguration;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/petrinet")
@ConditionalOnProperty(
        value = "nae.petrinet.web.enabled",
        havingValue = "true",
        matchIfMissing = true
)
@Tag(name = "PetriNet")
public class PetriNetController implements IPetriNetController {

    private final IPetriNetService petriNetService;

    private final FileStorageConfiguration fileStorageConfiguration;

    private final IProcessRoleService processRoleService;

    private final StringToVersionConverter converter;

    private final IUserService userService;

    public PetriNetController(@Autowired IPetriNetService petriNetService,
                              @Autowired FileStorageConfiguration fileStorageConfiguration,
                              @Autowired IProcessRoleService processRoleService,
                              @Autowired StringToVersionConverter converter,
                              @Autowired IUserService userService) {
        this.petriNetService = petriNetService;
        this.fileStorageConfiguration = fileStorageConfiguration;
        this.processRoleService = processRoleService;
        this.converter = converter;
        this.userService = userService;
    }

    @Override
    public IPetriNetService service() {
        return petriNetService;
    }

    @Override
    public FileStorageConfiguration fileStorageConfiguration() {
        return fileStorageConfiguration;
    }

    @Override
    public IProcessRoleService roleService() {
        return processRoleService;
    }

    @Override
    public StringToVersionConverter converter() {
        return converter;
    }

    @Override
    public IUserService userService() {
        return userService;
    }

    @Override
    public Logger log() {
        return log;
    }
}

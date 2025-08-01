package com.netgrif.application.engine.startup.runner;

import com.netgrif.application.engine.objects.petrinet.domain.PetriNet;
import com.netgrif.application.engine.objects.workflow.domain.menu.MenuItemView;
import com.netgrif.application.engine.startup.ApplicationEngineStartupRunner;
import com.netgrif.application.engine.startup.ImportHelper;
import com.netgrif.application.engine.startup.annotation.RunnerOrder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Component
@RunnerOrder(101)
@RequiredArgsConstructor
public class MenuProcessRunner implements ApplicationEngineStartupRunner {

    private final ImportHelper helper;

    private static final String MENU_ITEM_FILE_NAME = "engine-processes/menu/menu_item.xml";
    public static final String MENU_NET_IDENTIFIER = "menu_item";

    @Override
    public void run(ApplicationArguments args) throws Exception {
        helper.importProcessOnce("Petri net for filter preferences", MENU_NET_IDENTIFIER, MENU_ITEM_FILE_NAME);
        createConfigurationNets();
    }

    private List<PetriNet> createConfigurationNets() {
        return Arrays.stream(MenuItemView.values())
                .map(view -> {
                    String processIdentifier = view.getIdentifier() + "_configuration";
                    String filePath = String.format("engine-processes/menu/%s.xml", processIdentifier);
                    return helper.importProcessOnce(
                            String.format("Petri net for %s", processIdentifier),
                            processIdentifier,
                            filePath
                    );
                })
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

}

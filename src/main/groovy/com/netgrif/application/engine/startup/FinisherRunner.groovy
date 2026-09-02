package com.netgrif.application.engine.startup

import com.netgrif.application.engine.menu.service.MenuItemService
import com.netgrif.application.engine.menu.service.MenuItemTemplateHolder
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class FinisherRunner extends AbstractOrderedCommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(FinisherRunner)

    @Autowired
    private MenuItemService menuItemService

    @Override
    void run(String... strings) throws Exception {
//        menuItemService.createOrIgnoreMenuItem(MenuItemTemplateHolder.get("tabbed_case_view", "/", "tabbed_case_view").get())
//        menuItemService.createOrIgnoreMenuItem(MenuItemTemplateHolder.get("tabbed_task_view", "/", "tabbed_task_view").get())
//        menuItemService.createOrIgnoreMenuItem(MenuItemTemplateHolder.get("tabbed_ticket_view", "/", "tabbed_ticket_view").get())
//        menuItemService.createOrIgnoreMenuItem(MenuItemTemplateHolder.get("simple_case_view", "/", "simple_case_view").get())
//        menuItemService.createOrIgnoreMenuItem(MenuItemTemplateHolder.get("simple_task_view", "/", "simple_task_view").get())
//        menuItemService.createOrIgnoreMenuItem(MenuItemTemplateHolder.get("single_task_view", "/", "single_task_view").get())
        log.info("+----------------------------+")
        log.info("| Netgrif Application Engine |")
        log.info("+----------------------------+")
    }
}

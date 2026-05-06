package com.netgrif.application.engine.menu.web;

import com.netgrif.application.engine.menu.service.interfaces.IMenuItemService;
import com.netgrif.application.engine.menu.web.responsebodies.MenuItemDataResponse;
import com.netgrif.application.engine.petrinet.domain.dataset.Field;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.Base64;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@Tag(name = "Menu")
@RequiredArgsConstructor
@RequestMapping("/api/menu")
public class MenuController {

    private final IMenuItemService menuItemService;

    @Operation(summary = "Get relevant data for the menu item", security = {@SecurityRequirement(name = "BasicAuth")})
    @GetMapping(value = "/{encodedCaseId}", produces = MediaTypes.HAL_JSON_VALUE)
    public EntityModel<MenuItemDataResponse> getMenuItemData(String encodedCaseId) {
        try {
            String caseId = new String(Base64.getDecoder().decode(encodedCaseId));
            Map<String, List<Field<?>>> immediateDataMap = menuItemService.getMenuItemData(caseId);
            return EntityModel.of(new MenuItemDataResponse(immediateDataMap));
        } catch (Exception e) {
            log.error("Getting menu item data failed", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Getting menu item data failed", e);
        }
    }
}


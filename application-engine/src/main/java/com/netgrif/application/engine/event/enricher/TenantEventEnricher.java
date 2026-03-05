package com.netgrif.application.engine.event.enricher;

import com.netgrif.application.engine.adapter.spring.event.EventEnricher;
import com.netgrif.application.engine.auth.service.TenantService;
import com.netgrif.application.engine.auth.service.UserService;
import com.netgrif.application.engine.objects.auth.domain.AbstractUser;
import com.netgrif.application.engine.objects.auth.domain.ActorRef;
import com.netgrif.application.engine.objects.auth.domain.Attribute;
import com.netgrif.application.engine.objects.event.events.Event;
import com.netgrif.application.engine.objects.tenant.Tenant;

import java.util.Optional;

import static com.netgrif.application.engine.objects.tenant.TenantConstants.TENANT_EMPTY;
import static com.netgrif.application.engine.objects.tenant.TenantConstants.TENANT_ID;

public class TenantEventEnricher implements EventEnricher {

    private final UserService userService;
    private final TenantService tenantService;

    public TenantEventEnricher(UserService userService, TenantService tenantService) {
        this.userService = userService;
        this.tenantService = tenantService;
    }

    @Override
    public <T> T enrich(T event) {
        if (event instanceof Event e) {
            Attribute<?> tenantAtt;
            Optional<Tenant> tenant = getTenantFromEvent(e);
            if (tenant.isPresent()) {
                tenantAtt = Attribute.of(tenant.get().getId());
            } else {
                tenantAtt = getTenantIdFromLoggedUser();
            }
            e.addAttribute(TENANT_ID, tenantAtt);
        }
        return event;
    }

    private Optional<Tenant> getTenantFromEvent(Event event) {
        ActorRef actor = event.getActor();
        if (actor == null) {
            return Optional.empty();
        }
        return tenantService.getByRealm(actor.getRealmId());
    }

    private Attribute<?> getTenantIdFromLoggedUser() {
        AbstractUser loggedUser = userService.getLoggedOrSystem();
        if (loggedUser == null) {
            return Attribute.of(TENANT_EMPTY);
        }
        Attribute<?> tenantAtt = loggedUser.getAttribute(TENANT_ID);
        if (tenantAtt == null || tenantAtt.hasNullValue()) {
            return Attribute.of(TENANT_EMPTY);
        }
        return loggedUser.getAttribute(TENANT_ID);
    }
}

package com.netgrif.application.engine.auth.service;

import com.netgrif.application.engine.adapter.spring.auth.domain.User;
import com.netgrif.application.engine.adapter.spring.petrinet.service.ProcessRoleService;
import com.netgrif.application.engine.objects.auth.domain.AbstractActor;
import com.netgrif.application.engine.objects.auth.domain.Authority;
import com.netgrif.application.engine.objects.petrinet.domain.roles.ProcessRole;
import com.netgrif.application.engine.objects.workflow.domain.ProcessResourceId;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ActorMongoEventListenerTest {

    @Mock
    private ProcessRoleService processRoleService;

    @Mock
    private AuthorityService authorityService;

    private ActorMongoEventListener listener;

    static class TestAuthority extends Authority {
        private final ObjectId id = new ObjectId();

        public TestAuthority(String name) {
            super(name);
        }

        @Override
        public ObjectId get_id() {
            return id;
        }

        @Override
        public String getStringId() {
            return id.toString();
        }
    }

    static class TestProcessRole extends ProcessRole {
        public TestProcessRole(String id) {
            super(id);
        }
    }

    @BeforeEach
    void setUp() {
        listener = new ActorMongoEventListener(processRoleService, authorityService);
    }

    @Test
    void testOnAfterConvertWithEmptyRolesAndAuthorities() {
        User user = new User();
        Document document = new Document();

        AbstractActor result = listener.onAfterConvert(user, document, "actors");

        assertSame(user, result);
        assertTrue(result.getProcessRoles().isEmpty());
        assertTrue(result.getAuthoritySet().isEmpty());
        verifyNoInteractions(processRoleService);
        verifyNoInteractions(authorityService);
    }

    @Test
    void testOnAfterConvertPopulatesProcessRolesAndAuthorities() {
        User user = new User();
        String role1Id = new ProcessResourceId().getFullId();
        String role2Id = new ProcessResourceId().getFullId();
        user.setProcessRoleIds(Set.of(role1Id, role2Id));

        String auth1Id = new ObjectId().toString();
        String auth2Id = new ObjectId().toString();
        user.setAuthorityIds(Set.of(auth1Id, auth2Id));

        TestProcessRole role1 = new TestProcessRole(role1Id);
        when(processRoleService.findById(role1Id)).thenReturn(role1);
        when(processRoleService.findById(role2Id)).thenReturn(null);

        TestAuthority auth1 = new TestAuthority("USER");
        when(authorityService.getOne(auth1Id)).thenReturn(auth1);
        when(authorityService.getOne(auth2Id)).thenReturn(null);

        Document doc = new Document();
        AbstractActor result = listener.onAfterConvert(user, doc, "users");

        assertSame(user, result);
        assertEquals(1, result.getProcessRoles().size());
        assertTrue(result.getProcessRoles().contains(role1));

        assertEquals(1, result.getAuthoritySet().size());
        assertTrue(result.getAuthoritySet().contains(auth1));

        verify(processRoleService).findById(role1Id);
        verify(processRoleService).findById(role2Id);
        verify(authorityService).getOne(auth1Id);
        verify(authorityService).getOne(auth2Id);
    }
}

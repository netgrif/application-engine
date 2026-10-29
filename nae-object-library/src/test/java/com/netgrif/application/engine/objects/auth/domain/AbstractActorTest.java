package com.netgrif.application.engine.objects.auth.domain;

import com.netgrif.application.engine.objects.petrinet.domain.roles.ProcessRole;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class AbstractActorTest {

    static class TestActor extends AbstractActor {
        public TestActor() {
            super();
        }

        public TestActor(ObjectId id, String realmId) {
            super(id, realmId);
        }

        public TestActor(String stringId, String realmId) {
            super(stringId, realmId);
        }

        public TestActor(ObjectId id, String realmId, Map<String, Attribute<?>> attributes,
                         Set<String> authorityIds, Set<Authority> authoritySet,
                         Set<String> processRoleIds, Set<ProcessRole> processRoles,
                         Set<String> groupIds) {
            super(id, realmId, attributes, authorityIds, authoritySet, processRoleIds, processRoles, groupIds);
        }

        @Override
        String getName() {
            return "test-actor";
        }

        @Override
        String getFullName() {
            return "Test Actor FullName";
        }
    }

    static class TestAuthority extends Authority {
        private final ObjectId id;

        public TestAuthority(String name) {
            super(name);
            this.id = new ObjectId();
        }

        public TestAuthority(String id, String name) {
            super(name);
            this.id = new ObjectId(id);
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

    @Test
    void testConstructorsAndIdManagement() {
        TestActor actorDefault = new TestActor();
        assertNull(actorDefault.getId());
        assertNull(actorDefault.getRealmId());
        assertEquals("test-actor", actorDefault.getName());
        assertEquals("Test Actor FullName", actorDefault.getFullName());

        ObjectId objectId = new ObjectId();
        TestActor actorWithObjId = new TestActor(objectId, "realm-1");
        assertEquals(objectId, actorWithObjId.getId());
        assertEquals(objectId.toString(), actorWithObjId.getStringId());
        assertEquals("realm-1", actorWithObjId.getRealmId());

        String stringId = new ObjectId().toString();
        TestActor actorWithStringId = new TestActor(stringId, "realm-2");
        assertEquals(stringId, actorWithStringId.getId().toString());
        assertEquals("realm-2", actorWithStringId.getRealmId());

        ObjectId newId = new ObjectId();
        actorDefault.setId(newId);
        assertEquals(newId, actorDefault.getId());

        String newIdStr = new ObjectId().toString();
        actorDefault.setId(newIdStr);
        assertEquals(newIdStr, actorDefault.getId().toString());

        actorDefault.setRealmId("realm-updated");
        assertEquals("realm-updated", actorDefault.getRealmId());
    }

    @Test
    void testAllArgsConstructor() {
        ObjectId id = new ObjectId();
        Map<String, Attribute<?>> attrs = new HashMap<>();
        attrs.put("key1", new Attribute<>("val1", false));
        Set<String> authIds = Set.of(new ObjectId().toString());
        Set<Authority> authSet = Set.of(new TestAuthority("USER"));
        String roleIdStr = new com.netgrif.application.engine.objects.workflow.domain.ProcessResourceId().getFullId();
        Set<String> roleIds = Set.of(roleIdStr);
        Set<ProcessRole> roles = Set.of(new TestProcessRole(roleIdStr));
        Set<String> groupIds = Set.of(new ObjectId().toString());

        TestActor actor = new TestActor(id, "realm-3", attrs, authIds, authSet, roleIds, roles, groupIds);
        assertEquals(id, actor.getId());
        assertEquals("realm-3", actor.getRealmId());
        assertEquals(attrs, actor.getAttributes());
        assertEquals(authIds, actor.getAuthorityIds());
        assertEquals(authSet, actor.getAuthoritySet());
        assertEquals(roleIds, actor.getProcessRoleIds());
        assertEquals(roles, actor.getProcessRoles());
        assertEquals(groupIds, actor.getGroupIds());
    }

    @Test
    void testAttributesOperations() {
        TestActor actor = new TestActor();
        assertNotNull(actor.getAttributes());
        assertTrue(actor.getAttributeKeys().isEmpty());
        assertNull(actor.getAttributeValue("missing"));
        assertNull(actor.getAttribute("missing"));
        assertFalse(actor.isAttributeSet("missing"));

        // Setting attributes map
        actor.setAttributes(null);
        assertNotNull(actor.getAttributes());
        assertTrue(actor.getAttributes().isEmpty());

        Map<String, Attribute<?>> map = new HashMap<>();
        map.put("attr1", new Attribute<>("value1", true));
        map.put("attr2", new Attribute<>(null, false));
        actor.setAttributes(map);
        assertEquals(2, actor.getAttributeKeys().size());
        assertEquals("value1", actor.getAttributeValue("attr1"));
        assertNull(actor.getAttributeValue("attr2"));
        assertTrue(actor.isAttributeSet("attr1"));
        assertFalse(actor.isAttributeSet("attr2"));
        assertNotNull(actor.getAttribute("attr1"));

        // setAttribute single
        actor.setAttribute("attr3", "value3", false);
        assertEquals("value3", actor.getAttributeValue("attr3"));
        assertTrue(actor.isAttributeSet("attr3"));

        // removeAttribute
        actor.removeAttribute("attr1");
        assertNull(actor.getAttributeValue("attr1"));
        assertFalse(actor.isAttributeSet("attr1"));

        // test when internal map is null
        TestActor nullAttrActor = new TestActor(new ObjectId(), "realm", null, null, null, null, null, null);
        assertNotNull(nullAttrActor.getAttributes()); // getAttributes lazily creates map
        nullAttrActor.attributes = null; // force null for branch coverage
        assertNull(nullAttrActor.getAttributeValue("key"));
        assertNull(nullAttrActor.getAttribute("key"));
        assertFalse(nullAttrActor.isAttributeSet("key"));
        assertTrue(nullAttrActor.getAttributeKeys().isEmpty());
        nullAttrActor.removeAttribute("key");
        assertNotNull(nullAttrActor.attributes);

        nullAttrActor.attributes = null;
        nullAttrActor.setAttribute("newKey", "newVal", true);
        assertEquals("newVal", nullAttrActor.getAttributeValue("newKey"));
    }

    @Test
    void testValidateRequiredAttributes() {
        TestActor actor = new TestActor();
        assertTrue(actor.validateRequiredAttributes());

        actor.setAttribute("opt", null, false);
        assertTrue(actor.validateRequiredAttributes());

        Attribute<Object> reqAttr = new Attribute<>();
        reqAttr.setRequired(true);
        actor.getAttributes().put("req", reqAttr);
        assertFalse(actor.validateRequiredAttributes());

        actor.setAttribute("req", "validValue", true);
        assertTrue(actor.validateRequiredAttributes());
    }

    @Test
    void testAuthorityOperations() {
        TestActor actor = new TestActor();
        assertNotNull(actor.getAuthorityIds());
        assertNotNull(actor.getAuthoritySet());
        assertFalse(actor.isAdmin());

        // setAuthorityIds
        actor.setAuthorityIds(null);
        assertTrue(actor.getAuthorityIds().isEmpty());
        assertTrue(actor.getAuthoritySet().isEmpty());

        actor.setAuthorityIds(Set.of("auth-id-1"));
        assertEquals(Set.of("auth-id-1"), actor.getAuthorityIds());
        assertTrue(actor.getAuthoritySet().isEmpty());

        // setAuthoritySet
        TestAuthority userAuth = new TestAuthority("USER");
        TestAuthority adminAuth = new TestAuthority(Authority.admin);
        actor.setAuthoritySet(Set.of(userAuth, adminAuth));
        assertEquals(2, actor.getAuthoritySet().size());
        assertEquals(2, actor.getAuthorityIds().size());
        assertTrue(actor.getAuthorityIds().contains(userAuth.getStringId()));
        assertTrue(actor.getAuthorityIds().contains(adminAuth.getStringId()));
        assertTrue(actor.isAdmin());

        actor.setAuthoritySet(null);
        assertTrue(actor.getAuthoritySet().isEmpty());
        assertTrue(actor.getAuthorityIds().isEmpty());
        assertFalse(actor.isAdmin());

        // addAuthority
        actor.addAuthority(null);
        assertTrue(actor.getAuthoritySet().isEmpty());

        actor.addAuthority(userAuth);
        assertTrue(actor.getAuthoritySet().contains(userAuth));
        assertTrue(actor.getAuthorityIds().contains(userAuth.getStringId()));

        // removeAuthority with exact match
        actor.removeAuthority(null);
        actor.removeAuthority(userAuth);
        assertFalse(actor.getAuthoritySet().contains(userAuth));
        assertFalse(actor.getAuthorityIds().contains(userAuth.getStringId()));

        // removeAuthority with unequal instance but same name
        TestAuthority authA = new TestAuthority("MANAGER");
        TestAuthority authB = new TestAuthority("MANAGER") {
            @Override
            public boolean equals(Object o) {
                return false; // force equals to return false
            }
        };
        actor.addAuthority(authA);
        actor.removeAuthority(authB);
        assertFalse(actor.getAuthoritySet().contains(authA));

        // removeAuthorityByName
        actor.addAuthority(userAuth);
        actor.addAuthority(adminAuth);
        actor.removeAuthorityByName("NON_EXISTING");
        assertEquals(2, actor.getAuthoritySet().size());
        actor.removeAuthorityByName(userAuth.getName());
        assertEquals(1, actor.getAuthoritySet().size());
        assertFalse(actor.getAuthorityIds().contains(userAuth.getStringId()));

        // null fields coverage
        TestActor nullAuthActor = new TestActor();
        nullAuthActor.authorityIds = null;
        nullAuthActor.authoritySet = null;
        assertNotNull(nullAuthActor.getAuthorityIds());
        nullAuthActor.authorityIds = null;
        nullAuthActor.authoritySet = null;
        nullAuthActor.addAuthority(userAuth);
        assertEquals(1, nullAuthActor.getAuthoritySet().size());

        nullAuthActor.authorityIds = null;
        nullAuthActor.authoritySet = null;
        nullAuthActor.removeAuthority(userAuth);
        assertNotNull(nullAuthActor.authorityIds);
        assertNotNull(nullAuthActor.authoritySet);

        nullAuthActor.authoritySet = null;
        nullAuthActor.removeAuthorityByName("USER");
        assertNotNull(nullAuthActor.authoritySet);
    }

    @Test
    void testProcessRoleOperations() {
        TestActor actor = new TestActor();
        assertNotNull(actor.getProcessRoleIds());
        assertNotNull(actor.getProcessRoles());

        String r1Id = new com.netgrif.application.engine.objects.workflow.domain.ProcessResourceId().getFullId();
        String r2Id = new com.netgrif.application.engine.objects.workflow.domain.ProcessResourceId().getFullId();

        // setProcessRoleIds
        actor.setProcessRoleIds(null);
        assertTrue(actor.getProcessRoleIds().isEmpty());
        assertTrue(actor.getProcessRoles().isEmpty());

        actor.setProcessRoleIds(Set.of(r1Id));
        assertEquals(Set.of(r1Id), actor.getProcessRoleIds());
        assertTrue(actor.getProcessRoles().isEmpty());

        // setProcessRoles
        TestProcessRole role1 = new TestProcessRole(r1Id);
        TestProcessRole role2 = new TestProcessRole(r2Id);
        actor.setProcessRoles(Set.of(role1, role2));
        assertEquals(2, actor.getProcessRoles().size());
        assertEquals(2, actor.getProcessRoleIds().size());
        assertTrue(actor.getProcessRoleIds().contains(r1Id));

        actor.setProcessRoles(null);
        assertTrue(actor.getProcessRoles().isEmpty());
        assertTrue(actor.getProcessRoleIds().isEmpty());

        // addProcessRole
        actor.addProcessRole(null);
        assertTrue(actor.getProcessRoles().isEmpty());

        actor.addProcessRole(role1);
        assertTrue(actor.getProcessRoles().contains(role1));
        assertTrue(actor.getProcessRoleIds().contains(r1Id));

        // addAllProcessRoles
        actor.addAllProcessRoles(null);
        actor.addAllProcessRoles(List.of());
        assertEquals(1, actor.getProcessRoles().size());

        actor.addAllProcessRoles(List.of(role2));
        assertEquals(2, actor.getProcessRoles().size());

        // removeProcessRole
        actor.removeProcessRole(null);
        actor.removeProcessRole(role1);
        assertFalse(actor.getProcessRoles().contains(role1));
        assertFalse(actor.getProcessRoleIds().contains(r1Id));

        // removeProcessRole fallback by stringId
        TestProcessRole role2Copy = new TestProcessRole(r2Id) {
            @Override
            public boolean equals(Object o) {
                return false;
            }
        };
        actor.removeProcessRole(role2Copy);
        assertFalse(actor.getProcessRoleIds().contains(r2Id));

        // removeProcessRoleById
        actor.addProcessRole(role1);
        actor.removeProcessRoleById(r1Id);
        assertFalse(actor.getProcessRoleIds().contains(r1Id));

        // clearProcessRoles
        actor.addProcessRole(role1);
        actor.addProcessRole(role2);
        assertEquals(2, actor.getProcessRoles().size());
        actor.clearProcessRoles();
        assertTrue(actor.getProcessRoles().isEmpty());
        assertTrue(actor.getProcessRoleIds().isEmpty());

        // null fields branches
        TestActor nullRoleActor = new TestActor();
        nullRoleActor.processRoleIds = null;
        nullRoleActor.processRoles = null;
        assertNotNull(nullRoleActor.getProcessRoleIds());

        nullRoleActor.processRoleIds = null;
        nullRoleActor.processRoles = null;
        nullRoleActor.addProcessRole(role1);
        assertEquals(1, nullRoleActor.getProcessRoles().size());

        nullRoleActor.processRoleIds = null;
        nullRoleActor.processRoles = null;
        nullRoleActor.addAllProcessRoles(List.of(role2));
        assertEquals(1, nullRoleActor.getProcessRoles().size());

        nullRoleActor.processRoleIds = null;
        nullRoleActor.processRoles = null;
        nullRoleActor.removeProcessRole(role1);
        assertNotNull(nullRoleActor.processRoleIds);
        assertNotNull(nullRoleActor.processRoles);

        nullRoleActor.processRoleIds = null;
        nullRoleActor.processRoles = null;
        nullRoleActor.removeProcessRoleById(r1Id);
        assertNotNull(nullRoleActor.processRoleIds);
        assertNotNull(nullRoleActor.processRoles);

        nullRoleActor.processRoleIds = null;
        nullRoleActor.processRoles = null;
        nullRoleActor.clearProcessRoles();
        assertNotNull(nullRoleActor.processRoleIds);
        assertNotNull(nullRoleActor.processRoles);
    }

    @Test
    void testGroupOperations() {
        TestActor actor = new TestActor();
        assertNotNull(actor.getGroupIds());

        actor.setGroupIds(null);
        assertTrue(actor.getGroupIds().isEmpty());

        actor.setGroupIds(Set.of("g1", "g2"));
        assertEquals(Set.of("g1", "g2"), actor.getGroupIds());

        actor.addGroupId("g3");
        assertTrue(actor.getGroupIds().contains("g3"));

        actor.removeGroupId("g1");
        assertFalse(actor.getGroupIds().contains("g1"));

        // null groupIds branches
        TestActor nullGroupActor = new TestActor();
        nullGroupActor.groupIds = null;
        assertNotNull(nullGroupActor.getGroupIds());

        nullGroupActor.groupIds = null;
        nullGroupActor.addGroupId("g1");
        assertEquals(Set.of("g1"), nullGroupActor.getGroupIds());

        nullGroupActor.groupIds = null;
        nullGroupActor.removeGroupId("g1");
        assertNotNull(nullGroupActor.groupIds);
    }
}

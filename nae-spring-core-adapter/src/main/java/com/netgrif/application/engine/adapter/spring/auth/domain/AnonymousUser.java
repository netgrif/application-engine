//package com.netgrif.application.engine.adapter.spring.auth.domain;
//
//import com.netgrif.application.engine.adapter.spring.auth.domain.mapper.LoggedUserMapper;
//import com.netgrif.application.engine.adapter.spring.auth.domain.mapper.UserAuthorMapper;
//import com.netgrif.application.engine.objects.auth.domain.*;
//import com.netgrif.application.engine.objects.auth.domain.enums.UserState;
//import lombok.Data;
//
//import java.time.Duration;
//import java.util.*;
//
//@Data
//public class AnonymousUser extends AbstractUser implements IUser {
//
//    private final String id;
//    private final String username;
//    private final String realmId;
//
//    private String email;
//    private String firstName;
//    private String lastName;
//
//    private transient Duration sessionTimeout = Duration.ofHours(2);
//
//    private final Map<String, Attribute<?>> attributes = new HashMap<>();
//
//    public AnonymousUser(AnonymousUserRef ref, Authority anonymousAuthority) {
//        this.id = UUID.randomUUID().toString();
//        this.realmId = ref.getRealmId();
//        this.username = "anonymous@" + this.realmId;
//        this.state = ref.getState() != null ? ref.getState() : UserState.ACTIVE;
//        this.firstName = ref.getDisplayName();
//        this.lastName = "";
//
//        this.authorities = new HashSet<>();
//        if (ref.getAuthorities() != null && !ref.getAuthorities().isEmpty()) {
//            this.authorities.addAll(ref.getAuthorities());
//        } else {
//            this.authorities.add(anonymousAuthority);
//        }
//
//        this.processRoles = ref.getProcessRoles() != null ? new HashSet<>(ref.getProcessRoles()) : new HashSet<>();
//        this.groupIds = ref.getGroupIds() != null ? new HashSet<>(ref.getGroupIds()) : new HashSet<>();
//        this.groups = ref.getGroups() != null ? new HashSet<>(ref.getGroups()) : new HashSet<>();
//    }
//
//
//    @Override
//    public String getStringId() {
//        return id;
//    }
//
//    @Override
//    public String getUsername() {
//        return username;
//    }
//
//    @Override
//    public void setUsername(String username) {
//    }
//
//    @Override
//    public String getRealmId() {
//        return realmId;
//    }
//
//    @Override
//    public void setRealmId(String realmId) {
//    }
//
//    @Override
//    public String getEmail() {
//        return email;
//    }
//
//    @Override
//    public void setEmail(String email) {
//        this.email = email;
//    }
//
//    @Override
//    public String getFirstName() {
//        return firstName;
//    }
//
//    @Override
//    public void setFirstName(String firstName) {
//        this.firstName = firstName;
//    }
//
//    @Override
//    public String getLastName() {
//        return lastName;
//    }
//
//    @Override
//    public void setLastName(String lastName) {
//        this.lastName = lastName;
//    }
//
//    @Override
//    public String getFullName() {
//        return firstName;
//    }
//
//    @Override
//    public String getName() {
//        return firstName;
//    }
//
//    @Override
//    public String getAvatar() {
//        return null;
//    }
//
//    @Override
//    public void setAvatar(String avatar) {
//    }
//
//    @Override
//    public String getTelNumber() {
//        Attribute<?> tel = attributes.get("tel");
//        return tel != null ? String.valueOf(tel.getValue()) : null;
//    }
//
//    @Override
//    public void setTelNumber(String telNumber) {
//        this.attributes.put("tel", new Attribute<>(telNumber, false));
//    }
//
//    @Override
//    public void addGroupId(String groupId) {
//        this.groupIds.add(groupId);
//    }
//
//    @Override
//    public boolean validateRequiredAttributes() {
//        return attributes.values().stream().noneMatch(attr -> attr.isRequired() && attr.getValue() == null);
//    }
//
//    @Override
//    public void enableMFA(String type, String value, int order) {
//    }
//
//    @Override
//    public void disableMFA(String type) {
//    }
//
//    @Override
//    public boolean isMFAEnabled(String type) {
//        return false;
//    }
//
//    @Override
//    public void activateMFA(String type, String secret) {
//    }
//
//    @Override
//    public void activateMFA(String type, String secret, boolean activate) {
//    }
//
//    @Override
//    public Set<String> getEnabledMFAMethods() {
//        return Collections.emptySet();
//    }
//
//    @Override
//    public Credential<?> getCredential(String type) {
//        return null;
//    }
//
//    @Override
//    public <T> Object getCredentialValue(String type) {
//        return null;
//    }
//
//    @Override
//    public void setCredential(String type, String value, int order, boolean enabled) {
//    }
//
//    @Override
//    public void addCredential(Credential<?> credential) {
//    }
//
//    @Override
//    public void setCredentialProperty(String type, String key, Object value) {
//
//    }
//
//    @Override
//    public Object getCredentialProperty(String type, String key) {
//        return null;
//    }
//
//    @Override
//    public void removeCredential(String type) {
//    }
//
//    @Override
//    public boolean hasCredential(String type) {
//        return false;
//    }
//
//    @Override
//    public void setAttribute(String key, Object value, boolean required) {
//        attributes.put(key, new Attribute<>(value, required));
//    }
//
//    @Override
//    public Object getAttributeValue(String key) {
//        Attribute<?> a = attributes.get(key);
//        return a != null ? a.getValue() : null;
//    }
//
//    @Override
//    public void removeAttribute(String key) {
//        attributes.remove(key);
//    }
//
//    @Override
//    public boolean isAttributeSet(String key) {
//        return attributes.containsKey(key) && attributes.get(key).getValue() != null;
//    }
//
//    @Override
//    public Attribute<?> getAttribute(String key) {
//        return attributes.get(key);
//    }
//
//    @Override
//    public LoggedUser transformToLoggedUser() {
//        return LoggedUserMapper.toLoggedUser(this);
//    }
//
//    @Override
//    public Author transformToAuthor() {
//        return UserAuthorMapper.toAuthor(this);
//    }
//
//    @Override
//    public String toString() {
//        return "[AnonymousUser id=%s username=%s realm=%s]".formatted(id, username, realmId);
//    }
//}

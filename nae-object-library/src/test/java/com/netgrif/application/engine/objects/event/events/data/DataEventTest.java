package com.netgrif.application.engine.objects.event.events.data;

import com.netgrif.application.engine.objects.auth.domain.AbstractUser;
import com.netgrif.application.engine.objects.auth.domain.ActorTransformer;
import com.netgrif.application.engine.objects.auth.domain.LoggedUser;
import com.netgrif.application.engine.objects.petrinet.domain.events.EventPhase;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;

class DataEventTest {

    private ActorTransformer.LoggedUserFactory originalFactory;
    private AtomicInteger createdLoggedUsers;

    @BeforeEach
    void setUp() {
        originalFactory = ActorTransformer.getLoggedUserFactory();
        createdLoggedUsers = new AtomicInteger();
        ActorTransformer.setLoggedUserFactory(() -> {
            createdLoggedUsers.incrementAndGet();
            return new TestLoggedUser();
        });
    }

    @AfterEach
    void tearDown() {
        ActorTransformer.setLoggedUserFactory(originalFactory);
    }

    @Test
    void reusesAlreadyConvertedLoggedUser() {
        TestLoggedUser loggedUser = new TestLoggedUser();

        TestDataEvent event = new TestDataEvent(new Object(), loggedUser);

        assertSame(loggedUser, event.getUser());
        assertEquals(0, createdLoggedUsers.get());
    }

    @Test
    void reusesAlreadyConvertedLoggedUserWithEventPhase() {
        TestLoggedUser loggedUser = new TestLoggedUser();

        TestDataEvent event = new TestDataEvent(new Object(), EventPhase.PRE, loggedUser);

        assertSame(loggedUser, event.getUser());
        assertEquals(EventPhase.PRE, event.getEventPhase());
        assertEquals(0, createdLoggedUsers.get());
    }

    @Test
    void convertsPlainUserToLoggedUser() {
        TestUser user = new TestUser("alice");

        TestDataEvent event = new TestDataEvent(new Object(), user);

        assertNotSame(user, event.getUser());
        assertEquals("alice", event.getUser().getUsername());
        assertEquals(1, createdLoggedUsers.get());
    }

    @Test
    void convertsPlainUserToLoggedUserWithEventPhase() {
        TestUser user = new TestUser("bob");

        TestDataEvent event = new TestDataEvent(new Object(), EventPhase.POST, user);

        assertEquals("bob", event.getUser().getUsername());
        assertEquals(EventPhase.POST, event.getEventPhase());
        assertEquals(1, createdLoggedUsers.get());
    }

    private static class TestDataEvent extends DataEvent {

        TestDataEvent(Object source, AbstractUser user) {
            super(source, user);
        }

        TestDataEvent(Object source, EventPhase eventPhase, AbstractUser user) {
            super(source, eventPhase, user);
        }

        @Override
        public String getMessage() {
            return "test";
        }
    }

    private static class TestLoggedUser extends LoggedUser {

        private String password;

        @Override
        public String getPassword() {
            return password;
        }

        @Override
        public void setPassword(String password) {
            this.password = password;
        }
    }

    private static class TestUser extends AbstractUser {

        private String password;

        TestUser(String username) {
            super(new ObjectId(), "realm", username, "First", "Middle", "Last", username + "@example.com", null);
        }

        @Override
        public String getPassword() {
            return password;
        }

        @Override
        public void setPassword(String password) {
            this.password = password;
        }
    }
}

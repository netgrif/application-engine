package com.netgrif.application.engine.authorization.service;

import com.netgrif.application.engine.authorization.domain.Actor;
import com.netgrif.application.engine.authorization.domain.User;
import com.netgrif.application.engine.authorization.domain.params.UserParams;
import com.netgrif.application.engine.authorization.service.interfaces.IUserService;
import com.netgrif.application.engine.petrinet.domain.dataset.TextField;
import com.netgrif.application.engine.startup.SuperCreator;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;

@SpringBootTest
@ActiveProfiles({"test"})
@ExtendWith(SpringExtension.class)
public class ActorServiceTest {

    @Autowired
    private ActorService actorService;

    @Autowired
    private IUserService userService;

    @Autowired
    private SuperCreator superCreator;

    @Test
    public void testFindById() {
        assert actorService.findById(null).isEmpty();
        assert actorService.findById(new ObjectId().toString()).isEmpty();
        String notActorCaseId = superCreator.getSuperIdentity().getStringId();
        assert actorService.findById(notActorCaseId).isEmpty();

        User user = userService.create(UserParams.with()
                .email(new TextField("s@meemail.com"))
                .build());

        Optional<Actor> foundActorOpt = actorService.findById(user.getStringId());
        assert foundActorOpt.isPresent();
        assert foundActorOpt.get() instanceof User;
        assert foundActorOpt.get().getStringId().equals(user.getStringId());
    }
}

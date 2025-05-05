package com.netgrif.application.engine.authorization.service;

import com.netgrif.application.engine.authorization.domain.Actor;
import com.netgrif.application.engine.authorization.domain.User;
import com.netgrif.application.engine.authorization.domain.params.UserParams;
import com.netgrif.application.engine.authorization.service.interfaces.IUserService;
import com.netgrif.application.engine.petrinet.domain.dataset.TextField;
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
public class AllActorServiceTest {

    @Autowired
    private AllActorService allActorService;

    @Autowired
    private IUserService userService;

    @Test
    public void testFindById() {
        assert allActorService.findById(null).isEmpty();
        assert allActorService.findById(new ObjectId().toString()).isEmpty();

        User user = userService.create(UserParams.with()
                .email(new TextField("s@meemail.com"))
                .build());

        Optional<Actor> foundActorOpt = allActorService.findById(user.getStringId());
        assert foundActorOpt.isPresent();
    }
}

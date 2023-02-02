package com.netgrif.application.engine.manager.responseclass;

import com.netgrif.application.engine.TestHelper;
import com.netgrif.application.engine.auth.domain.LoggedUser;
import com.netgrif.application.engine.manager.web.body.response.AllLoggedUsersResponse;
import com.netgrif.application.engine.manager.web.body.response.MessageLogoutResponse;
import com.netgrif.application.engine.startup.SuperCreator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.ArrayList;
import java.util.Collection;

@SpringBootTest
@ActiveProfiles({"test"})
@ExtendWith(SpringExtension.class)
class ResponseTest {

    @Autowired
    private TestHelper testHelper;

    @Autowired
    private SuperCreator superCreator;

    @BeforeEach
    public void before() {
        testHelper.truncateDbs();
    }

    @Test
    void allLoggedUsersResponseTest() {
        Collection<LoggedUser> content = new ArrayList<>();
        content.add(superCreator.getLoggedSuper());
        AllLoggedUsersResponse response = new AllLoggedUsersResponse(content);
        assert response != null;
        assert response.getContent().size() == 1;
    }
    @Test
    void messageLogoutResponseTest() {
        MessageLogoutResponse response = new MessageLogoutResponse(true);
        assert response != null;
        assert response.getContent() == true;
    }

}

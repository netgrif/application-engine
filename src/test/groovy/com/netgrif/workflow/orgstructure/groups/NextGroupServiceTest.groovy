package com.netgrif.workflow.orgstructure.groups

import com.netgrif.workflow.auth.service.UserService
import com.netgrif.workflow.orgstructure.groups.interfaces.INextGroupService
import com.netgrif.workflow.petrinet.domain.PetriNet
import com.netgrif.workflow.startup.GroupRunner
import com.netgrif.workflow.workflow.domain.Case
import com.netgrif.workflow.workflow.domain.QCase
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit4.SpringRunner

@RunWith(SpringRunner.class)
@ActiveProfiles(["dev"])
@SpringBootTest
class NextGroupServiceTest {

    @Autowired
    INextGroupService nextGroupService

    @Autowired
    GroupRunner groupRunner

    @Autowired
    UserService userService


    @Test
    void groupTest(){
        Optional<PetriNet> groupNet = importGroup()
        assert groupNet.isPresent()

        Case customGroup = createGroup()
        if(customGroup == null){
            throw new NullPointerException()
        }

        List<Case> allGroups = findAllGroups()
        assert !allGroups.isEmpty()

        List<Case> byPredicate = findGroup()
        assert !byPredicate.isEmpty()

        Case addedUserGroup = addUser()
        assert !addedUserGroup.getDataSet().get("members").getOptions().isEmpty()

        Case removedUserGroup = removeUser()
        assert !removedUserGroup.getDataSet().get("members").getOptions().isEmpty()
    }

    Optional<PetriNet> importGroup(){
       return groupRunner.createDefaultGroup()
    }

    Case createGroup(){
        return nextGroupService.createGroup("CUSTOM_GROUP_1", userService.findByEmail("dummy@netgrif.com", false))
    }

    List<Case> findGroup(){
        QCase qCase = new QCase("case")
        return nextGroupService.findByPredicate(qCase.author.email.eq("dummy@netgrif.com"))
    }

    List<Case> findAllGroups(){
        return nextGroupService.findAllGroups()
    }

    Case addUser(){
        QCase qCase = new QCase("case")
        Case group = nextGroupService.findByPredicate(qCase.title.eq("CUSTOM_GROUP_1")).get(0)
        nextGroupService.addUser(userService.findByEmail("user@netgrif.com", false), group)
        nextGroupService.addUser(userService.findByEmail("engine@netgrif.com", false), group)
        return group
    }

    Case removeUser(){
        QCase qCase = new QCase("case")
        Case group = nextGroupService.findByPredicate(qCase.title.eq("CUSTOM_GROUP_1")).get(0)
        nextGroupService.removeUser(userService.findByEmail("user@netgrif.com", false), group)
        return group
    }
}

package com.netgrif.workflow.organisation

import com.netgrif.workflow.importer.service.Importer
import com.netgrif.workflow.orgstructure.domain.Group
import com.netgrif.workflow.orgstructure.domain.GroupRepository
import com.netgrif.workflow.orgstructure.domain.Member
import com.netgrif.workflow.orgstructure.domain.MemberRepository
import com.netgrif.workflow.startup.ImportHelper
import com.netgrif.workflow.startup.SuperCreator
import com.netgrif.workflow.workflow.domain.repositories.CaseRepository
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit4.SpringRunner

@RunWith(SpringRunner.class)
@ActiveProfiles(["test"])
@SpringBootTest
class Neo4jTest {

    public static final String PARENT = "parent"
    public static final String CHILD_1 = "child_1"
    public static final String CHILD_2 = "child_2"
    @Autowired
    private MemberRepository memberRepository

    @Autowired
    private GroupRepository groupRepository

    @Test
    void test() {
        groupRepository.deleteAll()
        memberRepository.deleteAll()

        Group parent = groupRepository.save(new Group(PARENT))
        Group child1 = groupRepository.save(new Group(CHILD_1))
        Group child2 = groupRepository.save(new Group(CHILD_2))

        child1.parentGroup = parent
        child2.parentGroup = parent

        groupRepository.save(child1)
        groupRepository.save(child2)

        parent = groupRepository.findOne(parent.getId())
        child1 = groupRepository.findOne(child1.getId())
        child2 = groupRepository.findOne(child2.getId())

        assert parent.childGroups.size() == 2
        assert child1.parentGroup
        assert child2.parentGroup
    }

    @Test
    void testRelationship() {
        groupRepository.deleteAll()
        memberRepository.deleteAll()

        Group parent = groupRepository.save(new Group(PARENT))
        Member member = memberRepository.save(new Member(userId: 1L, email: "member@parent.com"))

        parent.addMember(member)

        // Both methods works
//        parent = groupRepository.save(parent)
        memberRepository.save(member)

        assert parent.members.size() == 1
        assert member.groups.size() == 1
    }

    @Test
    void testMembers() {
        groupRepository.deleteAll()
        memberRepository.deleteAll()

        Group parent = groupRepository.save(new Group(PARENT))
        Group child1 = groupRepository.save(new Group(CHILD_1))
        Group child2 = groupRepository.save(new Group(CHILD_2))
        Group child3 = groupRepository.save(new Group("child_3"))

        child1.parentGroup = parent
        child2.parentGroup = parent
        child3.parentGroup = child1

        groupRepository.save(child1)
        groupRepository.save(child2)
        groupRepository.save(child3)

        parent = groupRepository.findOne(parent.getId())
        child1 = groupRepository.findOne(child1.getId())
        child2 = groupRepository.findOne(child2.getId())
        child3 = groupRepository.findOne(child3.getId())

        Member member1 = memberRepository.save(new Member(userId: 1L, email: "1@parent.com"))
        Member member2 = memberRepository.save(new Member(userId: 2L, email: "2@parent.com"))
        Member member3 = memberRepository.save(new Member(userId: 3L, email: "3@parent.com"))
        Member member4 = memberRepository.save(new Member(userId: 4L, email: "4@parent.com"))
        Member member5 = memberRepository.save(new Member(userId: 5L, email: "5@parent.com"))

        parent.addMember(member1)
        child1.addMember(member2)
        child1.addMember(member3)
        child2.addMember(member4)
        child3.addMember(member5)

        groupRepository.save(parent)
        groupRepository.save(child1)
        groupRepository.save(child2)
        groupRepository.save(child3)

        def comembers = memberRepository.findAllCoMembersIds("1@parent.com")

        assert comembers.size() == 5
    }

    public static final String SEARCH_NET_FILE = "ipc_group.xml"
    public static final String SEARCH_NET_NAME = "Groups"
    public static final String SEARCH_NET_INITIALS = "GRP"

    @Autowired
    private ImportHelper helper
    @Autowired
    private Importer importer
    @Autowired
    private CaseRepository caseRepository
    @Autowired
    private SuperCreator superCreator

    private def stream = { String name ->
        return Neo4jTest.getClassLoader().getResourceAsStream(name)
    }

    @Test
    void testFindOrganization() {
        groupRepository.deleteAll()
        memberRepository.deleteAll()

        def testNet = importer.importPetriNet(stream(SEARCH_NET_FILE), SEARCH_NET_NAME, SEARCH_NET_INITIALS)
        assert testNet.isPresent()

        def acase = helper.createCase("Case Group" as String, testNet.get())

        Group parent = groupRepository.save(new Group(PARENT))
        Member member1 = memberRepository.save(Member.from(superCreator.superUser))
        parent.addMember(member1)
        groupRepository.save(parent)

        helper.assignTaskToSuper("Task", acase.stringId)
        acase = caseRepository.findOne(acase.stringId)

        assert acase.dataSet["orgs"].choices.find {it.defaultValue == PARENT}
        assert acase.dataSet["orgs"].choices.find {it.defaultValue == "Test organization"}
    }
}
package com.netgrif.application.engine.petrinet.service

import com.netgrif.application.engine.TestHelper
import com.netgrif.application.engine.configuration.properties.UriProperties
import com.netgrif.application.engine.petrinet.domain.UriContentType
import com.netgrif.application.engine.petrinet.domain.UriNode
import com.netgrif.application.engine.petrinet.domain.repository.UriNodeRepository
import com.netgrif.application.engine.petrinet.service.interfaces.IUriService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension

import static org.junit.jupiter.api.Assertions.assertThrows

@SpringBootTest
@ActiveProfiles(["test"])
@ExtendWith(SpringExtension.class)
class UriServiceTest {

    private static final String testUri1 = "/test/uri/child1"
    private static final String testUri2 = "/test/uri/child2"
    private static final String testUri3 = "/test/uri/child1/grandchild"
    private static final String testUri4 = "/test/uri/child1/grandchild"
    private static final String testUri5wrong = "test/uri/child3/grandchild"
    private static final String testUri5 = "/test/uri/child3/grandchild"
    private static final String testUri5parent = "/test/uri/child3"
    private static final String testUri6wrong = ""
    private static final String testUri6 = "/"
    private static final String destination = "destination/path"

    @Autowired
    private IUriService uriService

    @Autowired
    private UriProperties uriProperties

    @Autowired
    private TestHelper testHelper

    @Autowired
    UriNodeRepository uriNodeRepository

    @BeforeEach
    void init() {
        testHelper.truncateDbs()
        uriService.getOrCreate(testUri1, UriContentType.DEFAULT)
        uriService.getOrCreate(testUri2, UriContentType.DEFAULT)
        uriService.getOrCreate(testUri3, UriContentType.DEFAULT)
        uriService.getOrCreate(testUri4, UriContentType.DEFAULT)
    }

    @Test
    void getOrCreateTest() {
        String[] splitUri = testUri3.split(uriProperties.separator)
        splitUri[0] = "/"
        UriNode uriNode = uriService.getOrCreate(testUri3, UriContentType.CASE)
        assert uriNode != null && uriNode.getName() == splitUri[splitUri.length - 1]
        assert uriNode.parentId == testUri1
        assert uriNode.contentTypes.size() == 2 && uriNode.contentTypes.contains(UriContentType.DEFAULT)
        assert uriNode.getChildrenId().isEmpty()
        assert uriNode.level == 4

        uriNode = uriService.getOrCreate(testUri1, UriContentType.CASE)
        assert uriNode.getChildrenId().size() == 1 && uriNode.getChildrenId().contains(testUri3)
        assert uriNode.containsCase()

        uriNode = uriService.getOrCreate(testUri5wrong, UriContentType.PROCESS)
        assert uriNode != null && uriNode.id == testUri5
        assert uriNode.parentId == testUri5parent
        assert uriNode.containsNet()

        uriNode = uriService.getOrCreate(testUri6wrong, UriContentType.DEFAULT)
        assert uriNode.id == testUri6
        assert uriNode.parentId == null
        assert uriNode.level == 0
    }

    @Test
    void getRootsTest() {
        UriNode root = uriService.getRoot()
        assert root.getParentId() == null

        UriNode root2 = new UriNode()
        root2.setId("root2")
        root2.setLevel(0)
        uriService.save(root2)

        assertThrows(IllegalStateException.class, () -> {
            uriService.getRoot()
        })
    }

    @Test
    void populateDirectRelativesTest() {
        UriNode uriNode = uriService.getOrCreate(testUri1, UriContentType.DEFAULT)
        uriNode = uriService.populateDirectRelatives(uriNode)

        assert uriNode.parent != null && uriNode.parent.id == uriNode.parentId
        assert uriNode.children.size() == 1 && uriNode.children.find {it.id == uriNode.childrenId[0]} != null
    }

    @Test
    @Disabled("Move needs refactor")
    void moveTest() {
        UriNode uriNode = uriService.move(testUri1, destination)
        assert uriNode.id == destination + uriProperties.separator + uriNode.name
        UriNode parent = uriService.findById(uriNode.parentId)
        assert parent.childrenId.contains(uriNode.id)
    }

    @Test
    void findTest() {
        UriNode uriNode = uriService.findById(testUri3)
        assert uriNode != null

        assertThrows(IllegalArgumentException.class, () -> {
            uriService.findById("notSavedId")
        })
    }

    @Test
    void createDefaultTest() {
        UriNode uriNode = uriService.createDefault()
        assert uriNode != null && uriNode.level == 0

        uriNodeRepository.deleteById(uriNode.id)

        uriNode = uriService.createDefault()
        assert uriNode != null && uriNode.level == 0
    }


}

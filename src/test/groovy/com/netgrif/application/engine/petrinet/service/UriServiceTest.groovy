package com.netgrif.application.engine.petrinet.service

import com.netgrif.application.engine.TestHelper
import com.netgrif.application.engine.configuration.properties.UriProperties
import com.netgrif.core.petrinet.domain.UriContentType
import com.netgrif.core.petrinet.domain.UriNode
import com.netgrif.application.engine.petrinet.domain.repository.UriNodeRepository
import com.netgrif.application.engine.petrinet.service.interfaces.IUriService
import org.junit.jupiter.api.BeforeEach
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
        assert uriNode.parentId == uriNodeRepository.findByPath(testUri1).stringId
        assert uriNode.contentTypes.size() == 2 && uriNode.contentTypes.contains(UriContentType.DEFAULT)
        assert uriNode.getChildrenId().isEmpty()
        assert uriNode.level == 4

        uriNode = uriService.getOrCreate(testUri1, UriContentType.CASE)
        assert uriNode.getChildrenId().size() == 1 && uriNode.getChildrenId().contains(uriNodeRepository.findByPath(testUri3).stringId)
        assert uriNode.containsCase()

        uriNode = uriService.getOrCreate(testUri5wrong, UriContentType.PROCESS)
        assert uriNode != null && uriNode.stringId == uriNodeRepository.findByPath(testUri5).stringId
        assert uriNode.parentId == uriNodeRepository.findByPath(testUri5parent).stringId
        assert uriNode.containsNet()

        uriNode = uriService.getOrCreate(testUri6wrong, UriContentType.DEFAULT)
        assert uriNode.stringId == uriNodeRepository.findByPath(testUri6).stringId
        assert uriNode.parentId == null
        assert uriNode.level == 0
    }

    @Test
    void getRootsTest() {
        UriNode root = uriService.getRoot()
        assert root.getParentId() == null

        UriNode root2 = new com.netgrif.adapter.petrinet.domain.UriNode()
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

        assert uriNode.parent != null && uriNode.parent.stringId == uriNode.parentId
        assert uriNode.children.size() == 1 && uriNode.children.find {it.stringId == uriNode.childrenId[0]} != null
    }

    @Test
    void moveTest() {
        prepareDatabase(List.of("/a/b/c"))
        UriNode cNode = uriService.move("/a/b/c", "/a")
        UriNode aNode = uriService.findByUri("/a")
        UriNode bNode = uriService.findByUri("/a/b")
        assert cNode.parentId == aNode.stringId && bNode.childrenId.size() == 0

        prepareDatabase(List.of("/a/b/c", "/a/b/d"))
        bNode = uriService.move("/a/b", "/")
        aNode = uriService.findByUri("/a")
        cNode = uriService.findByUri("/b/c")
        UriNode rootNode = uriService.findByUri("/")
        assert aNode.childrenId.size() == 0 && bNode.childrenId.size() == 2 && cNode.parentId == bNode.stringId
        assert rootNode.childrenId.size() == 2

        prepareDatabase(List.of("/a/b/c"))
        assertThrows(IllegalArgumentException.class, () -> {
            uriService.move("/a/b", "/a/b/c/d")
        })

        prepareDatabase(List.of("/a/b/c"))
        bNode = uriService.move("/a/b", "d")
        aNode = uriService.findByUri("/a")
        UriNode dNode = uriService.findByUri("/d")
        assert aNode.childrenId.size() == 0
        assert dNode.childrenId.size() == 1
        assert bNode.childrenId.size() == 1 && bNode.parentId == dNode.stringId
    }

    private prepareDatabase(List<String> listOfUriPaths) {
        uriNodeRepository.deleteAll()
        listOfUriPaths.each {path ->
            uriService.getOrCreate(path, UriContentType.DEFAULT)
        }
    }

    @Test
    void findTest() {
        UriNode uriNode = uriService.findByUri(testUri3)
        assert uriNode != null

        assertThrows(IllegalArgumentException.class, () -> {
            uriService.findById("notSavedId")
        })
    }

    @Test
    void createDefaultTest() {
        UriNode uriNode = uriService.createDefault()
        assert uriNode != null && uriNode.level == 1

        uriNodeRepository.deleteById(uriNode.stringId)

        uriNode = uriService.createDefault()
        assert uriNode != null && uriNode.level == 1
    }

    @Test
    void getRootTest() {
        UriNode uriNode = uriService.createDefault()
        assert uriNode != null && uriNode.level == 1
        uriNode = uriService.getRoot()
        assert uriNode != null && uriNode.level == 0
    }
}

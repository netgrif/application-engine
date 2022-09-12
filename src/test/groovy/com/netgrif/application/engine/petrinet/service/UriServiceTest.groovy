package com.netgrif.application.engine.petrinet.service

import com.netgrif.application.engine.TestHelper
import com.netgrif.application.engine.configuration.properties.UriProperties
import com.netgrif.application.engine.petrinet.domain.UriContentType
import com.netgrif.application.engine.petrinet.domain.UriNode
import com.netgrif.application.engine.petrinet.service.interfaces.IUriService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension

@SpringBootTest
@ActiveProfiles(["test"])
@ExtendWith(SpringExtension.class)
class UriServiceTest {

    private static final String testUri1 = "nae/test/uri/child1"
    private static final String testUri2 = "nae/test/uri/child2"
    private static final String testUri3 = "nae/test/uri/child1/grandchild"
    private static final String testUri4 = "nae_other/test/uri/child1/grandchild"
    private static final String destination = "destination/path"
    private static final String[] roots = new String[]{"default", "nae", "nae_other"}

    @Autowired
    private IUriService uriService

    @Autowired
    private UriProperties uriProperties

    @Autowired
    private TestHelper testHelper

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
        String[] splitUri = testUri1.split(uriProperties.separator)
        UriNode uriNode = uriService.getOrCreate(testUri1, UriContentType.DEFAULT)
        assert uriNode != null && uriNode.getName() == splitUri[splitUri.length - 1]
    }

    @Test
    @Disabled("Fix test")
    void getRootsTest() {
        UriNode root = uriService.getRoot()
        assert root.getParentId() == null
    }

    @Test
    void moveTest() {
        UriNode uriNode = uriService.move(testUri1, destination)
        assert uriNode.uriPath == destination + uriProperties.separator + uriNode.name
        UriNode parent = uriService.findById(uriNode.parentId)
        assert parent.childrenId.contains(uriNode.id)
    }

    @Test
    void findTest() {
        UriNode uriNode = uriService.findByUri(testUri3)
        assert uriNode != null
    }


}

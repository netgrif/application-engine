package com.netgrif.application.engine.petrinet.service

import com.netgrif.application.engine.TestHelper
import com.netgrif.application.engine.petrinet.domain.UriNode
import com.netgrif.application.engine.petrinet.domain.UriType
import com.netgrif.application.engine.petrinet.service.interfaces.IUriService
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(SpringExtension.class)
@ActiveProfiles(["test"])
@SpringBootTest
class UriServiceTest {

    private static final String uriSeparator = "/"
    private static final String testUri1 = "nae/test/uri/child1"
    private static final String testUri2 = "nae/test/uri/child2"
    private static final String testUri3 = "nae/test/uri/child1/grandchild"
    private static final String testUri4 = "nae_other/test/uri/child1/grandchild"
    private static final String destination = "destination/path"
    private static final String[] roots = new String[] {"default", "nae", "nae_other"}

    @Autowired
    private IUriService uriService

    @Autowired
    private TestHelper testHelper

    @BeforeEach
    void init() {
        testHelper.truncateDbs()
        uriService.getOrCreate(testUri1, UriType.DEFAULT)
        uriService.getOrCreate(testUri2, UriType.DEFAULT)
        uriService.getOrCreate(testUri3, UriType.DEFAULT)
        uriService.getOrCreate(testUri4, UriType.DEFAULT)
    }

    @Test
    void getOrCreateTest() {
        String[] splitUri = testUri1.split(uriSeparator)
        UriNode uriNode = uriService.getOrCreate(testUri1, UriType.DEFAULT)
        assert uriNode != null && uriNode.getName() == splitUri[splitUri.length - 1]
    }

    @Test
    void getRootsTest() {
        List<UriNode> rootList = uriService.getRoots()
        assert rootList.size() == roots.length
    }

    @Test
    void moveTest() {
        UriNode uriNode = uriService.move(testUri1, destination)
        assert uriNode.uriPath == destination + uriSeparator + uriNode.name
    }

    @Test
    void findTest() {
        UriNode uriNode = uriService.findByUri(testUri3)
        assert uriNode != null
    }


}

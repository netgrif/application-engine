package com.netgrif.application.engine.insurance

import com.netgrif.application.engine.business.orsr.IOrsrService
import com.netgrif.application.engine.business.orsr.OrsrReference
import groovy.util.logging.Slf4j
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(SpringExtension.class)
@ActiveProfiles(["test"])
@SpringBootTest
@Slf4j
class OrsrTest {

    @Autowired
    private IOrsrService service

    @Test
    @Disabled("External ORSR integration test depends on live registry data")
    void parseTest() {
        def ICO = 50_903_403 as String
        OrsrReference info = service.findByIco(ICO)
        assertCorrectValidOrsrInfo(info)
    }

    @SuppressWarnings("GrMethodMayBeStatic")
    private assertCorrectValidOrsrInfo(OrsrReference info) {
        assert info.name == "NETGRIF, s.r.o."
        assert info.city == "Bratislava - mestská časť Karlova Ves"
        assert info.postalCode == "841 04"
        assert info.street == "Dúbravská cesta"
        assert info.streetNumber == "14"
    }
}

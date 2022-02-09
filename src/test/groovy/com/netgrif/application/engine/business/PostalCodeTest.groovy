package com.netgrif.application.engine.business

import com.netgrif.application.engine.startup.PostalCodeImporter
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(SpringExtension.class)
@ActiveProfiles(["test"])
@SpringBootTest(properties = "nae.postal.codes.import=true")
@SuppressWarnings("GrMethodMayBeStatic")
class PostalCodeTest {

    private static boolean setup = false

    @Autowired
    private IPostalCodeService service

    @Autowired
    private PostalCodeImporter importer

    @BeforeEach
    void before() {
        if (setup)
            return

        if (service.findAllByCode("841 05").size() == 0)
            importer.run()

        setup = true
    }

    @Test
    void oneMatchTest() {
        def psc = "841 05"

        List<PostalCode> cities = service.findAllByCode(psc)

        assertOneMatch(cities)
    }

    @Test
    void multipleMatchTest() {
        def psc = "851 10"

        List<PostalCode> cities = service.findAllByCode(psc)

        assertMultipleMatch(cities)
    }

    private assertOneMatch(List<PostalCode> cities) {
        assert cities != null
        assert cities.size() == 1
        assert cities.first().city == "Bratislava"
    }

    private assertMultipleMatch(List<PostalCode> cities) {
        assert cities != null
        assert cities.size() > 1
        assert cities.find { it.city == "Bratislava - Rusovce" } != null
    }
}
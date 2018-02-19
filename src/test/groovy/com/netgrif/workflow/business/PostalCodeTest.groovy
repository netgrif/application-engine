package com.netgrif.workflow.business

import com.netgrif.workflow.startup.PostalCodeImporter
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit4.SpringRunner

@RunWith(SpringRunner.class)
@ActiveProfiles(["test"])
@SpringBootTest
@SuppressWarnings("GrMethodMayBeStatic")
class PostalCodeTest {

    private static boolean setup = false

    @Autowired
    private IPostalCodeService service

    @Autowired
    private PostalCodeImporter importer

    @Before
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
        def psc = "900 42"

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
        assert cities.find { it.city == "Alžbetin Dvor" }
    }
}
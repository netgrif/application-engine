package com.netgrif.application.engine.business

import com.netgrif.application.engine.TestHelper
import com.netgrif.application.engine.startup.PostalCodeImporter
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
class PostalCodeTest {

    private static boolean setup = false

    @Autowired
    private IPostalCodeService service

    @Autowired
    private PostalCodeImporter importer

    @Autowired
    private TestHelper testHelper

    @BeforeEach
    void before() {
        if (setup) {
            return
        }

        assert service != null: "IPostalCodeService is null!"
        assert importer != null: "PostalCodeImporter is null!"

        if (service.findAllByCode("841 05").isEmpty()) {
            testHelper.truncateDbs()

            System.out.println("Data not found, running importer...")
            importer.run();

            List<PostalCode> loadedCodes = service.findAllByCode("841 05")
            System.out.println("Loaded postal codes after import: " + loadedCodes)
            assert !loadedCodes.isEmpty(): "Postal codes were not imported!"
        }

        setup = true
    }


    @Test
    void oneMatchTest() {
        String psc = "841 05"

        List<PostalCode> cities = service.findAllByCode(psc)

        assertOneMatch(cities)
    }

    @Test
    @Disabled("Github action")
    void multipleMatchTest() {
        String psc = "851 10"

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
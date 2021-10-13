package com.netgrif.workflow.importer

import com.netgrif.workflow.petrinet.domain.PetriNet
import com.netgrif.workflow.petrinet.domain.VersionType
import com.netgrif.workflow.petrinet.domain.dataset.ChoiceField
import com.netgrif.workflow.petrinet.domain.throwable.MissingPetriNetMetaDataException
import com.netgrif.workflow.petrinet.service.interfaces.IPetriNetService
import com.netgrif.workflow.startup.ImportHelper
import com.netgrif.workflow.startup.SuperCreator
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.core.io.ClassPathResource
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit4.SpringRunner

@RunWith(SpringRunner.class)
@ActiveProfiles(["test"])
@SpringBootTest
class ImporterTestGroovy {

    @Autowired
    private ImportHelper importHelper

    @Autowired
    private IPetriNetService petriNetService

    @Autowired
    private SuperCreator superCreator

    public static final String FILE_NAME = "importer_upsert.xml"
    public static final String IDENTIFIER = "importer_upsert"

    private static final String ENUMERATION_FIELD = "enumeration"
    private static final String ENUMERATION_LIKE_MAP_FIELD = "enumeration_like_map"
    private static final String MULTICHOICE_FIELD = "multichoice"
    private static final String MULTICHOICE_LIKE_MAP_FIELD = "multichoice_like_map"

    @Test
    void upsertTest() {
        def net = importHelper.upsertNet(FILE_NAME, IDENTIFIER)
        assert net.present

        def upserted = importHelper.upsertNet(FILE_NAME, IDENTIFIER)
        assert upserted.present

        assert upserted.get().creationDate == net.get().creationDate
    }

    @Test
    void enumerationMultichoiceOptionsTest() throws IOException, MissingPetriNetMetaDataException {
        Optional<PetriNet> net = petriNetService.importPetriNet(new ClassPathResource("/enumeration_multichoice_options.xml").getInputStream(), VersionType.MAJOR, superCreator.getLoggedSuper())

        assert net.isPresent()
        ChoiceField multichoice = (ChoiceField) net.get().getDataSet().get(MULTICHOICE_FIELD)
        ChoiceField multichoice_like_map = (ChoiceField) net.get().getDataSet().get(MULTICHOICE_LIKE_MAP_FIELD)
        ChoiceField enumeration = (ChoiceField) net.get().getDataSet().get(ENUMERATION_FIELD)
        ChoiceField enumeration_like_map = (ChoiceField) net.get().getDataSet().get(ENUMERATION_LIKE_MAP_FIELD)

        assert multichoice.getChoices() == multichoice_like_map.getChoices()
        assert enumeration.getChoices() == enumeration_like_map.getChoices()

        assert multichoice.getValue() == multichoice_like_map.getValue()
        assert enumeration.getValue() == enumeration_like_map.getValue()

        assert multichoice.getDefaultValue() == multichoice_like_map.getDefaultValue()
        assert enumeration.getDefaultValue() == enumeration_like_map.getDefaultValue()
    }
}

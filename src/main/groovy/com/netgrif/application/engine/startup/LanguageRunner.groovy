package com.netgrif.application.engine.startup

import com.netgrif.application.engine.auth.service.interfaces.IUserService
import com.netgrif.application.engine.petrinet.domain.PetriNet
import com.netgrif.application.engine.petrinet.domain.VersionType
import com.netgrif.application.engine.petrinet.domain.dataset.EnumerationMapField
import com.netgrif.application.engine.petrinet.domain.dataset.Field
import com.netgrif.application.engine.petrinet.domain.dataset.TextField
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService
import com.netgrif.application.engine.workflow.domain.Case
import com.netgrif.application.engine.workflow.domain.QCase
import com.netgrif.application.engine.workflow.domain.QTask
import com.netgrif.application.engine.workflow.domain.Task
import com.netgrif.application.engine.workflow.service.interfaces.IDataService
import com.netgrif.application.engine.workflow.service.interfaces.ITaskService
import com.netgrif.application.engine.workflow.service.interfaces.IWorkflowService
import com.netgrif.application.engine.workflow.web.responsebodies.DataSet
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component

@Slf4j
@Component
@ConditionalOnProperty(value = "nae.language.enabled", matchIfMissing = true)
class LanguageRunner extends AbstractOrderedCommandLineRunner {

    @Autowired
    private IPetriNetService petriNetService

    @Autowired
    private ImportHelper helper

    @Autowired
    private SystemUserRunner systemCreator

    @Autowired
    private IWorkflowService workflowService

    @Autowired
    private IUserService userService

    @Autowired
    private ITaskService taskService

    @Autowired
    private IDataService dataService

    @Autowired
    private SuperCreator superCreator

    public static final String LANGUAGE_NET_IDENTIFIER = "language"
    private static final String LANGUAGE_FILE_NAME = "engine-processes/language.xml"
    private static final String CREATE_TRANSITION = "t1"
    private static final String LANGUAGE_FIELD_ID = "language"
    private static final String TRANSLATIONS_FIELD_ID = "translations"
    private static final String LANGUAGE_NAME_FIELD_ID = "languageName"
    private static final String LANGUAGE_SVG_ICON = "svgIcon"

    @Override
    void run(String... args) throws Exception {
        createLanguageNet()
        createLanguageCase("cz-CZ", "Čeština", '{ "toolbar": { "menu": {' +
                ' "lang": "Jazyk", "profile": "Profil", "logout": "Odhlásení",' +
                ' "open": "Otěvřít menu", "home": "Domov", "back": "Zpět", "folders": "Složky", "views": "Zobrazení" }}}',
        '<svg xmlns="http://www.w3.org/2000/svg" id="flag-icons-cz" viewBox="0 0 640 480"><path fill="#fff" d="M0 0h640v240H0z"/>' +
                '<path fill="#d7141a" d="M0 240h640v240H0z"/><path fill="#11457e" d="M360 240 0 0v480z"/></svg>')
    }

    Optional<PetriNet> createLanguageNet() {
        importProcess("Petri net for languages", LANGUAGE_NET_IDENTIFIER, LANGUAGE_FILE_NAME)
    }

    Optional<PetriNet> importProcess(String message, String netIdentifier, String netFileName) {
        PetriNet filter = petriNetService.getNewestVersionByIdentifier(netIdentifier)
        if (filter != null) {
            log.info("${message} has already been imported.")
            return Optional.of(filter)
        }
        Optional<PetriNet> net = helper.createNet(netFileName, VersionType.MAJOR, systemCreator.loggedSystem)
        if (!net.isPresent()) {
            log.error("Import of ${message} failed!")
        }
        return net
    }

    private Optional<Case> createLanguageCase(String languageKey, String languageName, String translations, String svgIcon) {
        PetriNet langNet = this.petriNetService.getNewestVersionByIdentifier(LANGUAGE_NET_IDENTIFIER)
        if (langNet == null) {
            return Optional.empty()
        }

        def loggedUser = this.userService.getLoggedOrSystem()

        Case langCase = this.workflowService.createCase(langNet.getStringId(), new StringBuilder(languageKey).append(' - ').append(languageName).toString() , null, loggedUser.transformToLoggedUser()).getCase()
        langCase = this.workflowService.save(langCase)
        Task newLangTask = this.taskService.searchOne(QTask.task.transitionId.eq(CREATE_TRANSITION).and(QTask.task.caseId.eq(langCase.getStringId())))
        this.taskService.assignTask(newLangTask, this.userService.getLoggedOrSystem())

        def setDataMap = [
                (LANGUAGE_FIELD_ID): [
                        "type": "text",
                        "value": languageKey
                ],
                (LANGUAGE_NAME_FIELD_ID): [
                        "type": "text",
                        "value": languageName
                ],
                (TRANSLATIONS_FIELD_ID): [
                        "type": "text",
                        "value": translations
                ],
                (LANGUAGE_SVG_ICON): [
                        "type": "text",
                        "value": svgIcon
                ],
        ]
        DataSet dataSet = new DataSet([
                (LANGUAGE_FIELD_ID): new TextField(rawValue: languageKey),
                (LANGUAGE_NAME_FIELD_ID): new TextField(rawValue: languageName),
                (TRANSLATIONS_FIELD_ID): new TextField(rawValue: translations),
                (LANGUAGE_SVG_ICON): new TextField(rawValue: svgIcon),
        ] as Map<String, Field<?>>)

        this.dataService.setData(newLangTask, dataSet, superCreator.getSuperUser())
        this.taskService.finishTask(newLangTask, this.userService.getLoggedOrSystem())
        return Optional.of(this.workflowService.findOne(langCase.getStringId()))
    }
}

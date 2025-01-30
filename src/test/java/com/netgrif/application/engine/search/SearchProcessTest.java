package com.netgrif.application.engine.search;

import com.netgrif.application.engine.TestHelper;
import com.netgrif.application.engine.petrinet.domain.PetriNet;
import com.netgrif.application.engine.petrinet.domain.VersionType;
import com.netgrif.application.engine.petrinet.domain.repositories.PetriNetRepository;
import com.netgrif.application.engine.search.interfaces.ISearchService;
import com.netgrif.application.engine.startup.ImportHelper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.netgrif.application.engine.search.SearchUtils.toDateTimeString;

@Slf4j
@SpringBootTest
@ActiveProfiles({"test"})
@ExtendWith(SpringExtension.class)
public class SearchProcessTest {
    @Autowired
    private PetriNetRepository petriNetRepository;

    @Autowired
    private ImportHelper importHelper;

    @Autowired
    private TestHelper testHelper;

    @Autowired
    private ISearchService searchService;

    @BeforeEach
    void setup() {
        testHelper.truncateDbs();
        List<String> idsToDelete = new ArrayList<>();
        idsToDelete.addAll(petriNetRepository.findAllByIdentifier("search_test").stream().map(PetriNet::getStringId).collect(Collectors.toList()));
        idsToDelete.addAll(petriNetRepository.findAllByIdentifier("search_test2").stream().map(PetriNet::getStringId).collect(Collectors.toList()));
        if (!idsToDelete.isEmpty()) {
            petriNetRepository.deleteAllById(idsToDelete);
        }
    }

    private PetriNet importPetriNet(String fileName, VersionType versionType) {
        PetriNet testNet = importHelper.createNet(fileName, versionType).orElse(null);
        assert testNet != null;
        return testNet;
    }

    private static PetriNet convertToPetriNet(Object petriNetObject) {
        assert petriNetObject instanceof PetriNet;
        return (PetriNet) petriNetObject;
    }

    private static List<PetriNet> convertToPetriNetList(Object petriNetListObject) {
        assert petriNetListObject instanceof List<?>;
        for (Object petriNetObject : (List<?>) petriNetListObject) {
            assert petriNetObject instanceof PetriNet;
        }

        return (List<PetriNet>) petriNetListObject;
    }

    private void comparePetriNets(PetriNet actual, PetriNet expected) {
        assert actual.getStringId().equals(expected.getStringId());
    }

    private void comparePetriNets(PetriNet actual, List<PetriNet> expected) {
        List<String> expectedStringIds = expected.stream().map(PetriNet::getStringId).collect(Collectors.toList());

        assert expectedStringIds.contains(actual.getStringId());
    }

    private void comparePetriNets(List<PetriNet> actual, List<PetriNet> expected) {
        List<String> actualStringIds = actual.stream().map(PetriNet::getStringId).collect(Collectors.toList());
        List<String> expectedStringIds = expected.stream().map(PetriNet::getStringId).collect(Collectors.toList());

        assert actualStringIds.containsAll(expectedStringIds);
    }

    @Test
    public void testSearchById() {
        PetriNet net = importPetriNet("search/search_test.xml", VersionType.MAJOR);

        String query = String.format("process: id eq '%s'", net.getStringId());

        long count = searchService.count(query);
        assert count == 1;

        Object process = searchService.search(query);

        comparePetriNets(convertToPetriNet(process), net);
    }

    @Test
    public void testSearchByIdentifier() {
        PetriNet net = importPetriNet("search/search_test.xml", VersionType.MAJOR);
        PetriNet netNewer = importPetriNet("search/search_test.xml", VersionType.MAJOR);
        PetriNet net2 = importPetriNet("search/search_test2.xml", VersionType.MAJOR);

        String query = String.format("process: identifier eq '%s'", net.getIdentifier());
        String queryMore = String.format("processes: identifier eq '%s'", net.getIdentifier());

        long count = searchService.count(query);
        assert count == 2;

        Object process = searchService.search(query);
        comparePetriNets(convertToPetriNet(process), List.of(net, netNewer));

        Object processes = searchService.search(queryMore);
        comparePetriNets(convertToPetriNetList(processes), List.of(net, netNewer));
    }

    @Test
    public void testSearchByVersion() {
        PetriNet net = importPetriNet("search/search_test.xml", VersionType.MAJOR);
        PetriNet netNewerPatch = importPetriNet("search/search_test.xml", VersionType.PATCH);
        PetriNet netNewerMinor = importPetriNet("search/search_test.xml", VersionType.MINOR);
        PetriNet netNewerMajor = importPetriNet("search/search_test.xml", VersionType.MAJOR);

        String queryEq = String.format("process: identifier eq '%s' and version eq %s", net.getIdentifier(), "1.0.0");
        String queryLt = String.format("processes: identifier eq '%s' and version lt %s", net.getIdentifier(), "2.0.0");
        String queryLte = String.format("processes: identifier eq '%s' and version lte %s", net.getIdentifier(), "2.0.0");
        String queryGt = String.format("processes: identifier eq '%s' and version gt %s", net.getIdentifier(), "1.0.0");
        String queryGte = String.format("processes: identifier eq '%s' and version gte %s", net.getIdentifier(), "1.0.0");

        long count = searchService.count(queryEq);
        assert count == 1;

        Object process = searchService.search(queryEq);
        comparePetriNets(convertToPetriNet(process), List.of(net));

        count = searchService.count(queryLt);
        assert count == 3;

        Object processes = searchService.search(queryLt);
        List<PetriNet> actual = convertToPetriNetList(processes);
        comparePetriNets(actual, List.of(net, netNewerPatch, netNewerMinor));
        assert !actual.stream().map(PetriNet::getStringId).collect(Collectors.toList()).contains(netNewerMajor.getStringId());

        count = searchService.count(queryLte);
        assert count == 4;

        processes = searchService.search(queryLte);
        comparePetriNets(convertToPetriNetList(processes), List.of(net, netNewerPatch, netNewerMinor, netNewerMajor));

        count = searchService.count(queryGt);
        assert count == 3;

        processes = searchService.search(queryGt);
        actual = convertToPetriNetList(processes);
        comparePetriNets(actual, List.of(netNewerPatch, netNewerMinor, netNewerMajor));
        assert !actual.stream().map(PetriNet::getStringId).collect(Collectors.toList()).contains(net.getStringId());

        count = searchService.count(queryGte);
        assert count == 4;

        processes = searchService.search(queryGte);
        comparePetriNets(convertToPetriNetList(processes), List.of(net, netNewerPatch, netNewerMinor, netNewerMajor));
    }

    @Test
    public void testSearchByTitle() {
        PetriNet net = importPetriNet("search/search_test.xml", VersionType.MAJOR);
        PetriNet netNewer = importPetriNet("search/search_test.xml", VersionType.MAJOR);
        PetriNet net2 = importPetriNet("search/search_test2.xml", VersionType.MAJOR);

        String query = String.format("process: title eq '%s'", net.getTitle().toString());
        String queryOther = String.format("process: title eq '%s'", net2.getTitle().toString());
        String queryMore = String.format("processes: title eq '%s'", net.getTitle().toString());

        long count = searchService.count(query);
        assert count == 2;

        Object process = searchService.search(query);
        comparePetriNets(convertToPetriNet(process), List.of(net, netNewer));

        count = searchService.count(queryOther);
        assert count == 1;

        process = searchService.search(queryOther);
        comparePetriNets(convertToPetriNet(process), net2);

        Object processes = searchService.search(queryMore);
        comparePetriNets(convertToPetriNetList(processes), List.of(net, netNewer));
    }

    @Test
    public void testSearchByCreationDate() {
        PetriNet net = importPetriNet("search/search_test.xml", VersionType.MAJOR);
        PetriNet netNewer = importPetriNet("search/search_test.xml", VersionType.MAJOR);
        PetriNet netNewest = importPetriNet("search/search_test.xml", VersionType.MAJOR);

        String queryEq = String.format("process: identifier eq '%s' and creationDate eq %s", net.getIdentifier(), toDateTimeString(net.getCreationDate()));
        String queryLt = String.format("processes: identifier eq '%s' and creationDate lt %s", net.getIdentifier(), toDateTimeString(netNewest.getCreationDate()));
        String queryLte = String.format("processes: identifier eq '%s' and creationDate lte %s", net.getIdentifier(), toDateTimeString(netNewest.getCreationDate()));
        String queryGt = String.format("processes: identifier eq '%s' and creationDate gt %s", net.getIdentifier(), toDateTimeString(net.getCreationDate()));
        String queryGte = String.format("processes: identifier eq '%s' and creationDate gte %s", net.getIdentifier(), toDateTimeString(net.getCreationDate()));

        long count = searchService.count(queryEq);
        assert count == 1;

        Object process = searchService.search(queryEq);
        comparePetriNets(convertToPetriNet(process), net);

        count = searchService.count(queryLt);
        assert count == 2;

        Object processes = searchService.search(queryLt);
        comparePetriNets(convertToPetriNetList(processes), List.of(net, netNewer));

        count = searchService.count(queryLte);
        assert count == 3;

        processes = searchService.search(queryLte);
        comparePetriNets(convertToPetriNetList(processes), List.of(net, netNewer, netNewest));

        count = searchService.count(queryGt);
        assert count == 2;

        processes = searchService.search(queryGt);
        comparePetriNets(convertToPetriNetList(processes), List.of(netNewer, netNewest));

        count = searchService.count(queryGte);
        assert count == 3;

        processes = searchService.search(queryGte);
        comparePetriNets(convertToPetriNetList(processes), List.of(net, netNewer, netNewest));
    }

}

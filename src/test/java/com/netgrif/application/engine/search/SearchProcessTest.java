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

import static com.netgrif.application.engine.search.utils.SearchTestUtils.*;
import static com.netgrif.application.engine.search.utils.SearchUtils.toDateTimeString;

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

    private void searchAndCompare(String query, PetriNet expected) {
        long count = searchService.count(query);
        assert count == 1;

        Object actual = searchService.search(query);
        compareById(convertToObject(actual, PetriNet.class), expected, PetriNet::getStringId);
    }

    private void searchAndCompare(String query, List<PetriNet> expected) {
        long count = searchService.count(query);
        assert count == expected.size();

        Object actual = searchService.search(query);
        compareById(convertToObject(actual, PetriNet.class), expected, PetriNet::getStringId);
    }

    private void searchAndCompareAsList(String query, List<PetriNet> expected) {
        long count = searchService.count(query);
        assert count == expected.size();

        Object actual = searchService.search(query);
        compareById(convertToObjectList(actual, PetriNet.class), expected, PetriNet::getStringId);
    }

    private void searchAndCompareAsListInOrder(String query, List<PetriNet> expected) {
        long count = searchService.count(query);
        assert count == expected.size();

        Object actual = searchService.search(query);
        compareByIdInOrder(convertToObjectList(actual, PetriNet.class), expected, PetriNet::getStringId);
    }

    @Test
    public void testSearchById() {
        PetriNet net = importPetriNet("search/search_test.xml", VersionType.MAJOR);
        PetriNet net2 = importPetriNet("search/search_test.xml", VersionType.MAJOR);

        String query = String.format("process: id eq '%s'", net.getStringId());

        searchAndCompare(query, net);

        // in list
        String queryInList = String.format("processes: id in ('%s', '%s')", net.getStringId(), net2.getStringId());

        searchAndCompareAsList(queryInList, List.of(net, net2));

        // sort
        query = String.format("processes: identifier eq '%s' sort by id", net.getIdentifier());
        searchAndCompareAsListInOrder(query, List.of(net, net2));

        query = String.format("processes: identifier eq '%s' sort by id desc", net.getIdentifier());
        searchAndCompareAsListInOrder(query, List.of(net2, net));
    }

    @Test
    public void testSearchByIdentifier() {
        PetriNet net = importPetriNet("search/search_test.xml", VersionType.MAJOR);
        PetriNet netNewer = importPetriNet("search/search_test.xml", VersionType.MAJOR);
        PetriNet net2 = importPetriNet("search/search_test2.xml", VersionType.MAJOR);
        PetriNet net3 = importPetriNet("search/search_test3.xml", VersionType.MAJOR);

        String query = String.format("process: identifier eq '%s'", net.getIdentifier());
        String queryMore = String.format("processes: identifier eq '%s'", net.getIdentifier());

        searchAndCompare(query, List.of(net, netNewer));
        searchAndCompareAsList(queryMore, List.of(net, netNewer));

        // in list
        String queryInList = String.format("processes: identifier in ('%s', '%s', '%s')", net.getIdentifier(), net2.getIdentifier(), net3.getIdentifier());
        searchAndCompareAsList(queryInList, List.of(net, netNewer, net2, net3));

        // in range
        String queryInRange = String.format("processes: identifier in ['%s' : '%s')", net.getIdentifier(), net3.getIdentifier());
        searchAndCompareAsList(queryInRange, List.of(net, netNewer, net2));

        // sort
        queryMore = String.format("processes: identifier in ('%s', '%s') sort by identifier", net.getIdentifier(), net2.getIdentifier());
        searchAndCompareAsListInOrder(queryMore, List.of(net, netNewer, net2));

        queryMore = String.format("processes: identifier in ('%s', '%s') sort by identifier desc", net.getIdentifier(), net2.getIdentifier());
        searchAndCompareAsListInOrder(queryMore, List.of(net2, net, netNewer));
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

        searchAndCompare(queryEq, net);
        searchAndCompareAsList(queryLt, List.of(net, netNewerPatch, netNewerMinor));
        searchAndCompareAsList(queryLte, List.of(net, netNewerPatch, netNewerMinor, netNewerMajor));
        searchAndCompareAsList(queryGt, List.of(netNewerPatch, netNewerMinor, netNewerMajor));
        searchAndCompareAsList(queryGte, List.of(net, netNewerPatch, netNewerMinor, netNewerMajor));

        // in list
        String queryInList = String.format("processes: identifier eq '%s' and version in (%s, %s, %s)", net.getIdentifier(), "1.0.0", "1.0.1", "1.1.0");
        searchAndCompareAsList(queryInList, List.of(net, netNewerPatch, netNewerMinor));

        // in range
        String queryInRange = String.format("processes: identifier eq '%s' and version in [%s : %s)", net.getIdentifier(), "1.0.0", "1.1.0");
        searchAndCompareAsList(queryInRange, List.of(net, netNewerPatch));

        // sort
        String query = String.format("processes: identifier eq '%s' sort by version", net.getIdentifier());
        searchAndCompareAsListInOrder(query, List.of(net, netNewerPatch, netNewerMinor, netNewerMajor));

        query = String.format("processes: identifier eq '%s' sort by version desc", net.getIdentifier());
        searchAndCompareAsListInOrder(query, List.of(netNewerMajor, netNewerMinor, netNewerPatch, net));
    }

    @Test
    public void testSearchByTitle() {
        PetriNet net = importPetriNet("search/search_test.xml", VersionType.MAJOR);
        PetriNet netNewer = importPetriNet("search/search_test.xml", VersionType.MAJOR);
        PetriNet net2 = importPetriNet("search/search_test2.xml", VersionType.MAJOR);

        String query = String.format("process: title eq '%s'", net.getTitle().toString());
        String queryOther = String.format("process: title eq '%s'", net2.getTitle().toString());
        String queryMore = String.format("processes: title eq '%s'", net.getTitle().toString());

        searchAndCompare(query, List.of(net, netNewer));
        searchAndCompare(queryOther, net2);
        searchAndCompareAsList(queryMore, List.of(net, netNewer));

        // in list
        String queryInList = String.format("processes: identifier in ('%s', '%s') and title in ('%s', '%s')", net.getIdentifier(), net2.getIdentifier(), net.getTitle().getDefaultValue(), net2.getTitle().getDefaultValue());
        searchAndCompareAsList(queryInList, List.of(net, netNewer, net2));

        // in range
        String queryInRange = String.format("processes: identifier in ('%s', '%s') and title in ['%s' : '%s')", net.getIdentifier(), net2.getIdentifier(), net.getTitle().getDefaultValue(), net2.getTitle().getDefaultValue());
        searchAndCompareAsList(queryInRange, List.of(net, netNewer));

        // sort
        queryMore = String.format("processes: identifier in ('%s', '%s') sort by title", net.getIdentifier(), net2.getIdentifier());
        searchAndCompareAsListInOrder(queryMore, List.of(net, netNewer, net2));

        queryMore = String.format("processes: identifier in ('%s', '%s') sort by title desc", net.getIdentifier(), net2.getIdentifier());
        searchAndCompareAsListInOrder(queryMore, List.of(net2, net, netNewer));
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

        searchAndCompare(queryEq, net);
        searchAndCompareAsList(queryLt, List.of(net, netNewer));
        searchAndCompareAsList(queryLte, List.of(net, netNewer, netNewest));
        searchAndCompareAsList(queryGt, List.of(netNewer, netNewest));
        searchAndCompareAsList(queryGte, List.of(net, netNewer, netNewest));

        // in list
        String queryInList = String.format("processes: identifier eq '%s' and creationDate in (%s, %s)", net.getIdentifier(), toDateTimeString(net.getCreationDate()), toDateTimeString(netNewest.getCreationDate()));
        searchAndCompareAsList(queryInList, List.of(net, netNewest));

        // in range
        String queryInRange = String.format("processes: identifier eq '%s' and creationDate in [%s : %s)", net.getIdentifier(), toDateTimeString(net.getCreationDate()), toDateTimeString(netNewest.getCreationDate()));
        searchAndCompareAsList(queryInRange, List.of(net, netNewer));

        // sort
        String query = String.format("processes: identifier eq '%s' sort by creationDate", net.getIdentifier());
        searchAndCompareAsListInOrder(query, List.of(net, netNewer, netNewest));

        query = String.format("processes: identifier eq '%s' sort by creationDate desc", net.getIdentifier());
        searchAndCompareAsListInOrder(query, List.of(netNewest, netNewer, net));
    }

    @Test
    public void testPagination() {
        List<PetriNet> nets = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            nets.add(importPetriNet("search/search_test.xml", VersionType.MAJOR));
        }

        String queryOne = String.format("process: identifier eq '%s'", "search_test");
        String queryMore = String.format("processes: identifier eq '%s'", "search_test");
        String queryMoreCustomPagination = String.format("processes: identifier eq '%s' page 1 size 5", "search_test");

        long count = searchService.count(queryOne);
        assert count == 50;

        Object process = searchService.search(queryOne);
        compareById(convertToObject(process, PetriNet.class), nets.get(0), PetriNet::getStringId);

        Object processes = searchService.search(queryMore);
        compareById(convertToObjectList(processes, PetriNet.class), nets.subList(0, 19), PetriNet::getStringId);

        processes = searchService.search(queryMoreCustomPagination);
        compareById(convertToObjectList(processes, PetriNet.class), nets.subList(5, 9), PetriNet::getStringId);
    }

}

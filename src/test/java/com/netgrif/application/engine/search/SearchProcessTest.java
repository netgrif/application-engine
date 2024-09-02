package com.netgrif.application.engine.search;

import com.netgrif.application.engine.TestHelper;
import com.netgrif.application.engine.petrinet.domain.PetriNet;
import com.netgrif.application.engine.petrinet.domain.VersionType;
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

import java.util.List;

import static com.netgrif.application.engine.search.SearchUtils.toDateTimeString;

@Slf4j
@SpringBootTest
@ActiveProfiles({"test"})
@ExtendWith(SpringExtension.class)
public class SearchProcessTest {

    @Autowired
    private ImportHelper importHelper;

    @Autowired
    private TestHelper testHelper;

    @Autowired
    private ISearchService searchService;

    @BeforeEach
    void setup() {
        testHelper.truncateDbs();
    }

    private PetriNet importPetriNet(String fileName, VersionType versionType) {
        PetriNet testNet = importHelper.createNet(fileName, versionType).orElse(null);
        assert testNet != null;
        return testNet;
    }

    @Test
    public void testSearchById() {
        PetriNet net = importPetriNet("search/search_test.xml", VersionType.MAJOR);
        PetriNet net2 = importPetriNet("search/search_test2.xml", VersionType.MAJOR);

        String query = String.format("process: id eq '%s'", net.getStringId());

        long count = searchService.count(query);
        assert count == 1;

        Object process = searchService.search(query);

        assert process instanceof PetriNet;
        assert process.equals(net);
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
        assert process instanceof PetriNet;
        assert process.equals(net) || process.equals(netNewer);

        Object processes = searchService.search(queryMore);
        assert processes instanceof List;
        assert ((List<PetriNet>) processes).containsAll(List.of(net, netNewer));
    }

    @Test
    public void testSearchByVersion() {
        PetriNet net = importPetriNet("search/search_test.xml", VersionType.MAJOR);
        PetriNet netNewerPatch = importPetriNet("search/search_test.xml", VersionType.PATCH);
        PetriNet netNewerMinor = importPetriNet("search/search_test.xml", VersionType.MINOR);
        PetriNet netNewerMajor = importPetriNet("search/search_test.xml", VersionType.MAJOR);
        PetriNet net2 = importPetriNet("search/search_test2.xml", VersionType.MAJOR);

        String queryEq = String.format("process: version eq '%s'", "1.0.0");
        String queryLt = String.format("processes: version lt '%s'", "2.0.0");
        String queryLte = String.format("processes: version lte '%s'", "2.0.0");
        String queryGt = String.format("processes: version gt '%s'", "1.0.0");
        String queryGte = String.format("processes: version gt '%s'", "1.0.0");

        long count = searchService.count(queryEq);
        assert count == 1;

        Object process = searchService.search(queryEq);
        assert process instanceof PetriNet;
        assert process.equals(net);

        count = searchService.count(queryLt);
        assert count == 3;

        Object processes = searchService.search(queryLt);
        assert processes instanceof List;
        assert ((List<PetriNet>) processes).containsAll(List.of(net, netNewerPatch, netNewerMinor));
        assert !((List<PetriNet>) processes).contains(netNewerMajor);

        count = searchService.count(queryLte);
        assert count == 4;

        processes = searchService.search(queryLte);
        assert processes instanceof List;
        assert ((List<PetriNet>) processes).containsAll(List.of(net, netNewerPatch, netNewerMinor, netNewerMajor));

        count = searchService.count(queryGt);
        assert count == 3;

        processes = searchService.search(queryGt);
        assert processes instanceof List;
        assert ((List<PetriNet>) processes).containsAll(List.of(netNewerPatch, netNewerMinor, netNewerMajor));
        assert !((List<PetriNet>) processes).contains(net);

        count = searchService.count(queryGte);
        assert count == 4;

        processes = searchService.search(queryGte);
        assert processes instanceof List;
        assert ((List<PetriNet>) processes).containsAll(List.of(net, netNewerPatch, netNewerMinor, netNewerMajor));
    }

    @Test
    public void testSearchByTitle() {
        PetriNet net = importPetriNet("search/search_test.xml", VersionType.MAJOR);
        PetriNet netNewer = importPetriNet("search/search_test.xml", VersionType.MAJOR);
        PetriNet net2 = importPetriNet("search/search_test2.xml", VersionType.MAJOR);

        String query = String.format("process: title eq '%s'", net.getTitle().toString());
        String queryMore = String.format("processes: title eq '%s'", net.getTitle().toString());

        long count = searchService.count(query);
        assert count == 2;

        Object process = searchService.search(query);
        assert process instanceof PetriNet;
        assert process.equals(net) || process.equals(netNewer);

        Object processes = searchService.search(queryMore);
        assert processes instanceof List;
        assert ((List<PetriNet>) processes).containsAll(List.of(net, netNewer));
    }

    @Test
    public void testSearchByCreationDate() {
        PetriNet net = importPetriNet("search/search_test.xml", VersionType.MAJOR);
        PetriNet netNewer = importPetriNet("search/search_test.xml", VersionType.MAJOR);
        PetriNet net2 = importPetriNet("search/search_test2.xml", VersionType.MAJOR);

        String queryEq = String.format("process: creationDate eq '%s'", toDateTimeString(net.getCreationDate()));
        String queryLt = String.format("processes: creationDate lt '%s'", toDateTimeString(net2.getCreationDate()));
        String queryLte = String.format("processes: creationDate lte '%s'", toDateTimeString(net2.getCreationDate()));
        String queryGt = String.format("processes: creationDate gt '%s'", toDateTimeString(net.getCreationDate()));
        String queryGte = String.format("processes: creationDate gte '%s'", toDateTimeString(net.getCreationDate()));

        long count = searchService.count(queryEq);
        assert count == 1;

        Object process = searchService.search(queryEq);
        assert process instanceof PetriNet;
        assert process.equals(net);

        count = searchService.count(queryLt);
        assert count == 2;

        Object processes = searchService.search(queryLt);
        assert processes instanceof List;
        assert ((List<PetriNet>) processes).containsAll(List.of(net, netNewer));

        count = searchService.count(queryLte);
        assert count == 2;

        processes = searchService.search(queryLte);
        assert processes instanceof List;
        assert ((List<PetriNet>) processes).containsAll(List.of(net, netNewer, net2));

        count = searchService.count(queryGt);
        assert count == 2;

        processes = searchService.search(queryGt);
        assert processes instanceof List;
        assert ((List<PetriNet>) processes).containsAll(List.of(netNewer, net2));

        count = searchService.count(queryGte);
        assert count == 2;

        processes = searchService.search(queryGte);
        assert processes instanceof List;
        assert ((List<PetriNet>) processes).containsAll(List.of(net, netNewer, net2));
    }

}

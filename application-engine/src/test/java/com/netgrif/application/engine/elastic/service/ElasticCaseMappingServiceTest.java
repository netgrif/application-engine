package com.netgrif.application.engine.elastic.service;

import com.netgrif.application.engine.objects.auth.domain.ActorRef;
import com.netgrif.application.engine.objects.elastic.domain.ElasticCase;
import com.netgrif.application.engine.objects.petrinet.domain.PetriNet;
import com.netgrif.application.engine.objects.workflow.domain.ProcessResourceId;
import com.netgrif.application.engine.objects.petrinet.domain.dataset.TextField;
import com.netgrif.application.engine.objects.workflow.domain.Case;
import com.netgrif.application.engine.objects.workflow.domain.DataField;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.elasticsearch.core.convert.MappingElasticsearchConverter;
import org.springframework.data.elasticsearch.core.document.Document;
import org.springframework.data.elasticsearch.core.mapping.SimpleElasticsearchMappingContext;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ElasticCaseMappingServiceTest {

    private ElasticCaseMappingService mappingService;
    private MappingElasticsearchConverter converter;

    @BeforeEach
    void setUp() {
        mappingService = new ElasticCaseMappingService();
        SimpleElasticsearchMappingContext mappingContext = new SimpleElasticsearchMappingContext();
        converter = new MappingElasticsearchConverter(mappingContext);
    }

    private Case mockCase(boolean partial, Set<String> immediateFields, Map<String, DataField> dataSet) {
        Case c = mock(Case.class);
        when(c.getStringId()).thenReturn("case-1");
        when(c.get_id()).thenReturn(new ProcessResourceId("process", new ObjectId()));
        when(c.getPetriNetId()).thenReturn("process");
        when(c.getProcessIdentifier()).thenReturn("process");
        when(c.getVisualId()).thenReturn("CASE-1");
        when(c.getTitle()).thenReturn("Test Case");
        when(c.getLastModified()).thenReturn(LocalDateTime.now());
        when(c.getCreationDate()).thenReturn(LocalDateTime.now());
        when(c.getAuthor()).thenReturn(new ActorRef("user-1", "netgrif", "Test User", "testuser"));
        when(c.getImmediateDataFields()).thenReturn(immediateFields);
        when(c.isPartiallyLoaded()).thenReturn(partial);
        when(c.getDataSet()).thenReturn(dataSet);
        when(c.getTasks()).thenReturn(Collections.emptySet());
        when(c.getEnabledRoles()).thenReturn(Collections.emptySet());
        when(c.getViewRoles()).thenReturn(Collections.emptyList());
        when(c.getViewActorRefs()).thenReturn(Collections.emptyList());
        when(c.getNegativeViewRoles()).thenReturn(Collections.emptyList());
        when(c.getViewActors()).thenReturn(Collections.emptyList());
        when(c.getNegativeViewActors()).thenReturn(Collections.emptyList());
        when(c.getTags()).thenReturn(Collections.emptyMap());
        when(c.getPermissions()).thenReturn(Collections.emptyMap());
        when(c.getActors()).thenReturn(Collections.emptyMap());
        when(c.getActorRefs()).thenReturn(Collections.emptyMap());
        when(c.getImmediateData()).thenReturn(Collections.emptyList());

        for (String fieldId : immediateFields) {
            TextField netField = new TextField();
            netField.setImportId(fieldId);
            doReturn(netField).when(c).getField(fieldId);
            if (dataSet != null && dataSet.containsKey(fieldId)) {
                when(c.getDataField(fieldId)).thenReturn(dataSet.get(fieldId));
            }
        }
        return c;
    }

    @Test
    void transformFullCaseIncludesOnlyNonNullImmediateFields() {
        Set<String> immediateFields = new LinkedHashSet<>(List.of("field_1", "field_2"));
        Map<String, DataField> dataSet = new HashMap<>();
        dataSet.put("field_1", new DataField("Hello"));
        dataSet.put("field_2", new DataField(null));

        Case fullCase = mockCase(false, immediateFields, dataSet);

        ElasticCase result = mappingService.transform(fullCase);

        assertNotNull(result);
        assertTrue(result.getDataSet().containsKey("field_1"));
        assertNotNull(result.getDataSet().get("field_1"));
        assertFalse(result.getDataSet().containsKey("field_2"));
    }

    @Test
    void transformPartialCaseIncludesChangedAndExplicitlyClearedFields() {
        Set<String> immediateFields = new LinkedHashSet<>(List.of("field_1", "field_2", "field_3"));
        Map<String, DataField> partialDataSet = new HashMap<>();
        partialDataSet.put("field_1", new DataField("Updated Value"));
        partialDataSet.put("field_2", new DataField(null)); // explicitly cleared

        Case partialCase = mockCase(true, immediateFields, partialDataSet);

        ElasticCase result = mappingService.transform(partialCase);

        assertNotNull(result);
        // field_1 should be updated with new value
        assertTrue(result.getDataSet().containsKey("field_1"));
        assertNotNull(result.getDataSet().get("field_1"));

        // field_2 was in partial case and cleared, so it should be mapped as null
        assertTrue(result.getDataSet().containsKey("field_2"));
        assertNull(result.getDataSet().get("field_2"));

        // field_3 was NOT in partial case (unloaded), so it should not be in dataSet at all
        assertFalse(result.getDataSet().containsKey("field_3"));

        // Verify that MappingElasticsearchConverter properly preserves null for field_2 and omits field_3
        Document doc = converter.mapObject(result);
        assertNotNull(doc);
        Map<?, ?> docDataSet = (Map<?, ?>) doc.get("dataSet");
        assertNotNull(docDataSet);
        assertTrue(docDataSet.containsKey("field_1"));
        assertNotNull(docDataSet.get("field_1"));
        assertTrue(docDataSet.containsKey("field_2"));
        assertNull(docDataSet.get("field_2"));
        assertFalse(docDataSet.containsKey("field_3"));
    }
}

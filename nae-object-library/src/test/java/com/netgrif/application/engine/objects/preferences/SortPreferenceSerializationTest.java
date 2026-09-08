package com.netgrif.application.engine.objects.preferences;

import com.netgrif.application.engine.objects.utils.Serializer;
import org.junit.jupiter.api.Test;

import java.io.Serial;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SortPreferenceSerializationTest {

    @Test
    void serializesPreferencesWithNonEmptySorts() {
        SortPreference sort = new SortPreference();
        sort.setHeaderUniqueId("meta-title");
        sort.setSortDirection("asc");
        TestPreferences preferences = new TestPreferences("68b079480000000000000001");
        preferences.setSorts(Map.of("case-view", List.of(sort)));

        TestPreferences deserialized = (TestPreferences) Serializer.deserialize(Serializer.serialize(preferences));

        assertEquals("meta-title", deserialized.getSorts().get("case-view").getFirst().getHeaderUniqueId());
        assertEquals("asc", deserialized.getSorts().get("case-view").getFirst().getSortDirection());
    }

    private static final class TestPreferences extends Preferences {

        @Serial
        private static final long serialVersionUID = 1L;

        private TestPreferences(String userId) {
            super(userId);
        }
    }
}

package com.netgrif.workflow.elastic.web;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@JsonDeserialize(using = CaseSearchRequestWrapper.SingleItemAsListDeserializer.class)
public class CaseSearchRequestWrapper {

    public List<CaseSearchRequest> list;

    public CaseSearchRequestWrapper() {
        list = new ArrayList<>();
    }

    public CaseSearchRequestWrapper(List<CaseSearchRequest> requests) {
        list = requests;
    }


    public static class SingleItemAsListDeserializer extends StdDeserializer<CaseSearchRequestWrapper> {

        protected SingleItemAsListDeserializer() {
            this(null);
        }

        protected SingleItemAsListDeserializer(Class<?> vc) {
            super(vc);
        }

        @Override
        public CaseSearchRequestWrapper deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
            CaseSearchRequestWrapper wrapper;

            ObjectMapper innerDeserializer = new ObjectMapper();
            JsonNode node = innerDeserializer.readTree(jsonParser);
            try {
                CaseSearchRequest request = innerDeserializer.convertValue(node, new TypeReference<CaseSearchRequest>() {
                });
                wrapper = new CaseSearchRequestWrapper();
                wrapper.getList().add(request);
            } catch (IllegalArgumentException e) {
                // parsing of single item failed
                List<CaseSearchRequest> requests = innerDeserializer.convertValue(node, new TypeReference<List<CaseSearchRequest>>() {
                });
                wrapper = new CaseSearchRequestWrapper(requests);
            }

            return wrapper;
        }
    }
}

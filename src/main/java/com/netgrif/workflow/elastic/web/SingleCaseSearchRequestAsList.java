package com.netgrif.workflow.elastic.web;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import lombok.Getter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Getter
@JsonDeserialize(using = SingleCaseSearchRequestAsList.SingleItemAsListDeserializer.class)
public class SingleCaseSearchRequestAsList {

    private List<CaseSearchRequest> list;

    public SingleCaseSearchRequestAsList() {
        list = new ArrayList<>();
    }

    public SingleCaseSearchRequestAsList(CaseSearchRequest item) {
        this();
        this.list.add(item);
    }

    public SingleCaseSearchRequestAsList(List<CaseSearchRequest> requests) {
        list = requests;
    }


    public static class SingleItemAsListDeserializer extends StdDeserializer<SingleCaseSearchRequestAsList> {

        protected SingleItemAsListDeserializer() {
            this(null);
        }

        protected SingleItemAsListDeserializer(Class<?> vc) {
            super(vc);
        }

        @Override
        public SingleCaseSearchRequestAsList deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, IllegalArgumentException {
            SingleCaseSearchRequestAsList wrapper;

            ObjectMapper innerDeserializer = new ObjectMapper();
            JsonNode node = innerDeserializer.readTree(jsonParser);
            try {
                CaseSearchRequest request = innerDeserializer.convertValue(node, new TypeReference<CaseSearchRequest>() {});
                wrapper = new SingleCaseSearchRequestAsList(request);
            } catch (IllegalArgumentException singleItemException) {
                try {
                    List<CaseSearchRequest> requests = innerDeserializer.convertValue(node, new TypeReference<List<CaseSearchRequest>>() {});
                    wrapper = new SingleCaseSearchRequestAsList(requests);
                } catch (IllegalArgumentException arrayException) {
                    if(node.isArray())
                        throw new IllegalArgumentException("Single item as list deserialization failed. List deserialization exception: " + arrayException.getMessage(), arrayException);
                    else
                        throw new IllegalArgumentException("Single item as list deserialization failed. Single item deserialization exception: " + singleItemException.getMessage(), singleItemException);
                }
            }

            return wrapper;
        }
    }
}

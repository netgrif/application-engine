package com.netgrif.application.engine.petrinet.domain;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Map;

@Data
@Document
public class Place extends Node {

    private Integer tokens;
    // TODO: release/8.0.0 unique key map
    // TODO: release/8.0.0 move to node?
    private Map<String, String> properties;

    public Place() {
        super();
    }

    public void addTokens(int tokens) {
        this.tokens += tokens;
    }

    public void removeTokens(Integer tokens) throws IllegalArgumentException {
        if (this.tokens - tokens < 0) {
            throw new IllegalArgumentException("Place can not have negative number of tokens.");
        }
        this.tokens -= tokens;
    }

    public void removeAllTokens() {
        this.tokens = 0;
    }

    public boolean hasAnyTokens() {
        return tokens > 0;
    }

    @Override
    public String toString() {
        return getTitle() + " (" + tokens + ")";
    }

    public Place clone() {
        Place clone = new Place();
        clone.setTokens(this.tokens);
        clone.setTitle(this.getTitle());
        clone.setImportId(this.getImportId());
        return clone;
    }
}
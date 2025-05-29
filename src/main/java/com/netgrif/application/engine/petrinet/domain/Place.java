package com.netgrif.application.engine.petrinet.domain;

import com.netgrif.application.engine.petrinet.domain.throwable.IllegalMarkingException;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@Document
public class Place extends Node {

    private int tokens;


    public void addTokens(int tokens) {
        this.tokens += tokens;
    }

    public void removeTokens(int tokens) throws IllegalArgumentException {
        if (this.tokens - tokens < 0) {
            throw new IllegalMarkingException(this);
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
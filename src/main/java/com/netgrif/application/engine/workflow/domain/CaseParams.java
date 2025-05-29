package com.netgrif.application.engine.workflow.domain;

import com.netgrif.application.engine.workflow.web.responsebodies.DataSet;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.annotation.Nullable;
import java.util.Map;

import static org.junit.Assert.assertNotNull;

@Getter
@NoArgsConstructor
public abstract class CaseParams {

    protected Map<String, String> properties;

    public CaseParams(@Nullable Map<String, String> properties) {
        this.properties = properties;
    }

    public void addProperty(String key, String value) {
        if (this.properties == null) {
            return;
        }
        assertNotNull(key);
        this.properties.put(key, value);
    }

    public void removeProperty(String key) {
        if (this.properties == null) {
            return;
        }
        this.properties.remove(key);
    }

    public String getProperty(String key) {
        if (this.properties == null) {
            return null;
        }
        return this.properties.get(key);
    }

    /**
     * todo javadoc
     * */
    public abstract DataSet toDataSet();

    public abstract String targetProcessIdentifier();
}

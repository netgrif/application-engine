package com.netgrif.workflow.workflow.web.requestbodies;


import com.netgrif.workflow.utils.SingleItemAsList;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public abstract class CreateFilterBody<T extends SingleItemAsList<?>> {

    private String title;
    private int visibility;
    private String description;
    private String type;
    private T query;

    public List<?> getQuery() {
        return query.getList();
    }
}

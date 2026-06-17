package com.netgrif.application.engine.objects.workflow.domain;

import java.util.Collection;
import java.util.Iterator;

public interface DataFieldsCollection<T> {
    Iterator<T> iterator();
    Collection<T> getContent();
}

package com.netgrif.application.engine.elastic.service;


import com.netgrif.application.engine.elastic.domain.DataField;
import com.netgrif.application.engine.elastic.domain.ElasticCase;
import com.netgrif.application.engine.elastic.service.interfaces.IElasticCaseMappingService;
import com.netgrif.application.engine.elastic.service.transform.ElasticDataFieldTransformer;
import com.netgrif.application.engine.importer.model.DataType;
import com.netgrif.application.engine.petrinet.domain.dataset.Field;
import com.netgrif.application.engine.workflow.domain.Case;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ElasticCaseMappingService implements IElasticCaseMappingService {

    private final Map<DataType, ElasticDataFieldTransformer<? extends Field<?>, ? extends DataField>> transformers;

    public ElasticCaseMappingService(@Autowired List<ElasticDataFieldTransformer<? extends Field<?>, ? extends DataField>> transformers) {
        this.transformers = transformers.stream().collect(Collectors.toMap(ElasticDataFieldTransformer::getType, Function.identity()));
    }

    @Override
    public ElasticCase transform(Case useCase) {
        ElasticCase transformedCase = new ElasticCase(useCase);
        this.populateDataSet(transformedCase, useCase);
        return transformedCase;
    }

    protected void populateDataSet(ElasticCase transformedCase, Case useCase) {
        for (String id : useCase.getImmediateDataFields()) {
            Optional<DataField> parsedValue = this.transformDataField(id, useCase);
            parsedValue.ifPresent(dataField -> transformedCase.getDataSet().put(id, dataField));
        }
    }

    protected Optional<DataField> transformDataField(String fieldId, Case useCase) {
        Field<?> netField = useCase.getDataSet().get(fieldId);
        Field<?> caseField = useCase.getDataSet().get(fieldId);
        if (caseField.getValue() == null) {
            return Optional.empty();
        }

        ElasticDataFieldTransformer<Field<?>, ?> transformer = (ElasticDataFieldTransformer<Field<?>, ?>) transformers.get(netField.getType());
        if (transformer == null) {
            log.error("Field " + netField.getImportId() + " has unsupported type " + netField.getType());
            return Optional.empty();
        }
        return Optional.ofNullable(transformer.transform(caseField, netField));
    }
}

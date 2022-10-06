package com.netgrif.application.engine.workflow.domain.repositories;

import com.netgrif.application.engine.auth.domain.LoggedUser;
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService;
import com.netgrif.application.engine.petrinet.web.responsebodies.PetriNetReference;
import com.netgrif.application.engine.petrinet.web.responsebodies.Reference;
import com.netgrif.application.engine.workflow.domain.QCase;
import com.netgrif.application.engine.workflow.domain.QDataField;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.StringExpression;
import com.querydsl.core.types.dsl.StringPath;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.querydsl.binding.QuerydslBindings;
import org.springframework.data.querydsl.binding.SingleValueBinding;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Repository("caseRepository")
public abstract class CaseRepositoryImpl implements CaseRepository {

    @Autowired
    private IPetriNetService petriNetService;

    @Override
    public void customize(QuerydslBindings bindings, QCase qCase) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        List<PetriNetReference> nets = petriNetService.getReferencesByUsersProcessRoles(((LoggedUser) auth.getPrincipal()).getSelfOrImpersonated(), null);
        Set<String> netIds = nets.stream().map(Reference::getStringId).collect(Collectors.toSet());
        Set<String> netIdentifiers = nets.stream().map(PetriNetReference::getIdentifier).collect(Collectors.toSet());

        bindings.bind(qCase.petriNetId).first((stringPath, s) -> {
            if (!netIds.contains(s))
                return Expressions.asBoolean(false);
            return stringPath.equalsIgnoreCase(s);
        });
        bindings.bind(qCase.processIdentifier).first((path, string) -> {
            if (!netIdentifiers.contains(string))
                return Expressions.asBoolean(false);
            return path.equalsIgnoreCase(string);
        });
        bindings.bind(String.class).first((SingleValueBinding<StringPath, String>) StringExpression::equalsIgnoreCase);
        bindings.bind(qCase.dataSet).first((path, map) ->
                map.entrySet().stream()
                        .map(o -> {
                            QDataField field = qCase.dataSet.get(o.getKey());
                            if (field == null || o.getValue() == null || o.getValue().getValue() == null)
                                return Expressions.asBoolean(false);
                            return field.value.eq(o.getValue().getValue().toString());
                        })
                        .reduce(BooleanExpression::and).get());
        bindings.bind(qCase.title).first(StringExpression::likeIgnoreCase);
    }
}
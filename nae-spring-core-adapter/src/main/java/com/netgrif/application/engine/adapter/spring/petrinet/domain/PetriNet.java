package com.netgrif.application.engine.adapter.spring.petrinet.domain;

import com.netgrif.application.engine.objects.petrinet.domain.Place;
import com.netgrif.application.engine.objects.petrinet.domain.Transaction;
import com.netgrif.application.engine.objects.petrinet.domain.Transition;
import com.netgrif.application.engine.objects.petrinet.domain.arcs.Arc;
import com.netgrif.application.engine.objects.petrinet.domain.dataset.Field;
import com.netgrif.application.engine.objects.petrinet.domain.roles.ProcessRole;
import com.querydsl.core.annotations.QueryEntity;
import lombok.Getter;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.*;

@Getter
@Document
@QueryEntity
public class PetriNet extends com.netgrif.application.engine.objects.petrinet.domain.PetriNet {


    public PetriNet() { super(); }

    public PetriNet(PetriNet petriNet) {
        super(petriNet);
    }

    @org.springframework.data.mongodb.core.mapping.Field("places")
    @Override
    public LinkedHashMap<String, Place> getPlaces() {
        return super.getPlaces();
    };

    @org.springframework.data.mongodb.core.mapping.Field("transitions")
    @Override
    public LinkedHashMap<String, Transition> getTransitions() {
        return super.getTransitions();
    }

    @org.springframework.data.mongodb.core.mapping.Field("arcs")
    @Override
    public LinkedHashMap<String, List<Arc>> getArcs() {
        return super.getArcs();
    }

    @org.springframework.data.mongodb.core.mapping.Field("dataset")
    @Override
    public LinkedHashMap<String, Field> getDataSet() {
        return super.getDataSet();
    }

    @org.springframework.data.mongodb.core.mapping.Field("transactions")
    @Override
    public LinkedHashMap<String, Transaction> getTransactions() {
        return super.getTransactions();
    }

    @org.springframework.data.mongodb.core.mapping.Field("roles")
    @Override
    public LinkedHashMap<String, ProcessRole> getRoles() {
        return super.getRoles();
    }

    // todo: delete clone method if not needed
//    @Override
//    public PetriNet clone() {
//        PetriNet clone = new PetriNet();
//        clone.setIdentifier(this.getIdentifier());
//        clone.setUriNodeId(this.getUriNodeId());
//        clone.setInitials(this.getInitials());
//        clone.setTitle(this.getTitle().clone());
//        clone.setDefaultRoleEnabled(this.isDefaultRoleEnabled());
//        clone.setDefaultCaseName(this.getDefaultCaseName() == null ? null : this.getDefaultCaseName().clone());
//        clone.setDefaultCaseNameExpression(this.getDefaultCaseNameExpression() == null ? null : this.getDefaultCaseNameExpression().clone());
//        clone.setIcon(this.getIcon());
//        clone.setCreationDate(this.getCreationDate());
//        clone.setVersion(this.getVersion() == null ? null : this.getVersion().clone());
//        clone.setAuthor(this.getAuthor() == null ? null : this.getAuthor().clone());
//        clone.setTransitions(this.getTransitions() == null ? null : this.getTransitions().entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> new com.netgrif.application.engine.adapter.spring.petrinet.domain.Transition(e.getValue()), (v1, v2) -> v1, LinkedHashMap::new)));
//        clone.setRoles(this.roles == null ? null : this.roles.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> new com.netgrif.application.engine.adapter.spring.petrinet.domain.roles.ProcessRole(e.getValue()), (v1, v2) -> v1, LinkedHashMap::new)));
//        clone.setTransactions(this.getTransactions() == null ? null : this.getTransactions().entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> new com.netgrif.application.engine.adapter.spring.petrinet.domain.Transaction(e.getValue()), (v1, v2) -> v1, LinkedHashMap::new)));
//        clone.setImportXmlPath(this.getImportXmlPath());
//        clone.setImportId(this.importId);
//        clone.setObjectId(this._id);
//        clone.setDataSet(this.getDataSet().entrySet()
//                .stream()
//                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().clone(), (x, y)->y, LinkedHashMap::new))
//        );
//        clone.setPlaces(this.getPlaces().entrySet()
//                .stream()
//                .collect(Collectors.toMap(Map.Entry::getKey, e -> new Place(e.getValue()), (x, y)->y, LinkedHashMap::new))
//        );
//        clone.setArcs(this.getArcs().entrySet()
//                .stream()
//                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().stream()
//                        .map(Arc::clone)
//                        .collect(Collectors.toList()), (x,y)->y, LinkedHashMap::new))
//        );
//        clone.initializeArcs();
//        clone.setCaseEvents(this.getCaseEvents() == null ? null : this.getCaseEvents().entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().clone())));
//        clone.setProcessEvents(this.getProcessEvents() == null ? null : this.getProcessEvents().entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().clone())));
//        clone.setPermissions(this.getPermissions() == null ? null : this.getPermissions().entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> new HashMap<>(e.getValue()))));
//        clone.setUserRefs(this.getUserRefs() == null ? null : this.getUserRefs().entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> new HashMap<>(e.getValue()))));
//        this.getNegativeViewRoles().forEach(clone::addNegativeViewRole);
//        this.getFunctions().forEach(clone::addFunction);
//        clone.setTags(new HashMap<>(this.getTags()));
//        return clone;
//    }
}

//package com.netgrif.application.engine.history.domain.baseevent;
//
//import com.netgrif.core.event.events.Event;
//import com.netgrif.core.petrinet.domain.events.EventPhase;
//import com.netgrif.core.workflow.domain.ProcessResourceId;
//import lombok.Getter;
//import lombok.Setter;
//import org.bson.types.ObjectId;
//import org.springframework.data.annotation.Id;
//import org.springframework.data.elasticsearch.annotations.Document;
//
//
//import java.io.Serial;
//import java.io.Serializable;
//import java.util.List;
//
////TODO
//@Document(indexName = "#{@eventLogIndex}")
//public abstract class EventLog implements Serializable {
//
//    @Serial
//    private static final long serialVersionUID = 7650412902302046885L;
//
//    @Id
//    @Getter
//    protected ObjectId id;
//
//    @Getter
//    protected ProcessResourceId triggerId;
//
//    @Getter
//    protected EventPhase eventPhase;
//
//    @Getter
//    @Setter
//    protected List<ObjectId> triggeredEvents;
//
//    protected EventLog() {
//        this.id = new ObjectId();
//    }
//
//    protected EventLog(ProcessResourceId triggerId, EventPhase eventPhase) {
//        this();
//        this.triggerId = triggerId;
//        this.eventPhase = eventPhase;
//    }
//
//    protected EventLog(ProcessResourceId triggerId, EventPhase eventPhase, List<ObjectId> triggeredEvents) {
//        this(triggerId, eventPhase);
//        this.triggeredEvents = triggeredEvents;
//    }
//
//    public String getStringId() {
//        return id.toString();
//    }
//}

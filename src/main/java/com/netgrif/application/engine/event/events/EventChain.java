package com.netgrif.application.engine.event.events;

import lombok.Getter;
import lombok.Setter;
import org.bson.types.ObjectId;

import java.util.*;


public class EventChain {

    @Getter
    private final ObjectId id;

    @Getter
    @Setter
    private int length = 100;

    @Getter
    private int wipeAfter = 100 * length;

    private final Set<Class<?>> allowedClass;

    @Getter
    private final List<EventObject> events = new ArrayList<>(length);

    private EventChain child;

    private EventChain parent;

    private ObjectId parentId;

    private boolean hasChild;


    public EventChain() {
        this.id = new ObjectId();
        this.allowedClass = new HashSet<>();
    }

    public EventChain(Set<Class<?>> allowedClass) {
        this.id = new ObjectId();
        this.allowedClass = allowedClass;
    }

    private EventChain(EventObject event, Set<Class<?>> allowedClass, EventChain parent) {
        this.id = new ObjectId();
        this.events.add(event);
        this.allowedClass = allowedClass;
        this.parent = parent;
        this.parentId = parent.getId();
    }

    public <E extends EventObject> void add(E event, Class<?> clazz) {
        if (!allowedClass.contains(clazz)) {
            throw new IllegalArgumentException("Class " + clazz.getName() + " is not allowed to add event " + event);
        }
        if (events.size() >= length) {
            if (!hasChild) {
                setChild(new EventChain(event, this.allowedClass, parent));
            } else {
                getChild().add(event, clazz);
            }
        }
        events.add(event);
    }

    public int size() {
        return events.size();
    }

    public void removeParent() {
        this.parent = null;
    }

    public void setChild(EventChain child) {
        this.child = child;
        hasChild = true;
    }

    public EventChain getChild() {
        if (this.child == null) {
            return null;
        }
        return this.child;
    }

    public EventChain getParent() {
        if (this.parent == null) {
            return null;
        }
        return this.parent;
    }

    public EventChain next() {
        return getChild();
    }

    public EventChain prev() {
        return getParent();
    }

    public boolean hasChild() {
        return hasChild;
    }

    public void wipe() {
        events.clear();
    }

    public void setWipeAfter(int wipeAfter) {
        if (wipeAfter % this.length == 0) {
            this.wipeAfter = wipeAfter;
        }
    }

    public void wipeAllWithChildren() {
        EventChain eventChain = getChild();
        while (eventChain != null) {
            eventChain.wipe();
            eventChain = next();
        }
    }

    private EventChain getRoot() {
        EventChain parent = getParent();
        while (parent != null) {
            if (parent.getParent() == null) {
                return parent;
            }
            parent = parent.getParent();
        }
        return parent;
    }

    public EventChain wipeNum(int num) {
        //todo upravit, lebo sa nachadzaju prazdne miesta, zatial premazavat iba cely eventChain
        for (int i = 0; i < num; i++) {
            events.removeFirst();
            if (events.isEmpty()) {
                EventChain child = getChild();
                if (child != null) {
                    child.wipeNum(i);
                    child.removeParent();
                    return child;
                }
                return this;
            }
        }
        return this;
    }

    public EventChain wipeFromRoot(int num) {
        return getRoot().wipeNum(num);
    }

}

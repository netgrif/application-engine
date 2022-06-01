package api.events;

/**
 * Interface for event dispatcher
 * */
public interface IDispatcher {

    /**
     * Returns ID of dispatcher
     * */
    String getId();

    /**
     * Registers ISubscriber object to dispatcher
     * @param subscriber a subscriber object from plugin
     * */
    void registerSubscriber(ISubscriber subscriber);

    /**
     * Listener function for a type of event
     * @param eventClass 
     * */
    <T> void listen(Class<T> eventClass, T event);
    void listen(IEvent event);
}

package api.events;

import java.util.List;

/**
 * Interface for the event subscriber
 * */
public interface ISubscriber {

   /**
    * Returns dispatcher IDs, where the subscriber needs to be subscribed
    * @return list of IDs
    * */
   List<String> getRequiredDispatcherIds();

   /**
    * Subscribes the subscriber to dispatchers.
    * */
   void subscribeToDispatchers();

   /**
    * Subscribes the subscriber to one dispatcher.
    * */
   void subscribeToDispatcher(IDispatcher dispatcher);

   /**
    * Listener function that is called, when an event has been caught in dispatcher
    * @param event the event object that is being sent to subscriber
    * */
   Object onEvent(IEvent event);

   /**
    * Listener function that is called, when an event has been caught in dispatcher
    * @param eventClass the type of the event object
    * @param event the event object
    * */
   <T> void onEvent(Class<T> eventClass, T event);
}

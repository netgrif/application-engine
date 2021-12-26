# Event Queue

The NAE frontend library implements an event queue (`EventQueueService`) that assures a serial execution of task related
events.

Events performed by the following services are managed by the queue:

* `AssignTaskService`
* `CancelTaskService`
* `DelegateTaskService`
* `FinishTaskService`
* `TaskDataService`

These services correspond to the `assign`, `cancel`, `delegate`, `finish`, `setData` and `getData` task events.

A queued event consists of 3 parts:

* a condition that determines whether the event can execute
* the execution of the event
* the cancellation of the event

When a scheduled event gets to the front of the queue and is executed its condition is evaluated first. The condition
should check if the event can be performed in the current state of the application and its associated task. Examples of
such conditions include:

* Is the task we wanted to cancel still assigned to somebody?
* Is the data field we wanted to set still editable?

If the condition evaluates to **true**, the event will be executed in accordance to the provided callback.
An `AfterAction` is passed to the callback, which signals to the queue, that the next event can be executed. If you want
to schedule your own events into the queue, don’t forget to resolve the provided `AfterAction`. Otherwise, the queue
will get stuck and no more task events will be executed in the entire application!

If the condition evaluates to **false**, the effects of the event that remain present on the frontend should be cleared.
For example:

* A `setData` event fails the condition, because the associated data field was made `visible` by a previous event.
  However, the value set by the user still remains on the frontend. The revert operation changes the value of
  the `visible` field to the last known backend value.
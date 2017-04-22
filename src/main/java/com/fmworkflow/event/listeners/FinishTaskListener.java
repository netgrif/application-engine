package com.fmworkflow.event.listeners;

import com.fmworkflow.event.events.UserFinishTaskEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class FinishTaskListener {

    @EventListener
    public void onUserEvent(UserFinishTaskEvent event) {
    }
}
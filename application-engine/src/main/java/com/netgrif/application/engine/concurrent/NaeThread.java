package com.netgrif.application.engine.concurrent;

import org.springframework.security.concurrent.DelegatingSecurityContextRunnable;

public class NaeThread extends Thread {

    // todo javadoc

    public NaeThread(Runnable runnable) {
        super(new DelegatingSecurityContextRunnable(runnable));
    }

    public NaeThread(Runnable runnable, String name) {
        super(new DelegatingSecurityContextRunnable(runnable), name);
    }

}

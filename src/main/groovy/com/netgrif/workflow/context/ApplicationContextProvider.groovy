package com.netgrif.workflow.context

import org.springframework.beans.BeansException
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware

class ApplicationContextProvider implements ApplicationContextAware {

    private static ApplicationContext context

    @Override
    void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        context = applicationContext
    }

    static ApplicationContext getAppContext() {
        return context
    }

    static def getBean(String name) {
        return context.getBean(name)
    }
}
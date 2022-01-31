package com.netgrif.application.engine.configuration

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

    static def getBean(Class aClass) {
        return context.getBean(aClass)
    }
}
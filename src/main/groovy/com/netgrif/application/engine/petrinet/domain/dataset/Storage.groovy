//package com.netgrif.application.engine.petrinet.domain.dataset
//
//import com.netgrif.application.engine.files.local.LocalStorageService
//import com.querydsl.core.annotations.PropertyType
//import com.querydsl.core.annotations.QueryType
//import org.codehaus.groovy.runtime.metaclass.MetaClassRegistryImpl
//
//class Storage implements Serializable {
//    @Serial
//    static final long serialVersionUID = 9172755427378929926L
//    private String type
//    private String host
//
//    Storage() {
//        this.type = LocalStorageService.LOCAL_TYPE
//    }
//
//    Storage(String type) {
//        this()
//        this.type = type
//    }
//
//    Storage(String type, String host) {
//        this(type)
//        this.host = host
//    }
//
//    String getType() {
//        return type
//    }
//
//    void setType(String type) {
//        this.type = type
//    }
//
//    String getHost() {
//        return host
//    }
//
//    void setHost(String host) {
//        this.host = host
//    }
//
//    @Override
//    @QueryType(PropertyType.NONE)
//    MetaClass getMetaClass() {
//        return this.metaClass != null ? this.metaClass  : ((MetaClassRegistryImpl) GroovySystem.getMetaClassRegistry()).getMetaClass(this)
//    }
//}

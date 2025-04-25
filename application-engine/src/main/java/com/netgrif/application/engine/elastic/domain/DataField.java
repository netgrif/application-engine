//package com.netgrif.application.engine.elastic.domain;
//
//import lombok.AllArgsConstructor;
//import lombok.Data;
//import lombok.EqualsAndHashCode;
//import lombok.NoArgsConstructor;
//import org.springframework.data.elasticsearch.annotations.Field;
//
//import java.io.Serial;
//import java.io.Serializable;
//
//import static org.springframework.data.elasticsearch.annotations.FieldType.Text;
//
//@Data
//@NoArgsConstructor
//@EqualsAndHashCode
//@AllArgsConstructor
//public class DataField implements Serializable {
//
//    @Serial
//    private static final long serialVersionUID = 2035013102812591274L;
//
//    @Field(type = Text)
//    public String[] fulltextValue;
//
//    DataField(String fulltextValue) {
//        this.fulltextValue = new String[1];
//        this.fulltextValue[0] = fulltextValue;
//    }
//}
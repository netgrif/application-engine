package com.netgrif.workflow.quartz.domain;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Objects;

@Entity
@Table(name = "QRTZ_SIMPROP_TRIGGERS")
@IdClass(QrtzSimpropTriggersPK.class)
public class QrtzSimpropTriggers {
    private String schedName;
    private String triggerName;
    private String triggerGroup;
    private String strProp1;
    private String strProp2;
    private String strProp3;
    private Integer intProp1;
    private Integer intProp2;
    private Long longProp1;
    private Long longProp2;
    private BigDecimal decProp1;
    private BigDecimal decProp2;
    private String boolProp1;
    private String boolProp2;

    @Id
    @Column(name = "SCHED_NAME", length = 120)
    public String getSchedName() {
        return schedName;
    }

    public void setSchedName(String schedName) {
        this.schedName = schedName;
    }

    @Id
    @Column(name = "TRIGGER_NAME", length = 190)
    public String getTriggerName() {
        return triggerName;
    }

    public void setTriggerName(String triggerName) {
        this.triggerName = triggerName;
    }

    @Id
    @Column(name = "TRIGGER_GROUP", length = 190)
    public String getTriggerGroup() {
        return triggerGroup;
    }

    public void setTriggerGroup(String triggerGroup) {
        this.triggerGroup = triggerGroup;
    }

    @Basic
    @Column(name = "STR_PROP_1", length = 512)
    public String getStrProp1() {
        return strProp1;
    }

    public void setStrProp1(String strProp1) {
        this.strProp1 = strProp1;
    }

    @Basic
    @Column(name = "STR_PROP_2", length = 512)
    public String getStrProp2() {
        return strProp2;
    }

    public void setStrProp2(String strProp2) {
        this.strProp2 = strProp2;
    }

    @Basic
    @Column(name = "STR_PROP_3", length = 512)
    public String getStrProp3() {
        return strProp3;
    }

    public void setStrProp3(String strProp3) {
        this.strProp3 = strProp3;
    }

    @Basic
    @Column(name = "INT_PROP_1")
    public Integer getIntProp1() {
        return intProp1;
    }

    public void setIntProp1(Integer intProp1) {
        this.intProp1 = intProp1;
    }

    @Basic
    @Column(name = "INT_PROP_2")
    public Integer getIntProp2() {
        return intProp2;
    }

    public void setIntProp2(Integer intProp2) {
        this.intProp2 = intProp2;
    }

    @Basic
    @Column(name = "LONG_PROP_1")
    public Long getLongProp1() {
        return longProp1;
    }

    public void setLongProp1(Long longProp1) {
        this.longProp1 = longProp1;
    }

    @Basic
    @Column(name = "LONG_PROP_2")
    public Long getLongProp2() {
        return longProp2;
    }

    public void setLongProp2(Long longProp2) {
        this.longProp2 = longProp2;
    }

    @Basic
    @Column(name = "DEC_PROP_1")
    public BigDecimal getDecProp1() {
        return decProp1;
    }

    public void setDecProp1(BigDecimal decProp1) {
        this.decProp1 = decProp1;
    }

    @Basic
    @Column(name = "DEC_PROP_2")
    public BigDecimal getDecProp2() {
        return decProp2;
    }

    public void setDecProp2(BigDecimal decProp2) {
        this.decProp2 = decProp2;
    }

    @Basic
    @Column(name = "BOOL_PROP_1", length = 1)
    public String getBoolProp1() {
        return boolProp1;
    }

    public void setBoolProp1(String boolProp1) {
        this.boolProp1 = boolProp1;
    }

    @Basic
    @Column(name = "BOOL_PROP_2", length = 1)
    public String getBoolProp2() {
        return boolProp2;
    }

    public void setBoolProp2(String boolProp2) {
        this.boolProp2 = boolProp2;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        QrtzSimpropTriggers that = (QrtzSimpropTriggers) o;
        return Objects.equals(schedName, that.schedName) &&
                Objects.equals(triggerName, that.triggerName) &&
                Objects.equals(triggerGroup, that.triggerGroup) &&
                Objects.equals(strProp1, that.strProp1) &&
                Objects.equals(strProp2, that.strProp2) &&
                Objects.equals(strProp3, that.strProp3) &&
                Objects.equals(intProp1, that.intProp1) &&
                Objects.equals(intProp2, that.intProp2) &&
                Objects.equals(longProp1, that.longProp1) &&
                Objects.equals(longProp2, that.longProp2) &&
                Objects.equals(decProp1, that.decProp1) &&
                Objects.equals(decProp2, that.decProp2) &&
                Objects.equals(boolProp1, that.boolProp1) &&
                Objects.equals(boolProp2, that.boolProp2);
    }

    @Override
    public int hashCode() {
        return Objects.hash(schedName, triggerName, triggerGroup, strProp1, strProp2, strProp3, intProp1, intProp2, longProp1, longProp2, decProp1, decProp2, boolProp1, boolProp2);
    }
}

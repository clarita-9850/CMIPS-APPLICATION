package com.cmips.entity;

import jakarta.persistence.*;

/**
 * Hourly Task Guidelines Entity - Reference/lookup table.
 *
 * Stores the min/max guideline values for each service type, functional area,
 * and functional rank combination. Used to validate and display HTG ranges
 * during IHSS service assessment. No audit fields needed.
 */
@Entity
@Table(name = "hourly_task_guidelines", indexes = {
        @Index(name = "idx_htg_lookup", columnList = "service_type_code, func_area_code, func_rank")
})
public class HourlyTaskGuidelinesEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "service_type_code", length = 10)
    private String serviceTypeCode;

    @Column(name = "func_area_code", length = 10)
    private String funcAreaCode;

    @Column(name = "func_rank", length = 10)
    private String funcRank;

    @Column(name = "min_value")
    private Integer minValue;

    @Column(name = "max_value")
    private Integer maxValue;

    @Column(name = "other_ind")
    private Boolean otherInd;

    // ── Getters and Setters ──────────────────────────────────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getServiceTypeCode() { return serviceTypeCode; }
    public void setServiceTypeCode(String serviceTypeCode) { this.serviceTypeCode = serviceTypeCode; }

    public String getFuncAreaCode() { return funcAreaCode; }
    public void setFuncAreaCode(String funcAreaCode) { this.funcAreaCode = funcAreaCode; }

    public String getFuncRank() { return funcRank; }
    public void setFuncRank(String funcRank) { this.funcRank = funcRank; }

    public Integer getMinValue() { return minValue; }
    public void setMinValue(Integer minValue) { this.minValue = minValue; }

    public Integer getMaxValue() { return maxValue; }
    public void setMaxValue(Integer maxValue) { this.maxValue = maxValue; }

    public Boolean getOtherInd() { return otherInd; }
    public void setOtherInd(Boolean otherInd) { this.otherInd = otherInd; }
}

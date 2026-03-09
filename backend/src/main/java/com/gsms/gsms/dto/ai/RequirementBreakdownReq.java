package com.gsms.gsms.dto.ai;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Min;
import javax.validation.constraints.Max;
import javax.validation.constraints.Size;

/**
 * 需求拆分请求
 */
public class RequirementBreakdownReq {

    /**
     * 需求描述
     */
    @NotBlank(message = "需求描述不能为空")
    @Size(max = 5000, message = "需求描述不能超过5000字符")
    private String requirement;

    /**
     * 项目类型（可选，用于更准确的估算）
     */
    private String projectType;

    /**
     * 团队规模（必填，用于更准确的估算）
     */
    @NotNull(message = "团队规模不能为空")
    @Min(value = 1, message = "团队规模至少为1人")
    @Max(value = 50, message = "团队规模最多为50人")
    private Integer teamSize;

    /**
     * 期望完成时间（必填，单位：天）
     */
    @NotNull(message = "期望完成时间不能为空")
    @Min(value = 1, message = "期望完成时间至少为1天")
    @Max(value = 365, message = "期望完成时间最多为365天")
    private Integer expectedDays;

    /**
     * 期望工时（必填，单位：小时）
     */
    @NotNull(message = "期望工时不能为空")
    @Min(value = 1, message = "期望工时至少为1小时")
    @Max(value = 10000, message = "期望工时最多为10000小时")
    private Integer estimateHours;

    public String getRequirement() {
        return requirement;
    }

    public void setRequirement(String requirement) {
        this.requirement = requirement;
    }

    public String getProjectType() {
        return projectType;
    }

    public void setProjectType(String projectType) {
        this.projectType = projectType;
    }

    public Integer getTeamSize() {
        return teamSize;
    }

    public void setTeamSize(Integer teamSize) {
        this.teamSize = teamSize;
    }

    public Integer getExpectedDays() {
        return expectedDays;
    }

    public void setExpectedDays(Integer expectedDays) {
        this.expectedDays = expectedDays;
    }

    public Integer getEstimateHours() {
        return estimateHours;
    }

    public void setEstimateHours(Integer estimateHours) {
        this.estimateHours = estimateHours;
    }
}

package com.gsms.gsms.dto.ai;

import javax.validation.constraints.NotBlank;
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
     * 团队规模（可选，用于更准确的估算）
     */
    private Integer teamSize;

    /**
     * 期望完成时间（可选，单位：天）
     */
    private Integer expectedDays;

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
}

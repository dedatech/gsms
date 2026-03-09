package com.gsms.gsms.dto.ai;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * 需求拆分响应
 */
public class RequirementBreakdownResp {

    /**
     * 需求概述
     */
    @JsonProperty("summary")
    private String summary;

    /**
     * 子任务列表
     */
    @JsonProperty("subTasks")
    private List<SubTaskEstimate> subTasks;

    /**
     * 总预估人天
     */
    @JsonProperty("totalEstimatedDays")
    private Double totalEstimatedDays;

    /**
     * 建议团队规模
     */
    @JsonProperty("suggestedTeamSize")
    private Integer suggestedTeamSize;

    /**
     * 建议迭代周期（单位：天）
     */
    @JsonProperty("suggestedIterationDays")
    private Integer suggestedIterationDays;

    /**
     * 风险提示
     */
    @JsonProperty("risks")
    private List<String> risks;

    /**
     * 技术建议
     */
    @JsonProperty("suggestions")
    private List<String> suggestions;

    /**
     * AI 分析的备注信息
     */
    @JsonProperty("notes")
    private String notes;

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public List<SubTaskEstimate> getSubTasks() {
        return subTasks;
    }

    public void setSubTasks(List<SubTaskEstimate> subTasks) {
        this.subTasks = subTasks;
    }

    public Double getTotalEstimatedDays() {
        return totalEstimatedDays;
    }

    public void setTotalEstimatedDays(Double totalEstimatedDays) {
        this.totalEstimatedDays = totalEstimatedDays;
    }

    public Integer getSuggestedTeamSize() {
        return suggestedTeamSize;
    }

    public void setSuggestedTeamSize(Integer suggestedTeamSize) {
        this.suggestedTeamSize = suggestedTeamSize;
    }

    public Integer getSuggestedIterationDays() {
        return suggestedIterationDays;
    }

    public void setSuggestedIterationDays(Integer suggestedIterationDays) {
        this.suggestedIterationDays = suggestedIterationDays;
    }

    public List<String> getRisks() {
        return risks;
    }

    public void setRisks(List<String> risks) {
        this.risks = risks;
    }

    public List<String> getSuggestions() {
        return suggestions;
    }

    public void setSuggestions(List<String> suggestions) {
        this.suggestions = suggestions;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}

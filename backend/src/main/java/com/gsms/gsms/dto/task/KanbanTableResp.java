package com.gsms.gsms.dto.task;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * 看板表格数据响应DTO
 */
@Schema(description = "看板表格数据")
public class KanbanTableResp {

    @Schema(description = "需求行列表")
    private List<RequirementRow> rows;

    @Schema(description = "待办状态任务总数")
    private Integer totalTodoTasks;

    @Schema(description = "进行中状态任务总数")
    private Integer totalInProgressTasks;

    @Schema(description = "待验证状态任务总数（缺陷）")
    private Integer totalTestingTasks;

    @Schema(description = "已完成状态任务总数")
    private Integer totalDoneTasks;

    @Schema(description = "重新打开状态任务总数（缺陷）")
    private Integer totalReopenedTasks;

    @Schema(description = "已关闭状态任务总数")
    private Integer totalClosedTasks;

    /**
     * 需求行数据
     */
    @Schema(description = "需求行数据")
    public static class RequirementRow {

        @Schema(description = "需求信息")
        private RequirementStatsResp requirement;

        @Schema(description = "待办任务列表")
        private List<TaskInfoResp> todoTasks;

        @Schema(description = "进行中任务列表")
        private List<TaskInfoResp> inProgressTasks;

        @Schema(description = "待验证任务列表（缺陷）")
        private List<TaskInfoResp> testingTasks;

        @Schema(description = "已完成任务列表")
        private List<TaskInfoResp> doneTasks;

        @Schema(description = "重新打开任务列表（缺陷）")
        private List<TaskInfoResp> reopenedTasks;

        @Schema(description = "已关闭任务列表")
        private List<TaskInfoResp> closedTasks;

        // Getters and Setters
        public RequirementStatsResp getRequirement() {
            return requirement;
        }

        public void setRequirement(RequirementStatsResp requirement) {
            this.requirement = requirement;
        }

        public List<TaskInfoResp> getTodoTasks() {
            return todoTasks;
        }

        public void setTodoTasks(List<TaskInfoResp> todoTasks) {
            this.todoTasks = todoTasks;
        }

        public List<TaskInfoResp> getInProgressTasks() {
            return inProgressTasks;
        }

        public void setInProgressTasks(List<TaskInfoResp> inProgressTasks) {
            this.inProgressTasks = inProgressTasks;
        }

        public List<TaskInfoResp> getTestingTasks() {
            return testingTasks;
        }

        public void setTestingTasks(List<TaskInfoResp> testingTasks) {
            this.testingTasks = testingTasks;
        }

        public List<TaskInfoResp> getDoneTasks() {
            return doneTasks;
        }

        public void setDoneTasks(List<TaskInfoResp> doneTasks) {
            this.doneTasks = doneTasks;
        }

        public List<TaskInfoResp> getReopenedTasks() {
            return reopenedTasks;
        }

        public void setReopenedTasks(List<TaskInfoResp> reopenedTasks) {
            this.reopenedTasks = reopenedTasks;
        }

        public List<TaskInfoResp> getClosedTasks() {
            return closedTasks;
        }

        public void setClosedTasks(List<TaskInfoResp> closedTasks) {
            this.closedTasks = closedTasks;
        }
    }

    // Getters and Setters
    public List<RequirementRow> getRows() {
        return rows;
    }

    public void setRows(List<RequirementRow> rows) {
        this.rows = rows;
    }

    public Integer getTotalTodoTasks() {
        return totalTodoTasks;
    }

    public void setTotalTodoTasks(Integer totalTodoTasks) {
        this.totalTodoTasks = totalTodoTasks;
    }

    public Integer getTotalInProgressTasks() {
        return totalInProgressTasks;
    }

    public void setTotalInProgressTasks(Integer totalInProgressTasks) {
        this.totalInProgressTasks = totalInProgressTasks;
    }

    public Integer getTotalTestingTasks() {
        return totalTestingTasks;
    }

    public void setTotalTestingTasks(Integer totalTestingTasks) {
        this.totalTestingTasks = totalTestingTasks;
    }

    public Integer getTotalDoneTasks() {
        return totalDoneTasks;
    }

    public void setTotalDoneTasks(Integer totalDoneTasks) {
        this.totalDoneTasks = totalDoneTasks;
    }

    public Integer getTotalReopenedTasks() {
        return totalReopenedTasks;
    }

    public void setTotalReopenedTasks(Integer totalReopenedTasks) {
        this.totalReopenedTasks = totalReopenedTasks;
    }

    public Integer getTotalClosedTasks() {
        return totalClosedTasks;
    }

    public void setTotalClosedTasks(Integer totalClosedTasks) {
        this.totalClosedTasks = totalClosedTasks;
    }
}

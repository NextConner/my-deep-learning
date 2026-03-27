package com.jtcool.common.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 导出任务领域模型
 */
public class ExportJob implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 任务ID (UUID) */
    private String jobId;

    /** 用户ID */
    private Long userId;

    /** 导出类型 (如 "OmsOrder", "WmsInventory") */
    private String exportType;

    /** 任务状态 */
    private ExportStatus status;

    /** 进度 (0-100) */
    private Integer progress;

    /** 文件路径 */
    private String filePath;

    /** 文件名 */
    private String fileName;

    /** 总记录数 */
    private Long totalRecords;

    /** 已处理记录数 */
    private Long processedRecords;

    /** 错误信息 */
    private String errorMessage;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdTime;

    /** 完成时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime completedTime;

    public ExportJob() {
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getExportType() {
        return exportType;
    }

    public void setExportType(String exportType) {
        this.exportType = exportType;
    }

    public ExportStatus getStatus() {
        return status;
    }

    public void setStatus(ExportStatus status) {
        this.status = status;
    }

    public Integer getProgress() {
        return progress;
    }

    public void setProgress(Integer progress) {
        this.progress = progress;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Long getTotalRecords() {
        return totalRecords;
    }

    public void setTotalRecords(Long totalRecords) {
        this.totalRecords = totalRecords;
    }

    public Long getProcessedRecords() {
        return processedRecords;
    }

    public void setProcessedRecords(Long processedRecords) {
        this.processedRecords = processedRecords;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public LocalDateTime getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(LocalDateTime createdTime) {
        this.createdTime = createdTime;
    }

    public LocalDateTime getCompletedTime() {
        return completedTime;
    }

    public void setCompletedTime(LocalDateTime completedTime) {
        this.completedTime = completedTime;
    }
}

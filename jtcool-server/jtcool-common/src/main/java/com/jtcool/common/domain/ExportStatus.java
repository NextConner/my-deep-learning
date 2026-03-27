package com.jtcool.common.domain;

/**
 * 导出任务状态枚举
 */
public enum ExportStatus {
    /** 等待中 */
    PENDING,

    /** 处理中 */
    PROCESSING,

    /** 已完成 */
    COMPLETED,

    /** 失败 */
    FAILED
}

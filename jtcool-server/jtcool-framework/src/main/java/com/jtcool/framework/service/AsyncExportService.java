package com.jtcool.framework.service;

import com.jtcool.common.domain.ExportStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.Semaphore;

/**
 * 异步导出执行服务
 */
@Service
public class AsyncExportService {

    private static final Logger log = LoggerFactory.getLogger(AsyncExportService.class);

    @Autowired
    private ExportJobService exportJobService;

    @Value("${jtcool.profile}")
    private String downloadPath;

    /** 最多 20 个并发导出 */
    private final Semaphore exportSemaphore = new Semaphore(20);

    /**
     * 异步执行导出任务
     */
    @Async
    public void executeExport(String jobId, String exportType, Object queryParams) {
        if (!exportSemaphore.tryAcquire()) {
            exportJobService.failJob(jobId, "导出任务过多，请稍后重试");
            log.warn("导出任务过多，拒绝任务: jobId={}", jobId);
            return;
        }

        try {
            log.info("开始执行导出任务: jobId={}, exportType={}", jobId, exportType);
            exportJobService.updateStatus(jobId, ExportStatus.PROCESSING);

            // 生成文件路径（永久保留）
            String filePath = generateFilePath(jobId, exportType);

            // TODO: 实际的导出逻辑将在 Phase 3 实现
            // 这里先创建一个占位实现
            simulateExport(jobId, filePath);

            exportJobService.completeJob(jobId, filePath);
            log.info("导出任务完成: jobId={}, filePath={}", jobId, filePath);

        } catch (Exception e) {
            log.error("导出任务失败: jobId={}", jobId, e);
            exportJobService.failJob(jobId, e.getMessage());
        } finally {
            exportSemaphore.release();
        }
    }

    /**
     * 生成文件路径
     * 格式: {downloadPath}/exports/{date}/{jobId}_{exportType}_{timestamp}.xlsx
     */
    private String generateFilePath(String jobId, String exportType) {
        LocalDate today = LocalDate.now();
        String dateStr = today.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));

        String dirPath = downloadPath + "/exports/" + dateStr;
        File dir = new File(dirPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        String fileName = jobId + "_" + exportType + "_" + timestamp + ".xlsx";
        return dirPath + "/" + fileName;
    }

    /**
     * 模拟导出过程（Phase 3 将替换为真实实现）
     */
    private void simulateExport(String jobId, String filePath) throws Exception {
        // 模拟进度更新
        for (int i = 0; i <= 100; i += 20) {
            exportJobService.updateJobProgress(jobId, i, i * 100L);
            Thread.sleep(500);
        }

        // 创建空文件作为占位
        File file = new File(filePath);
        file.createNewFile();
    }
}

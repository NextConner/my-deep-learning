package com.jtcool.web.controller.common;

import com.jtcool.common.core.controller.BaseController;
import com.jtcool.common.core.domain.AjaxResult;
import com.jtcool.common.domain.ExportJob;
import com.jtcool.common.utils.SecurityUtils;
import com.jtcool.framework.service.AsyncExportService;
import com.jtcool.framework.service.ExportJobService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

/**
 * 导出任务控制器
 */
@RestController
@RequestMapping("/api/export")
public class ExportController extends BaseController {

    @Autowired
    private ExportJobService exportJobService;

    @Autowired
    private AsyncExportService asyncExportService;

    /**
     * 提交导出任务
     */
    @PostMapping("/submit")
    public AjaxResult submitExport(@RequestBody Map<String, Object> request) {
        String exportType = (String) request.get("exportType");
        Object queryParams = request.get("queryParams");

        Long userId = SecurityUtils.getUserId();
        String jobId = exportJobService.submitExportJob(userId, exportType, queryParams);

        // 异步执行导出
        asyncExportService.executeExport(jobId, exportType, queryParams);

        return AjaxResult.success(jobId);
    }

    /**
     * 查询任务状态
     */
    @GetMapping("/status/{jobId}")
    public AjaxResult getStatus(@PathVariable String jobId) {
        ExportJob job = exportJobService.getJobStatus(jobId);
        if (job == null) {
            return AjaxResult.error("任务不存在");
        }
        return AjaxResult.success(job);
    }

    /**
     * 下载导出文件
     */
    @GetMapping("/download/{jobId}")
    public void downloadFile(@PathVariable String jobId, HttpServletResponse response) {
        ExportJob job = exportJobService.getJobStatus(jobId);
        if (job == null || job.getFilePath() == null) {
            return;
        }

        File file = new File(job.getFilePath());
        if (!file.exists()) {
            return;
        }

        try (FileInputStream fis = new FileInputStream(file);
             OutputStream os = response.getOutputStream()) {

            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename=" + file.getName());

            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
            os.flush();

        } catch (Exception e) {
            logger.error("文件下载失败: jobId={}", jobId, e);
        }
    }

    /**
     * 查询用户的导出历史
     */
    @GetMapping("/jobs")
    public AjaxResult getUserJobs() {
        Long userId = SecurityUtils.getUserId();
        List<ExportJob> jobs = exportJobService.getUserJobs(userId);
        return AjaxResult.success(jobs);
    }
}

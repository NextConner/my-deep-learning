package com.jtcool.framework.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jtcool.common.domain.ExportJob;
import com.jtcool.common.domain.ExportStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * 导出任务服务
 */
@Service
public class ExportJobService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String JOB_KEY_PREFIX = "export:job:";
    private static final String USER_INDEX_PREFIX = "export:user:";

    /**
     * 提交导出任务
     */
    public String submitExportJob(Long userId, String exportType, Object queryParams) {
        String jobId = UUID.randomUUID().toString();

        ExportJob job = new ExportJob();
        job.setJobId(jobId);
        job.setUserId(userId);
        job.setExportType(exportType);
        job.setStatus(ExportStatus.PENDING);
        job.setProgress(0);
        job.setProcessedRecords(0L);
        job.setCreatedTime(LocalDateTime.now());

        // 保存任务到 Redis (永久保留)
        redisTemplate.opsForValue().set(JOB_KEY_PREFIX + jobId, job);

        // 添加到用户索引
        redisTemplate.opsForSet().add(USER_INDEX_PREFIX + userId, jobId);

        return jobId;
    }

    /**
     * 查询任务状态
     */
    public ExportJob getJobStatus(String jobId) {
        Object obj = redisTemplate.opsForValue().get(JOB_KEY_PREFIX + jobId);
        if (obj == null) {
            return null;
        }
        return objectMapper.convertValue(obj, ExportJob.class);
    }

    /**
     * 更新任务进度
     */
    public void updateJobProgress(String jobId, int progress, long processedRecords) {
        ExportJob job = getJobStatus(jobId);
        if (job != null) {
            job.setProgress(progress);
            job.setProcessedRecords(processedRecords);
            redisTemplate.opsForValue().set(JOB_KEY_PREFIX + jobId, job);
        }
    }

    /**
     * 更新任务状态
     */
    public void updateStatus(String jobId, ExportStatus status) {
        ExportJob job = getJobStatus(jobId);
        if (job != null) {
            job.setStatus(status);
            redisTemplate.opsForValue().set(JOB_KEY_PREFIX + jobId, job);
        }
    }

    /**
     * 标记任务完成
     */
    public void completeJob(String jobId, String filePath) {
        ExportJob job = getJobStatus(jobId);
        if (job != null) {
            job.setStatus(ExportStatus.COMPLETED);
            job.setProgress(100);
            job.setFilePath(filePath);
            job.setCompletedTime(LocalDateTime.now());
            redisTemplate.opsForValue().set(JOB_KEY_PREFIX + jobId, job);
        }
    }

    /**
     * 标记任务失败
     */
    public void failJob(String jobId, String errorMessage) {
        ExportJob job = getJobStatus(jobId);
        if (job != null) {
            job.setStatus(ExportStatus.FAILED);
            job.setErrorMessage(errorMessage);
            job.setCompletedTime(LocalDateTime.now());
            redisTemplate.opsForValue().set(JOB_KEY_PREFIX + jobId, job);
        }
    }

    /**
     * 查询用户的导出历史
     */
    public List<ExportJob> getUserJobs(Long userId) {
        Set<Object> jobIds = redisTemplate.opsForSet().members(USER_INDEX_PREFIX + userId);
        List<ExportJob> jobs = new ArrayList<>();

        if (jobIds != null) {
            for (Object jobId : jobIds) {
                ExportJob job = getJobStatus(jobId.toString());
                if (job != null) {
                    jobs.add(job);
                }
            }
        }

        return jobs;
    }
}

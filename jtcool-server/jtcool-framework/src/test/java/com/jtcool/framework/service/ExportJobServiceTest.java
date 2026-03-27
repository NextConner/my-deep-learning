package com.jtcool.framework.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jtcool.common.domain.ExportJob;
import com.jtcool.common.domain.ExportStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.ValueOperations;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ExportJobServiceTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @Mock
    private SetOperations<String, Object> setOperations;

    @InjectMocks
    private ExportJobService exportJobService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(redisTemplate.opsForSet()).thenReturn(setOperations);
    }

    @Test
    void testSubmitExportJob() {
        Long userId = 1L;
        String exportType = "OmsOrder";

        String jobId = exportJobService.submitExportJob(userId, exportType, null);

        assertNotNull(jobId);
        verify(valueOperations).set(startsWith("export:job:"), any(ExportJob.class));
        verify(setOperations).add(eq("export:user:" + userId), eq(jobId));
    }

    @Test
    void testGetJobStatus() {
        String jobId = "test-job-id";
        ExportJob mockJob = new ExportJob();
        mockJob.setJobId(jobId);
        mockJob.setStatus(ExportStatus.PENDING);

        when(valueOperations.get("export:job:" + jobId)).thenReturn(mockJob);
        when(objectMapper.convertValue(mockJob, ExportJob.class)).thenReturn(mockJob);

        ExportJob result = exportJobService.getJobStatus(jobId);

        assertNotNull(result);
        assertEquals(jobId, result.getJobId());
        assertEquals(ExportStatus.PENDING, result.getStatus());
    }

    @Test
    void testUpdateJobProgress() {
        String jobId = "test-job-id";
        ExportJob mockJob = new ExportJob();
        mockJob.setJobId(jobId);

        when(valueOperations.get("export:job:" + jobId)).thenReturn(mockJob);
        when(objectMapper.convertValue(mockJob, ExportJob.class)).thenReturn(mockJob);

        exportJobService.updateJobProgress(jobId, 50, 5000L);

        verify(valueOperations).set(eq("export:job:" + jobId), any(ExportJob.class));
    }

    @Test
    void testCompleteJob() {
        String jobId = "test-job-id";
        String filePath = "/path/to/file.xlsx";
        ExportJob mockJob = new ExportJob();
        mockJob.setJobId(jobId);

        when(valueOperations.get("export:job:" + jobId)).thenReturn(mockJob);
        when(objectMapper.convertValue(mockJob, ExportJob.class)).thenReturn(mockJob);

        exportJobService.completeJob(jobId, filePath);

        verify(valueOperations).set(eq("export:job:" + jobId), any(ExportJob.class));
    }

    @Test
    void testFailJob() {
        String jobId = "test-job-id";
        String errorMessage = "Export failed";
        ExportJob mockJob = new ExportJob();
        mockJob.setJobId(jobId);

        when(valueOperations.get("export:job:" + jobId)).thenReturn(mockJob);
        when(objectMapper.convertValue(mockJob, ExportJob.class)).thenReturn(mockJob);

        exportJobService.failJob(jobId, errorMessage);

        verify(valueOperations).set(eq("export:job:" + jobId), any(ExportJob.class));
    }

    @Test
    void testGetUserJobs() {
        Long userId = 1L;
        Set<Object> jobIds = Set.of("job1", "job2");

        when(setOperations.members("export:user:" + userId)).thenReturn(jobIds);
        when(valueOperations.get(anyString())).thenReturn(new ExportJob());
        when(objectMapper.convertValue(any(), eq(ExportJob.class))).thenReturn(new ExportJob());

        List<ExportJob> result = exportJobService.getUserJobs(userId);

        assertEquals(2, result.size());
    }
}

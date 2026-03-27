package com.jtcool.web.controller.common;

import com.jtcool.common.core.domain.AjaxResult;
import com.jtcool.common.domain.ExportJob;
import com.jtcool.common.domain.ExportStatus;
import com.jtcool.framework.service.AsyncExportService;
import com.jtcool.framework.service.ExportJobService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ExportControllerTest {

    @Mock
    private ExportJobService exportJobService;

    @Mock
    private AsyncExportService asyncExportService;

    @InjectMocks
    private ExportController exportController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testSubmitExport() {
        Map<String, Object> request = new HashMap<>();
        request.put("exportType", "OmsOrder");
        request.put("queryParams", new HashMap<>());

        when(exportJobService.submitExportJob(anyLong(), anyString(), any())).thenReturn("job-123");

        AjaxResult result = exportController.submitExport(request);

        assertEquals(AjaxResult.Type.SUCCESS, result.get(AjaxResult.CODE_TAG));
        verify(asyncExportService).executeExport(eq("job-123"), eq("OmsOrder"), any());
    }

    @Test
    void testGetStatus() {
        String jobId = "job-123";
        ExportJob mockJob = new ExportJob();
        mockJob.setJobId(jobId);
        mockJob.setStatus(ExportStatus.PROCESSING);

        when(exportJobService.getJobStatus(jobId)).thenReturn(mockJob);

        AjaxResult result = exportController.getStatus(jobId);

        assertEquals(AjaxResult.Type.SUCCESS, result.get(AjaxResult.CODE_TAG));
    }

    @Test
    void testGetStatusNotFound() {
        String jobId = "job-123";

        when(exportJobService.getJobStatus(jobId)).thenReturn(null);

        AjaxResult result = exportController.getStatus(jobId);

        assertEquals(AjaxResult.Type.ERROR, result.get(AjaxResult.CODE_TAG));
    }

    @Test
    void testGetUserJobs() {
        ExportJob job1 = new ExportJob();
        job1.setJobId("job-1");

        when(exportJobService.getUserJobs(anyLong())).thenReturn(List.of(job1));

        AjaxResult result = exportController.getUserJobs();

        assertEquals(AjaxResult.Type.SUCCESS, result.get(AjaxResult.CODE_TAG));
    }
}

package com.jtcool.framework.service;

import com.jtcool.common.domain.ExportStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class AsyncExportServiceTest {

    @Mock
    private ExportJobService exportJobService;

    @InjectMocks
    private AsyncExportService asyncExportService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(asyncExportService, "downloadPath", "/tmp/test");
    }

    @Test
    void testExecuteExportSuccess() throws Exception {
        String jobId = "test-job-id";
        String exportType = "OmsOrder";

        asyncExportService.executeExport(jobId, exportType, null);

        Thread.sleep(3000); // Wait for async execution

        verify(exportJobService).updateStatus(eq(jobId), eq(ExportStatus.PROCESSING));
        verify(exportJobService).completeJob(eq(jobId), anyString());
    }

    @Test
    void testExecuteExportWithException() throws Exception {
        String jobId = "test-job-id";
        String exportType = "OmsOrder";

        doThrow(new RuntimeException("Test error"))
            .when(exportJobService).updateStatus(anyString(), any(ExportStatus.class));

        asyncExportService.executeExport(jobId, exportType, null);

        Thread.sleep(1000);

        verify(exportJobService).failJob(eq(jobId), anyString());
    }
}

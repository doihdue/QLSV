package com.demo.be.service;

import com.demo.be.dto.report.ReportExportMessage;
import com.demo.be.dto.report.ReportExportRequest;
import com.demo.be.dto.report.ReportJobResponse;

import java.io.File;
import java.util.List;

public interface ReportJobService {
    ReportJobResponse requestExportDrl(ReportExportRequest request, String requestedBy);
    ReportJobResponse requestExportGpa(Long lopId, String requestedBy);
    ReportJobResponse getJobStatus(String jobId);
    File getReportFile(String jobId);
    List<ReportJobResponse> getMyHistory(String username);
    void processJob(ReportExportMessage message);
}

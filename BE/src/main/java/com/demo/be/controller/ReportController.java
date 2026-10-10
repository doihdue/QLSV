package com.demo.be.controller;

import com.demo.be.dto.report.ReportExportRequest;
import com.demo.be.dto.report.ReportJobResponse;
import com.demo.be.service.ReportJobService;
import jakarta.validation.Valid;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportJobService reportJobService;

    public ReportController(ReportJobService reportJobService) {
        this.reportJobService = reportJobService;
    }

    /**
     * Yêu cầu xuất báo cáo rèn luyện bất đồng bộ qua RabbitMQ (tránh load trang lâu)
     */
    @PostMapping("/export-drl")
    public ResponseEntity<ReportJobResponse> exportDrl(
            Principal principal,
            @Valid @RequestBody ReportExportRequest request
    ) {
        ReportJobResponse response = reportJobService.requestExportDrl(request, principal.getName());
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    /**
     * Yêu cầu xuất báo cáo thống kê GPA bất đồng bộ qua RabbitMQ (tránh load trang lâu)
     */
    @PostMapping("/export-gpa")
    public ResponseEntity<ReportJobResponse> exportGpa(
            Principal principal,
            @org.springframework.web.bind.annotation.RequestParam(required = false) Long lopId
    ) {
        ReportJobResponse response = reportJobService.requestExportGpa(lopId, principal != null ? principal.getName() : "admin");
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    /**
     * Kiểm tra tiến độ / trạng thái xử lý của tác vụ xuất báo cáo (Polling / Check)
     */
    @GetMapping("/status/{jobId}")
    public ResponseEntity<ReportJobResponse> getStatus(@PathVariable String jobId) {
        return ResponseEntity.ok(reportJobService.getJobStatus(jobId));
    }

    /**
     * Tải về file PDF báo cáo đã được JasperReports tạo xong
     */
    @GetMapping("/download/{jobId}")
    public ResponseEntity<Resource> downloadReport(@PathVariable String jobId) {
        File file = reportJobService.getReportFile(jobId);
        Resource resource = new FileSystemResource(file);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getName() + "\"")
                .body(resource);
    }

    /**
     * Lấy danh sách lịch sử các báo cáo đã tạo của người dùng
     */
    @GetMapping("/my-history")
    public ResponseEntity<List<ReportJobResponse>> getMyHistory(Principal principal) {
        return ResponseEntity.ok(reportJobService.getMyHistory(principal.getName()));
    }
}

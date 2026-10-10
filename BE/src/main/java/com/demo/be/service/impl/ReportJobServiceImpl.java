package com.demo.be.service.impl;

import com.demo.be.dto.report.ReportExportMessage;
import com.demo.be.dto.report.ReportExportRequest;
import com.demo.be.dto.report.ReportJobResponse;
import com.demo.be.exception.ResourceNotFoundException;
import com.demo.be.model.DotRenLuyen;
import com.demo.be.model.Lop;
import com.demo.be.model.ReportJob;
import com.demo.be.producer.ReportExportProducer;
import com.demo.be.repository.DotRenLuyenRepository;
import com.demo.be.repository.LopRepository;
import com.demo.be.repository.ReportJobRepository;
import com.demo.be.service.JasperReportService;
import com.demo.be.service.ReportJobService;
import com.demo.be.service.ThongBaoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class ReportJobServiceImpl implements ReportJobService {

    private static final Logger log = LoggerFactory.getLogger(ReportJobServiceImpl.class);

    private final ReportJobRepository reportJobRepository;
    private final ReportExportProducer reportExportProducer;
    private final JasperReportService jasperReportService;
    private final DotRenLuyenRepository dotRenLuyenRepository;
    private final LopRepository lopRepository;
    private final ThongBaoService thongBaoService;

    public ReportJobServiceImpl(
            ReportJobRepository reportJobRepository,
            ReportExportProducer reportExportProducer,
            JasperReportService jasperReportService,
            DotRenLuyenRepository dotRenLuyenRepository,
            LopRepository lopRepository,
            ThongBaoService thongBaoService
    ) {
        this.reportJobRepository = reportJobRepository;
        this.reportExportProducer = reportExportProducer;
        this.jasperReportService = jasperReportService;
        this.dotRenLuyenRepository = dotRenLuyenRepository;
        this.lopRepository = lopRepository;
        this.thongBaoService = thongBaoService;
    }

    @Override
    @Transactional
    public ReportJobResponse requestExportDrl(ReportExportRequest request, String requestedBy) {
        DotRenLuyen dot = dotRenLuyenRepository.findById(request.dotId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đợt rèn luyện với ID " + request.dotId()));
        Lop lop = lopRepository.findById(request.lopId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy lớp với ID " + request.lopId()));

        String jobId = UUID.randomUUID().toString();
        String title = "Báo cáo Điểm rèn luyện - " + lop.getTenLop() + " (" + dot.getTenDot() + ")";
        String cleanMaLop = lop.getMaLop().replaceAll("[^a-zA-Z0-9_-]", "_");
        String fileName = "BaoCao_DRL_" + cleanMaLop + "_HK" + dot.getHocKy() + "_" + System.currentTimeMillis() + ".pdf";

        ReportJob job = new ReportJob(
                jobId,
                "DIEM_REN_LUYEN",
                title,
                dot.getId(),
                lop.getId(),
                request.format() != null ? request.format().toUpperCase() : "PDF",
                requestedBy
        );
        job.setFileName(fileName);
        ReportJob saved = reportJobRepository.save(job);

        // Gửi thông điệp xử lý bất đồng bộ vào RabbitMQ Queue
        reportExportProducer.sendExportMessage(new ReportExportMessage(
                jobId,
                "DIEM_REN_LUYEN",
                dot.getId(),
                lop.getId(),
                job.getFormat(),
                requestedBy
        ));

        return toResponse(saved);
    }

    @Override
    @Transactional
    public ReportJobResponse requestExportGpa(Long lopId, String requestedBy) {
        String lopName = "Toàn trường";
        String cleanLop = "ALL";
        if (lopId != null) {
            Lop lop = lopRepository.findById(lopId)
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy lớp với ID " + lopId));
            lopName = lop.getTenLop() + " (" + lop.getMaLop() + ")";
            cleanLop = lop.getMaLop().replaceAll("[^a-zA-Z0-9_-]", "_");
        }

        String jobId = UUID.randomUUID().toString();
        String title = "Báo cáo Thống kê Điểm TB & Học lực - " + lopName;
        String fileName = "BaoCao_GPA_" + cleanLop + "_" + System.currentTimeMillis() + ".pdf";

        ReportJob job = new ReportJob(
                jobId,
                "THONG_KE_GPA",
                title,
                null,
                lopId,
                "PDF",
                requestedBy
        );
        job.setFileName(fileName);
        ReportJob saved = reportJobRepository.save(job);

        // Gửi thông điệp xử lý bất đồng bộ vào RabbitMQ Queue
        reportExportProducer.sendExportMessage(new ReportExportMessage(
                jobId,
                "THONG_KE_GPA",
                null,
                lopId,
                "PDF",
                requestedBy
        ));

        return toResponse(saved);
    }

    @Override
    public ReportJobResponse getJobStatus(String jobId) {
        ReportJob job = reportJobRepository.findByJobId(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tác vụ xuất báo cáo với JobID " + jobId));
        return toResponse(job);
    }

    @Override
    public File getReportFile(String jobId) {
        ReportJob job = reportJobRepository.findByJobId(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tác vụ xuất báo cáo với JobID " + jobId));

        if (!"COMPLETED".equalsIgnoreCase(job.getStatus())) {
            throw new IllegalStateException("Báo cáo hiện chưa được tạo xong (Trạng thái: " + job.getStatus() + "). Vui lòng đợi trong giây lát.");
        }

        if (job.getFilePath() == null) {
            throw new ResourceNotFoundException("Đường dẫn tệp báo cáo không hợp lệ.");
        }

        File file = new File(job.getFilePath());
        if (!file.exists() || !file.canRead()) {
            throw new ResourceNotFoundException("Tệp báo cáo không tồn tại trên hệ thống lưu trữ.");
        }

        return file;
    }

    @Override
    public List<ReportJobResponse> getMyHistory(String username) {
        List<ReportJob> jobs = "admin".equalsIgnoreCase(username)
                ? reportJobRepository.findAllByOrderByCreatedAtDesc()
                : reportJobRepository.findByRequestedByOrderByCreatedAtDesc(username);
        return jobs.stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional
    public void processJob(ReportExportMessage message) {
        ReportJob job = reportJobRepository.findByJobId(message.jobId())
                .orElse(null);

        if (job == null) {
            log.error("[REPORT SERVICE] Không tìm thấy ReportJob cho jobId: {}", message.jobId());
            return;
        }

        job.setStatus("PROCESSING");
        reportJobRepository.save(job);

        try {
            File storageDir = new File("reports_storage");
            if (!storageDir.exists()) {
                storageDir.mkdirs();
            }

            File targetFile = new File(storageDir, job.getFileName());

            if ("THONG_KE_GPA".equalsIgnoreCase(message.reportType()) || "THONG_KE".equalsIgnoreCase(message.reportType())) {
                jasperReportService.generateGpaReport(message.lopId(), targetFile.getAbsolutePath());
            } else {
                jasperReportService.generateDrlReport(message.dotId(), message.lopId(), targetFile.getAbsolutePath());
            }

            job.setStatus("COMPLETED");
            job.setFilePath(targetFile.getAbsolutePath());
            job.setFileSize(targetFile.length());
            job.setCompletedAt(LocalDateTime.now());
            job.setErrorMessage(null);
            reportJobRepository.save(job);

            log.info("[REPORT SERVICE] ===> Hoàn tất xuất báo cáo thành công! JobId={}, File={}", job.getJobId(), targetFile.getAbsolutePath());

            // Gửi thông báo đến người yêu cầu xuất báo cáo
            thongBaoService.createNotification(
                    message.requestedBy(),
                    "Xuất báo cáo thành công",
                    "Báo cáo '" + job.getTitle() + "' đã được tạo hoàn tất. Bạn có thể tải về ngay.",
                    "BAO_CAO",
                    "/api/reports/download/" + job.getJobId()
            );
        } catch (Exception e) {
            log.error("[REPORT SERVICE] Lỗi khi tạo báo cáo JasperReports cho jobId={}: {}", message.jobId(), e.getMessage(), e);
            job.setStatus("FAILED");
            job.setErrorMessage(e.getMessage() != null ? e.getMessage() : "Lỗi không xác định khi biên dịch báo cáo JasperReports");
            job.setCompletedAt(LocalDateTime.now());
            reportJobRepository.save(job);

            thongBaoService.createNotification(
                    message.requestedBy(),
                    "Xuất báo cáo thất bại",
                    "Quá trình xuất báo cáo '" + job.getTitle() + "' bị lỗi: " + job.getErrorMessage(),
                    "BAO_CAO",
                    null
            );
        }
    }

    private ReportJobResponse toResponse(ReportJob j) {
        return new ReportJobResponse(
                j.getJobId(),
                j.getReportType(),
                j.getTitle(),
                j.getStatus(),
                j.getFileName(),
                j.getFileSize(),
                j.getRequestedBy(),
                j.getCreatedAt(),
                j.getCompletedAt(),
                j.getErrorMessage()
        );
    }
}

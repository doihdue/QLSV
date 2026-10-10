package com.demo.be.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "report_job")
public class ReportJob {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 64)
    private String jobId;

    @Column(nullable = false, length = 50)
    private String reportType; // DIEM_REN_LUYEN, BANG_DIEM, THONG_KE

    @Column(nullable = false, length = 255, columnDefinition = "NVARCHAR(255)")
    private String title;

    @Column
    private Long dotId;

    @Column
    private Long lopId;

    @Column(nullable = false, length = 10)
    private String format = "PDF";

    @Column(nullable = false, length = 100)
    private String requestedBy;

    @Column(nullable = false, length = 30)
    private String status = "PENDING"; // PENDING, PROCESSING, COMPLETED, FAILED

    @Column(length = 255, columnDefinition = "NVARCHAR(255)")
    private String fileName;

    @Column(length = 500, columnDefinition = "NVARCHAR(500)")
    private String filePath;

    @Column
    private Long fileSize;

    @Column(length = 1000, columnDefinition = "NVARCHAR(1000)")
    private String errorMessage;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column
    private LocalDateTime completedAt;

    public ReportJob() {
    }

    public ReportJob(String jobId, String reportType, String title, Long dotId, Long lopId, String format, String requestedBy) {
        this.jobId = jobId;
        this.reportType = reportType;
        this.title = title;
        this.dotId = dotId;
        this.lopId = lopId;
        this.format = format;
        this.requestedBy = requestedBy;
        this.status = "PENDING";
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public String getReportType() {
        return reportType;
    }

    public void setReportType(String reportType) {
        this.reportType = reportType;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Long getDotId() {
        return dotId;
    }

    public void setDotId(Long dotId) {
        this.dotId = dotId;
    }

    public Long getLopId() {
        return lopId;
    }

    public void setLopId(Long lopId) {
        this.lopId = lopId;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public String getRequestedBy() {
        return requestedBy;
    }

    public void setRequestedBy(String requestedBy) {
        this.requestedBy = requestedBy;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }
}

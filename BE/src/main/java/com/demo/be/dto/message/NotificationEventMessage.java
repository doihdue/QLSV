package com.demo.be.dto.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationEventMessage implements Serializable {

    private String eventType; // GRADE_APPROVED, GRADE_REJECTED, GRADE_SUBMITTED
    private Long monHocMoId;
    private String maMonHoc;
    private String tenMonHoc;
    private String maLop;
    private String tenLop;
    private String maGiangVien;
    private String tenGiangVien;
    private List<String> studentMssvList;
    private String reason;
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();
}

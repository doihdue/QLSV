package com.demo.be.controller;

import com.demo.be.dto.diem.AdminDuyetDiemRequest;
import com.demo.be.dto.diem.LecturerGradeItemDto;
import com.demo.be.dto.diem.PendingApprovalClassDto;
import com.demo.be.service.QuanLyDiemService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/duyet-diem")
public class AdminDuyetDiemController {

    private final QuanLyDiemService quanLyDiemService;

    public AdminDuyetDiemController(QuanLyDiemService quanLyDiemService) {
        this.quanLyDiemService = quanLyDiemService;
    }

    /**
     * Lấy danh sách các lớp môn học đang có bảng điểm chờ duyệt
     */
    @GetMapping("/pending")
    public ResponseEntity<List<PendingApprovalClassDto>> getPendingClasses() {
        return ResponseEntity.ok(quanLyDiemService.getPendingApprovalClasses());
    }

    /**
     * Xem chi tiết bảng điểm chờ duyệt của một lớp môn học
     */
    @GetMapping("/chi-tiet/{monHocMoId}")
    public ResponseEntity<List<LecturerGradeItemDto>> getPendingGradeDetails(
            @PathVariable Long monHocMoId
    ) {
        return ResponseEntity.ok(quanLyDiemService.getStudentsAndGradesForMonHocMo(monHocMoId));
    }

    /**
     * Admin duyệt công bố điểm cho lớp môn học -> Uỷ thác toàn bộ cho Service xử lý và bắn sự kiện RabbitMQ
     */
    @PutMapping("/{monHocMoId}/approve")
    public ResponseEntity<Void> approve(
            @PathVariable Long monHocMoId
    ) {
        quanLyDiemService.approveGrades(monHocMoId);
        return ResponseEntity.ok().build();
    }

    /**
     * Admin từ chối bảng điểm (yêu cầu sửa lại kèm lý do) -> Uỷ thác cho Service xử lý và bắn sự kiện RabbitMQ
     */
    @PutMapping("/{monHocMoId}/reject")
    public ResponseEntity<Void> reject(
            @PathVariable Long monHocMoId,
            @RequestBody(required = false) AdminDuyetDiemRequest req
    ) {
        quanLyDiemService.rejectGrades(monHocMoId, req);
        return ResponseEntity.ok().build();
    }
}

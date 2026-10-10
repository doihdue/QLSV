package com.demo.be.controller;

import com.demo.be.dto.renluyen.AdminPheDuyetDrlRequest;
import com.demo.be.dto.renluyen.BangDiemRenLuyenResponse;
import com.demo.be.dto.renluyen.LecturerDanhGiaDrlRequest;
import com.demo.be.dto.renluyen.StudentDanhGiaDrlRequest;
import com.demo.be.dto.renluyen.ThongKeDrlResponse;
import com.demo.be.service.DiemRenLuyenService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/diem-ren-luyen")
public class DiemRenLuyenController {

    private final DiemRenLuyenService diemRenLuyenService;

    public DiemRenLuyenController(DiemRenLuyenService diemRenLuyenService) {
        this.diemRenLuyenService = diemRenLuyenService;
    }

    // ==========================================
    // SINH VIÊN
    // ==========================================

    @GetMapping("/me")
    public ResponseEntity<BangDiemRenLuyenResponse> getMyScore(
            Principal principal,
            @RequestParam(required = false) Long dotId
    ) {
        return ResponseEntity.ok(diemRenLuyenService.getMyScore(principal.getName(), dotId));
    }

    @PostMapping("/me/submit")
    public ResponseEntity<BangDiemRenLuyenResponse> studentSubmit(
            Principal principal,
            @Valid @RequestBody StudentDanhGiaDrlRequest request
    ) {
        return ResponseEntity.ok(diemRenLuyenService.studentSubmit(principal.getName(), request));
    }

    // ==========================================
    // GIẢNG VIÊN
    // ==========================================

    @GetMapping("/lop/{lopId}")
    public ResponseEntity<List<BangDiemRenLuyenResponse>> getScoresForClass(
            @PathVariable Long lopId,
            @RequestParam(required = false) Long dotId
    ) {
        return ResponseEntity.ok(diemRenLuyenService.getScoresForClass(lopId, dotId));
    }

    @GetMapping("/chi-tiet/{id}")
    public ResponseEntity<BangDiemRenLuyenResponse> getScoreById(@PathVariable Long id) {
        return ResponseEntity.ok(diemRenLuyenService.getScoreById(id));
    }

    @PostMapping("/giang-vien/evaluate")
    public ResponseEntity<BangDiemRenLuyenResponse> lecturerEvaluate(
            Principal principal,
            @Valid @RequestBody LecturerDanhGiaDrlRequest request
    ) {
        return ResponseEntity.ok(diemRenLuyenService.lecturerEvaluate(principal.getName(), request));
    }

    @PostMapping("/giang-vien/submit-lop")
    public ResponseEntity<Map<String, Object>> lecturerSubmitClass(
            Principal principal,
            @RequestParam Long lopId,
            @RequestParam(required = false) Long dotId
    ) {
        int count = diemRenLuyenService.lecturerSubmitWholeClassToAdmin(principal.getName(), lopId, dotId);
        return ResponseEntity.ok(Map.of("message", "Đã gửi thành công " + count + " phiếu rèn luyện lên Admin xét duyệt.", "count", count));
    }

    // ==========================================
    // ADMIN
    // ==========================================

    @GetMapping("/admin")
    public ResponseEntity<List<BangDiemRenLuyenResponse>> getScoresForAdmin(
            @RequestParam(required = false) Long dotId,
            @RequestParam(required = false) Long lopId,
            @RequestParam(required = false) String trangThai
    ) {
        return ResponseEntity.ok(diemRenLuyenService.getScoresForAdmin(dotId, lopId, trangThai));
    }

    @GetMapping("/admin/thong-ke")
    public ResponseEntity<ThongKeDrlResponse> getThongKeDrl(
            @RequestParam(required = false) Long dotId,
            @RequestParam(required = false) Long lopId
    ) {
        return ResponseEntity.ok(diemRenLuyenService.getThongKeDrl(dotId, lopId));
    }

    @PostMapping("/admin/phe-duyet")
    public ResponseEntity<BangDiemRenLuyenResponse> adminApproveOrReject(
            @Valid @RequestBody AdminPheDuyetDrlRequest request
    ) {
        return ResponseEntity.ok(diemRenLuyenService.adminApproveOrReject(request));
    }

    @PostMapping("/admin/duyet-lop")
    public ResponseEntity<Map<String, Object>> adminApproveWholeClass(
            @RequestParam(required = false) Long dotId,
            @RequestParam Long lopId
    ) {
        int count = diemRenLuyenService.adminApproveWholeClass(dotId, lopId);
        return ResponseEntity.ok(Map.of("message", "Đã phê duyệt hoàn tất " + count + " phiếu rèn luyện của lớp.", "count", count));
    }
}

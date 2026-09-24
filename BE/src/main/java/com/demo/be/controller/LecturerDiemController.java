package com.demo.be.controller;

import com.demo.be.dto.diem.LecturerGradeBatchRequest;
import com.demo.be.dto.diem.LecturerGradeItemDto;
import com.demo.be.dto.monhocmo.MonHocMoResponse;
import com.demo.be.service.MonHocMoService;
import com.demo.be.service.QuanLyDiemService;
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

@RestController
@RequestMapping("/api/lecturer")
public class LecturerDiemController {

    private final MonHocMoService monHocMoService;
    private final QuanLyDiemService quanLyDiemService;

    public LecturerDiemController(
            MonHocMoService monHocMoService,
            QuanLyDiemService quanLyDiemService
    ) {
        this.monHocMoService = monHocMoService;
        this.quanLyDiemService = quanLyDiemService;
    }

    /**
     * Lấy danh sách các lớp môn học mà giảng viên đăng nhập được phân công giảng dạy
     */
    @GetMapping("/mon-hoc-mo")
    public ResponseEntity<List<MonHocMoResponse>> getMyClasses(
            Principal principal,
            @RequestParam(required = false) String hocKy,
            @RequestParam(required = false) String namHoc
    ) {
        return ResponseEntity.ok(monHocMoService.findByGiangVien(principal.getName(), hocKy, namHoc));
    }

    /**
     * Lấy danh sách sinh viên và bảng điểm trong lớp môn học
     */
    @GetMapping("/bang-diem/{monHocMoId}")
    public ResponseEntity<List<LecturerGradeItemDto>> getGradeSheet(
            @PathVariable Long monHocMoId
    ) {
        return ResponseEntity.ok(quanLyDiemService.getStudentsAndGradesForMonHocMo(monHocMoId));
    }

    /**
     * Giảng viên lưu điểm theo lô (Lưu nháp hoặc Gửi Admin duyệt) -> Uỷ thác toàn bộ cho Service xử lý
     */
    @PostMapping("/bang-diem")
    public ResponseEntity<List<LecturerGradeItemDto>> saveGrades(
            @RequestBody LecturerGradeBatchRequest request
    ) {
        return ResponseEntity.ok(quanLyDiemService.saveGradesForMonHocMo(request));
    }
}

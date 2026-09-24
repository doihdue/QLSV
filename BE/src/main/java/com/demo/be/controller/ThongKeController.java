package com.demo.be.controller;

import com.demo.be.dto.thongke.SinhVienGpaResponse;
import com.demo.be.service.ThongKeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/thong-ke")
public class ThongKeController {

    private final ThongKeService thongKeService;

    public ThongKeController(ThongKeService thongKeService) {
        this.thongKeService = thongKeService;
    }

    /**
     * API Native Query: Lấy bảng xếp hạng GPA và thống kê học lực sinh viên
     * Thực thi qua Native Query kết hợp nhiều bảng với các hàm tổng hợp SQL Server
     *
     * @param lopId (Tùy chọn) ID lớp học cần lọc
     * @return Danh sách sinh viên kèm GPA và xếp loại học lực
     */
    @GetMapping("/sinh-vien-gpa")
    public ResponseEntity<List<SinhVienGpaResponse>> getThongKeGpa(
            @RequestParam(required = false) Long lopId
    ) {
        return ResponseEntity.ok(thongKeService.getThongKeGpa(lopId));
    }
}

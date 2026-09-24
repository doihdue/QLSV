package com.demo.be.controller;

import com.demo.be.dto.dangky.DangKyMonHocRequest;
import com.demo.be.dto.dangky.DangKyMonHocResponse;
import com.demo.be.service.DangKyMonHocService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.security.Principal;
import com.demo.be.dto.dangky.StudentDangKyMonHocRequest;

@RestController
@RequestMapping("/api/dang-ky-mon-hoc")
public class DangKyMonHocController {

    private final DangKyMonHocService dangKyMonHocService;

    public DangKyMonHocController(DangKyMonHocService dangKyMonHocService) {
        this.dangKyMonHocService = dangKyMonHocService;
    }

    @GetMapping
    public ResponseEntity<List<DangKyMonHocResponse>> getAll() {
        return ResponseEntity.ok(dangKyMonHocService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<DangKyMonHocResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(dangKyMonHocService.findById(id));
    }

        @GetMapping("/me")
        public ResponseEntity<List<DangKyMonHocResponse>> getCurrent(
            Principal principal,
            @RequestParam(required = false) String hocKy,
            @RequestParam(required = false) String namHoc
        ) {
        return ResponseEntity.ok(dangKyMonHocService.findBySinhVienAndSemester(principal.getName(), hocKy, namHoc));
        }

        @PostMapping("/me")
        public ResponseEntity<DangKyMonHocResponse> register(
            Principal principal,
            @Valid @RequestBody StudentDangKyMonHocRequest request
        ) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(dangKyMonHocService.createForStudent(principal.getName(), request));
        }

        @DeleteMapping("/me/{id}")
        public ResponseEntity<Void> cancelMyRegistration(
            Principal principal,
            @PathVariable Long id
        ) {
            dangKyMonHocService.deleteForStudent(principal.getName(), id);
            return ResponseEntity.noContent().build();
        }

    @GetMapping("/sinh-vien/{sinhVienId}")
    public ResponseEntity<List<DangKyMonHocResponse>> getBySinhVien(@PathVariable Long sinhVienId) {
        return ResponseEntity.ok(dangKyMonHocService.findBySinhVien(sinhVienId));
    }

    @PostMapping
    public ResponseEntity<DangKyMonHocResponse> create(@Valid @RequestBody DangKyMonHocRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(dangKyMonHocService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DangKyMonHocResponse> update(@PathVariable Long id, @Valid @RequestBody DangKyMonHocRequest request) {
        return ResponseEntity.ok(dangKyMonHocService.update(id, request));
    }

    @PostMapping("/lop/{lopId}")
    public ResponseEntity<List<DangKyMonHocResponse>> registerForClass(
            @PathVariable Long lopId,
            @RequestParam Long monHocId,
            @RequestParam String hocKy,
            @RequestParam String namHoc
    ) {
        return ResponseEntity.ok(dangKyMonHocService.dangKyChoLopHanhChinh(lopId, monHocId, hocKy, namHoc));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        dangKyMonHocService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

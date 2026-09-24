package com.demo.be.controller;

import com.demo.be.dto.monhocmo.MonHocMoRequest;
import com.demo.be.dto.monhocmo.MonHocMoResponse;
import com.demo.be.service.MonHocMoService;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/mon-hoc-mo")
public class MonHocMoController {

    private final MonHocMoService monHocMoService;

    public MonHocMoController(MonHocMoService monHocMoService) {
        this.monHocMoService = monHocMoService;
    }

    @GetMapping
    public ResponseEntity<List<MonHocMoResponse>> getByFilters(
            @RequestParam(required = false) String hocKy,
            @RequestParam(required = false) String namHoc,
            @RequestParam(required = false) Long khoaId,
            @RequestParam(required = false) String khoaHoc,
            @RequestParam(required = false) Long lopId
    ) {
        return ResponseEntity.ok(monHocMoService.findByFilters(hocKy, namHoc, khoaId, khoaHoc, lopId));
    }

    @GetMapping("/lop/{lopId}")
    public ResponseEntity<List<MonHocMoResponse>> getForLop(
            @PathVariable Long lopId,
            @RequestParam(required = false) String hocKy,
            @RequestParam(required = false) String namHoc
    ) {
        return ResponseEntity.ok(monHocMoService.findForLop(lopId, hocKy, namHoc));
    }

    @PostMapping
    public ResponseEntity<MonHocMoResponse> create(@Valid @RequestBody MonHocMoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(monHocMoService.create(request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        monHocMoService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/sinh-vien")
    public ResponseEntity<List<MonHocMoResponse>> getForStudent(
            Principal principal,
            @RequestParam(required = false) String hocKy,
            @RequestParam(required = false) String namHoc
    ) {
        return ResponseEntity.ok(monHocMoService.getMonHocMoForStudent(principal.getName(), hocKy, namHoc));
    }

    @org.springframework.web.bind.annotation.PutMapping("/{id}/gan-giang-vien")
    public ResponseEntity<MonHocMoResponse> ganGiangVien(
            @PathVariable Long id,
            @RequestParam(required = false) Long giangVienId
    ) {
        return ResponseEntity.ok(monHocMoService.ganGiangVien(id, giangVienId));
    }

    @GetMapping("/giang-vien")
    public ResponseEntity<List<MonHocMoResponse>> getForLecturer(
            Principal principal,
            @RequestParam(required = false) String hocKy,
            @RequestParam(required = false) String namHoc
    ) {
        return ResponseEntity.ok(monHocMoService.findByGiangVien(principal.getName(), hocKy, namHoc));
    }
}

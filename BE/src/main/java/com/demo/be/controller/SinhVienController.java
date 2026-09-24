package com.demo.be.controller;

import com.demo.be.dto.sinhvien.SinhVienRequest;
import com.demo.be.dto.sinhvien.SinhVienResponse;
import com.demo.be.dto.sinhvien.StudentProfileUpdateRequest;
import com.demo.be.service.SinhVienService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.security.Principal;

@RestController
@RequestMapping("/api/sinh-vien")
public class SinhVienController {

    private final SinhVienService sinhVienService;

    public SinhVienController(SinhVienService sinhVienService) {
        this.sinhVienService = sinhVienService;
    }

    @GetMapping
    public ResponseEntity<List<SinhVienResponse>> getAll() {
        return ResponseEntity.ok(sinhVienService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<SinhVienResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(sinhVienService.findById(id));
    }

    @GetMapping("/generate-mssv")
    public ResponseEntity<java.util.Map<String, String>> generateMssv(
            @org.springframework.web.bind.annotation.RequestParam Long lopId,
            @org.springframework.web.bind.annotation.RequestParam(required = false)
            @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate ngayNhapHoc
    ) {
        String mssv = sinhVienService.generateNextMssv(lopId, ngayNhapHoc);
        return ResponseEntity.ok(java.util.Map.of("mssv", mssv));
    }

    @GetMapping("/generate-email")
    public ResponseEntity<java.util.Map<String, String>> generateEmail(
            @org.springframework.web.bind.annotation.RequestParam String hoTen,
            @org.springframework.web.bind.annotation.RequestParam String mssv
    ) {
        String email = SinhVienService.generateEmailFromNameAndMssv(hoTen, mssv);
        return ResponseEntity.ok(java.util.Map.of("email", email));
    }

    @GetMapping("/me")
    public ResponseEntity<SinhVienResponse> getCurrent(Principal principal) {
        return ResponseEntity.ok(sinhVienService.findByMssv(principal.getName()));
    }

    @PatchMapping("/me/profile")
    public ResponseEntity<SinhVienResponse> updateCurrentProfile(
            Principal principal,
            @Valid @RequestBody StudentProfileUpdateRequest request
    ) {
        return ResponseEntity.ok(sinhVienService.updateProfile(principal.getName(), request));
    }

    @PostMapping
    public ResponseEntity<SinhVienResponse> create(@Valid @RequestBody SinhVienRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(sinhVienService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SinhVienResponse> update(@PathVariable Long id, @Valid @RequestBody SinhVienRequest request) {
        return ResponseEntity.ok(sinhVienService.update(id, request));
    }

    @PostMapping("/{id}/reset-password")
    public ResponseEntity<String> resetPassword(@PathVariable Long id) {
        return ResponseEntity.ok(sinhVienService.resetPassword(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        sinhVienService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

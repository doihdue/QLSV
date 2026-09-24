package com.demo.be.controller;

import com.demo.be.dto.diem.QuanLyDiemRequest;
import com.demo.be.dto.diem.QuanLyDiemResponse;
import com.demo.be.service.QuanLyDiemService;
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

@RestController
@RequestMapping("/api/quan-ly-diem")
public class QuanLyDiemController {

    private final QuanLyDiemService quanLyDiemService;

    public QuanLyDiemController(QuanLyDiemService quanLyDiemService) {
        this.quanLyDiemService = quanLyDiemService;
    }

    @GetMapping
    public ResponseEntity<List<QuanLyDiemResponse>> getAll() {
        return ResponseEntity.ok(quanLyDiemService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<QuanLyDiemResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(quanLyDiemService.findById(id));
    }

    @GetMapping("/me")
    public ResponseEntity<List<QuanLyDiemResponse>> getCurrent(
            Principal principal,
            @RequestParam(required = false) String hocKy,
            @RequestParam(required = false) String namHoc
    ) {
        return ResponseEntity.ok(quanLyDiemService.findBySinhVienAndSemester(
                principal.getName(), hocKy, namHoc));
    }

    @PostMapping
    public ResponseEntity<QuanLyDiemResponse> create(@Valid @RequestBody QuanLyDiemRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(quanLyDiemService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<QuanLyDiemResponse> update(@PathVariable Long id, @Valid @RequestBody QuanLyDiemRequest request) {
        return ResponseEntity.ok(quanLyDiemService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        quanLyDiemService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

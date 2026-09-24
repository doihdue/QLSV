package com.demo.be.controller;

import com.demo.be.dto.giangvien.GiangVienRequest;
import com.demo.be.dto.giangvien.GiangVienResponse;
import com.demo.be.service.GiangVienService;
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
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/giang-vien")
public class GiangVienController {

    private final GiangVienService giangVienService;

    public GiangVienController(GiangVienService giangVienService) {
        this.giangVienService = giangVienService;
    }

    @GetMapping
    public ResponseEntity<List<GiangVienResponse>> getAll() {
        return ResponseEntity.ok(giangVienService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<GiangVienResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(giangVienService.findById(id));
    }

    @PostMapping
    public ResponseEntity<GiangVienResponse> create(@Valid @RequestBody GiangVienRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(giangVienService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<GiangVienResponse> update(@PathVariable Long id, @Valid @RequestBody GiangVienRequest request) {
        return ResponseEntity.ok(giangVienService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        giangVienService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

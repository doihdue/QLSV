package com.demo.be.controller;

import com.demo.be.dto.monhoc.MonHocRequest;
import com.demo.be.dto.monhoc.MonHocResponse;
import com.demo.be.service.MonHocService;
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
@RequestMapping("/api/mon-hoc")
public class MonHocController {

    private final MonHocService monHocService;

    public MonHocController(MonHocService monHocService) {
        this.monHocService = monHocService;
    }

    @GetMapping
    public ResponseEntity<List<MonHocResponse>> getAll() {
        return ResponseEntity.ok(monHocService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<MonHocResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(monHocService.findById(id));
    }

    @GetMapping("/active")
    public ResponseEntity<List<MonHocResponse>> getActive() {
        return ResponseEntity.ok(monHocService.findActive());
    }

    @PostMapping
    public ResponseEntity<MonHocResponse> create(@Valid @RequestBody MonHocRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(monHocService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<MonHocResponse> update(@PathVariable Long id, @Valid @RequestBody MonHocRequest request) {
        return ResponseEntity.ok(monHocService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        monHocService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

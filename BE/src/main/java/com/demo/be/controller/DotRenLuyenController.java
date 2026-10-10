package com.demo.be.controller;

import com.demo.be.dto.renluyen.DotRenLuyenRequest;
import com.demo.be.dto.renluyen.DotRenLuyenResponse;
import com.demo.be.service.DotRenLuyenService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/dot-ren-luyen")
public class DotRenLuyenController {

    private final DotRenLuyenService dotRenLuyenService;

    public DotRenLuyenController(DotRenLuyenService dotRenLuyenService) {
        this.dotRenLuyenService = dotRenLuyenService;
    }

    @GetMapping
    public ResponseEntity<List<DotRenLuyenResponse>> getAll() {
        return ResponseEntity.ok(dotRenLuyenService.findAll());
    }

    @GetMapping("/hien-tai")
    public ResponseEntity<DotRenLuyenResponse> getCurrent() {
        return ResponseEntity.ok(dotRenLuyenService.getCurrent());
    }

    @GetMapping("/{id}")
    public ResponseEntity<DotRenLuyenResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(dotRenLuyenService.getById(id));
    }

    @PostMapping
    public ResponseEntity<DotRenLuyenResponse> create(@Valid @RequestBody DotRenLuyenRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(dotRenLuyenService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DotRenLuyenResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody DotRenLuyenRequest request
    ) {
        return ResponseEntity.ok(dotRenLuyenService.update(id, request));
    }

    @PatchMapping("/{id}/toggle")
    public ResponseEntity<DotRenLuyenResponse> toggle(
            @PathVariable Long id,
            @RequestParam boolean open
    ) {
        return ResponseEntity.ok(dotRenLuyenService.toggle(id, open));
    }
}

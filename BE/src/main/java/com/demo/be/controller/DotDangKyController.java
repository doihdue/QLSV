package com.demo.be.controller;

import com.demo.be.dto.dangky.DotDangKyRequest;
import com.demo.be.dto.dangky.DotDangKyResponse;
import com.demo.be.service.DotDangKyService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dot-dang-ky")
public class DotDangKyController {

    private final DotDangKyService dotDangKyService;

    public DotDangKyController(DotDangKyService dotDangKyService) {
        this.dotDangKyService = dotDangKyService;
    }

    @GetMapping("/hien-tai")
    public ResponseEntity<DotDangKyResponse> getCurrent() {
        return ResponseEntity.ok(dotDangKyService.getCurrentResponse());
    }

    @PutMapping("/hien-tai")
    public ResponseEntity<DotDangKyResponse> updateCurrent(@Valid @RequestBody DotDangKyRequest request) {
        return ResponseEntity.ok(dotDangKyService.updateCurrent(request));
    }

    @PatchMapping("/hien-tai/toggle")
    public ResponseEntity<DotDangKyResponse> toggle(@RequestParam boolean open) {
        return ResponseEntity.ok(dotDangKyService.toggleOpen(open));
    }
}

package com.demo.be.service.impl;

import com.demo.be.dto.dangky.DotDangKyRequest;
import com.demo.be.dto.dangky.DotDangKyResponse;
import com.demo.be.model.DotDangKy;
import com.demo.be.repository.DotDangKyRepository;
import com.demo.be.service.DotDangKyService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
public class DotDangKyServiceImpl implements DotDangKyService {

    private final DotDangKyRepository dotDangKyRepository;

    public DotDangKyServiceImpl(DotDangKyRepository dotDangKyRepository) {
        this.dotDangKyRepository = dotDangKyRepository;
    }

    @Override
    public DotDangKy getCurrent() {
        DotDangKy current = dotDangKyRepository.findFirstByOrderByIdAsc().orElseGet(() -> {
            DotDangKy defaultDot = new DotDangKy("1", "2026-2027", "Đợt đăng ký tín chỉ Học kỳ 1 (2026-2027)", true);
            defaultDot.setNgayBatDau(LocalDate.now());
            defaultDot.setNgayKetThuc(LocalDate.now().plusMonths(1));
            return dotDangKyRepository.save(defaultDot);
        });

        boolean updated = false;

        if (current.getNgayBatDau() == null) {
            current.setNgayBatDau(LocalDate.now());
            updated = true;
        }
        if (current.getNgayKetThuc() == null) {
            current.setNgayKetThuc(LocalDate.now().plusMonths(1));
            updated = true;
        }

        if (current.getTenDot() == null || current.getTenDot().contains("?") || current.getTenDot().isBlank()) {
            current.setTenDot("Đợt đăng ký tín chỉ Học kỳ " + (current.getHocKy() != null ? current.getHocKy() : "1")
                    + " (" + (current.getNamHoc() != null ? current.getNamHoc() : "2026-2027") + ")");
            updated = true;
        }

        if (applyAutoSchedule(current)) {
            updated = true;
        }

        if (updated) {
            current = dotDangKyRepository.save(current);
        }

        return current;
    }

    private boolean applyAutoSchedule(DotDangKy dot) {
        if (dot == null) return false;
        if (dot.getNgayBatDau() == null && dot.getNgayKetThuc() == null) {
            return false;
        }

        LocalDate today = LocalDate.now();
        boolean shouldBeOpen = true;

        if (dot.getNgayBatDau() != null && today.isBefore(dot.getNgayBatDau())) {
            shouldBeOpen = false;
        } else if (dot.getNgayKetThuc() != null && today.isAfter(dot.getNgayKetThuc())) {
            shouldBeOpen = false;
        }

        if (dot.isDangMo() != shouldBeOpen) {
            dot.setDangMo(shouldBeOpen);
            return true;
        }
        return false;
    }

    @Override
    @Scheduled(fixedRate = 60000)
    @Transactional
    public void scheduledAutoSync() {
        dotDangKyRepository.findFirstByOrderByIdAsc().ifPresent(dot -> {
            if (applyAutoSchedule(dot)) {
                dotDangKyRepository.save(dot);
            }
        });
    }

    @Override
    public DotDangKyResponse getCurrentResponse() {
        return toResponse(getCurrent());
    }

    @Override
    @Transactional
    public DotDangKyResponse updateCurrent(DotDangKyRequest request) {
        if (request.ngayBatDau() != null && request.ngayKetThuc() != null) {
            if (!request.ngayBatDau().isBefore(request.ngayKetThuc())) {
                throw new IllegalArgumentException("Thời gian bắt đầu phải nhỏ hơn thời gian kết thúc (thời gian trước phải nhỏ hơn thời gian sau).");
            }
        }

        DotDangKy current = getCurrent();
        current.setHocKy(request.hocKy());
        current.setNamHoc(request.namHoc());
        current.setTenDot(request.tenDot() != null && !request.tenDot().isBlank() && !request.tenDot().contains("?")
                ? request.tenDot()
                : "Đợt đăng ký tín chỉ Học kỳ " + request.hocKy() + " (" + request.namHoc() + ")");
        current.setNgayBatDau(request.ngayBatDau());
        current.setNgayKetThuc(request.ngayKetThuc());

        if (request.ngayBatDau() != null || request.ngayKetThuc() != null) {
            LocalDate today = LocalDate.now();
            boolean open = true;
            if (request.ngayBatDau() != null && today.isBefore(request.ngayBatDau())) {
                open = false;
            } else if (request.ngayKetThuc() != null && today.isAfter(request.ngayKetThuc())) {
                open = false;
            }
            current.setDangMo(open);
        } else if (request.dangMo() != null) {
            current.setDangMo(request.dangMo());
        }

        return toResponse(dotDangKyRepository.save(current));
    }

    @Override
    @Transactional
    public DotDangKyResponse toggleOpen(boolean open) {
        DotDangKy current = getCurrent();
        current.setDangMo(open);
        return toResponse(dotDangKyRepository.save(current));
    }

    private DotDangKyResponse toResponse(DotDangKy dot) {
        String tenDot = dot.getTenDot();
        if (tenDot == null || tenDot.contains("?") || tenDot.isBlank()) {
            tenDot = "Đợt đăng ký tín chỉ Học kỳ " + (dot.getHocKy() != null ? dot.getHocKy() : "1")
                    + " (" + (dot.getNamHoc() != null ? dot.getNamHoc() : "2026-2027") + ")";
        }
        return new DotDangKyResponse(
                dot.getId(),
                dot.getHocKy(),
                dot.getNamHoc(),
                tenDot,
                dot.isDangMo(),
                dot.getNgayBatDau(),
                dot.getNgayKetThuc()
        );
    }
}

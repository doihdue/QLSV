package com.demo.be.service;

import com.demo.be.dto.dangky.DotDangKyRequest;
import com.demo.be.dto.dangky.DotDangKyResponse;
import com.demo.be.model.DotDangKy;
import com.demo.be.repository.DotDangKyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DotDangKyService {

    private final DotDangKyRepository dotDangKyRepository;

    public DotDangKyService(DotDangKyRepository dotDangKyRepository) {
        this.dotDangKyRepository = dotDangKyRepository;
    }

    public DotDangKy getCurrent() {
        DotDangKy current = dotDangKyRepository.findFirstByOrderByIdAsc().orElseGet(() -> {
            DotDangKy defaultDot = new DotDangKy("1", "2026-2027", "Đợt đăng ký tín chỉ Học kỳ 1 (2026-2027)", true);
            return dotDangKyRepository.save(defaultDot);
        });
        if (current.getTenDot() == null || current.getTenDot().contains("?") || current.getTenDot().isBlank()) {
            current.setTenDot("Đợt đăng ký tín chỉ Học kỳ " + (current.getHocKy() != null ? current.getHocKy() : "1")
                    + " (" + (current.getNamHoc() != null ? current.getNamHoc() : "2026-2027") + ")");
            current = dotDangKyRepository.save(current);
        }
        return current;
    }

    public DotDangKyResponse getCurrentResponse() {
        return toResponse(getCurrent());
    }

    @Transactional
    public DotDangKyResponse updateCurrent(DotDangKyRequest request) {
        DotDangKy current = getCurrent();
        current.setHocKy(request.hocKy());
        current.setNamHoc(request.namHoc());
        current.setTenDot(request.tenDot() != null && !request.tenDot().isBlank() && !request.tenDot().contains("?")
                ? request.tenDot()
                : "Đợt đăng ký tín chỉ Học kỳ " + request.hocKy() + " (" + request.namHoc() + ")");
        if (request.dangMo() != null) {
            current.setDangMo(request.dangMo());
        }
        current.setNgayBatDau(request.ngayBatDau());
        current.setNgayKetThuc(request.ngayKetThuc());

        return toResponse(dotDangKyRepository.save(current));
    }

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

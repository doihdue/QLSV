package com.demo.be.service.impl;

import com.demo.be.dto.renluyen.DotRenLuyenRequest;
import com.demo.be.dto.renluyen.DotRenLuyenResponse;
import com.demo.be.exception.ResourceNotFoundException;
import com.demo.be.model.DotRenLuyen;
import com.demo.be.repository.DotRenLuyenRepository;
import com.demo.be.service.DotRenLuyenService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class DotRenLuyenServiceImpl implements DotRenLuyenService {

    private final DotRenLuyenRepository dotRenLuyenRepository;

    public DotRenLuyenServiceImpl(DotRenLuyenRepository dotRenLuyenRepository) {
        this.dotRenLuyenRepository = dotRenLuyenRepository;
    }

    @Override
    public List<DotRenLuyenResponse> findAll() {
        return dotRenLuyenRepository.findAllByOrderByIdDesc().stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public DotRenLuyenResponse getCurrent() {
        DotRenLuyen current = dotRenLuyenRepository.findFirstByDangMoTrueOrderByIdDesc().orElseGet(() -> {
            // Khởi tạo đợt mặc định nếu hệ thống chưa có đợt nào
            DotRenLuyen defaultDot = new DotRenLuyen(
                    "Đợt đánh giá rèn luyện Học kỳ 1 (2026-2027)",
                    "1",
                    "2026-2027",
                    LocalDate.now().minusDays(5),
                    LocalDate.now().plusMonths(1),
                    true,
                    "Sinh viên hoàn thành phiếu tự đánh giá rèn luyện trước hạn chót."
            );
            return dotRenLuyenRepository.save(defaultDot);
        });
        return toResponse(current);
    }

    @Override
    public DotRenLuyenResponse getById(Long id) {
        DotRenLuyen dot = dotRenLuyenRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đợt rèn luyện với ID " + id));
        return toResponse(dot);
    }

    @Override
    @Transactional
    public DotRenLuyenResponse create(DotRenLuyenRequest request) {
        if (request.ngayBatDau() != null && request.ngayKetThuc() != null) {
            if (!request.ngayBatDau().isBefore(request.ngayKetThuc())) {
                throw new IllegalArgumentException("Thời gian trước phải nhỏ hơn thời gian sau (Ngày bắt đầu phải nhỏ hơn ngày kết thúc).");
            }
        }

        DotRenLuyen dot = new DotRenLuyen(
                request.tenDot().trim(),
                request.hocKy().trim(),
                request.namHoc().trim(),
                request.ngayBatDau(),
                request.ngayKetThuc(),
                request.dangMo() != null ? request.dangMo() : true,
                request.ghiChu() != null ? request.ghiChu().trim() : null
        );
        return toResponse(dotRenLuyenRepository.save(dot));
    }

    @Override
    @Transactional
    public DotRenLuyenResponse update(Long id, DotRenLuyenRequest request) {
        if (request.ngayBatDau() != null && request.ngayKetThuc() != null) {
            if (!request.ngayBatDau().isBefore(request.ngayKetThuc())) {
                throw new IllegalArgumentException("Thời gian trước phải nhỏ hơn thời gian sau (Ngày bắt đầu phải nhỏ hơn ngày kết thúc).");
            }
        }

        DotRenLuyen dot = dotRenLuyenRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đợt rèn luyện với ID " + id));

        dot.setTenDot(request.tenDot().trim());
        dot.setHocKy(request.hocKy().trim());
        dot.setNamHoc(request.namHoc().trim());
        dot.setNgayBatDau(request.ngayBatDau());
        dot.setNgayKetThuc(request.ngayKetThuc());
        if (request.dangMo() != null) {
            dot.setDangMo(request.dangMo());
        }
        dot.setGhiChu(request.ghiChu() != null ? request.ghiChu().trim() : null);

        return toResponse(dotRenLuyenRepository.save(dot));
    }

    @Override
    @Transactional
    public DotRenLuyenResponse toggle(Long id, boolean open) {
        DotRenLuyen dot = dotRenLuyenRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đợt rèn luyện với ID " + id));
        dot.setDangMo(open);
        return toResponse(dotRenLuyenRepository.save(dot));
    }

    private DotRenLuyenResponse toResponse(DotRenLuyen dot) {
        LocalDate today = LocalDate.now();
        String timeStatus = "ACTIVE";
        if (dot.getNgayBatDau() != null && today.isBefore(dot.getNgayBatDau())) {
            timeStatus = "NOT_YET";
        } else if (dot.getNgayKetThuc() != null && today.isAfter(dot.getNgayKetThuc())) {
            timeStatus = "EXPIRED";
        }

        return new DotRenLuyenResponse(
                dot.getId(),
                dot.getTenDot(),
                dot.getHocKy(),
                dot.getNamHoc(),
                dot.getNgayBatDau(),
                dot.getNgayKetThuc(),
                dot.isDangMo(),
                dot.getGhiChu(),
                timeStatus
        );
    }
}

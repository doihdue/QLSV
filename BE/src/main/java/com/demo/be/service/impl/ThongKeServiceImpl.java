package com.demo.be.service.impl;

import com.demo.be.dto.thongke.SinhVienGpaResponse;
import com.demo.be.repository.SinhVienRepository;
import com.demo.be.service.ThongKeService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ThongKeServiceImpl implements ThongKeService {

    private final SinhVienRepository sinhVienRepository;

    public ThongKeServiceImpl(SinhVienRepository sinhVienRepository) {
        this.sinhVienRepository = sinhVienRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<SinhVienGpaResponse> getThongKeGpa(Long lopId) {
        return sinhVienRepository.findThongKeGpaNative(lopId).stream()
                .map(SinhVienGpaResponse::fromProjection)
                .toList();
    }
}

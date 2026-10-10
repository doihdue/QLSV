package com.demo.be.service.impl;

import com.demo.be.dto.thongke.SinhVienGpaResponse;
import com.demo.be.repository.ThongKeRepository;
import com.demo.be.service.ThongKeService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ThongKeServiceImpl implements ThongKeService {

    private final ThongKeRepository thongKeRepository;

    public ThongKeServiceImpl(ThongKeRepository thongKeRepository) {
        this.thongKeRepository = thongKeRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<SinhVienGpaResponse> getThongKeGpa(Long lopId) {
        return thongKeRepository.findThongKeGpaNative(lopId);
    }
}

package com.demo.be.service.impl;

import com.demo.be.dto.monhoc.MonHocRequest;
import com.demo.be.dto.monhoc.MonHocResponse;
import com.demo.be.exception.ResourceNotFoundException;
import com.demo.be.model.Khoa;
import com.demo.be.model.MonHoc;
import com.demo.be.repository.KhoaRepository;
import com.demo.be.repository.MonHocRepository;
import com.demo.be.service.MonHocService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MonHocServiceImpl implements MonHocService {

    private final MonHocRepository monHocRepository;
    private final KhoaRepository khoaRepository;

    public MonHocServiceImpl(MonHocRepository monHocRepository, KhoaRepository khoaRepository) {
        this.monHocRepository = monHocRepository;
        this.khoaRepository = khoaRepository;
    }

    @Override
    public List<MonHocResponse> findAll() {
        return monHocRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Override
    public MonHocResponse findById(Long id) {
        return toResponse(getMonHoc(id));
    }

    @Override
    public List<MonHocResponse> findActive() {
        return monHocRepository.findByActiveTrueOrderByMaMonHocAsc().stream().map(this::toResponse).toList();
    }

    @Override
    public MonHocResponse create(MonHocRequest request) {
        MonHoc monHoc = new MonHoc();
        apply(request, monHoc);
        return toResponse(monHocRepository.save(monHoc));
    }

    @Override
    public MonHocResponse update(Long id, MonHocRequest request) {
        MonHoc monHoc = getMonHoc(id);
        apply(request, monHoc);
        return toResponse(monHocRepository.save(monHoc));
    }

    @Override
    public void delete(Long id) {
        monHocRepository.delete(getMonHoc(id));
    }

    private MonHoc getMonHoc(Long id) {
        return monHocRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("MonHoc not found with id " + id));
    }

    private void apply(MonHocRequest request, MonHoc monHoc) {
        Khoa khoa = khoaRepository.findById(request.khoaId())
                .orElseThrow(() -> new ResourceNotFoundException("Khoa not found with id " + request.khoaId()));
        monHoc.setMaMonHoc(request.maMonHoc());
        monHoc.setTenMonHoc(request.tenMonHoc());
        monHoc.setSoTinChi(request.soTinChi());
        monHoc.setSoTietLyThuyet(request.soTietLyThuyet());
        monHoc.setSoTietThucHanh(request.soTietThucHanh());
        monHoc.setMoTa(request.moTa());
        monHoc.setMonHocTienQuyet(request.monHocTienQuyet());
        monHoc.setKhoa(khoa);
        monHoc.setActive(request.active() == null || request.active());
    }

    private MonHocResponse toResponse(MonHoc monHoc) {
        Khoa khoa = monHoc.getKhoa();
        return new MonHocResponse(
                monHoc.getId(),
                monHoc.getMaMonHoc(),
                monHoc.getTenMonHoc(),
                monHoc.getSoTinChi(),
                monHoc.getSoTietLyThuyet(),
                monHoc.getSoTietThucHanh(),
                monHoc.getMoTa(),
                monHoc.getMonHocTienQuyet(),
                khoa != null ? khoa.getId() : null,
                khoa != null ? khoa.getTenKhoa() : null,
                monHoc.isActive()
        );
    }
}

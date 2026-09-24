package com.demo.be.service;

import com.demo.be.dto.khoa.KhoaRequest;
import com.demo.be.dto.khoa.KhoaResponse;
import com.demo.be.exception.ResourceNotFoundException;
import com.demo.be.model.Khoa;
import com.demo.be.repository.KhoaRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class KhoaService {

    private final KhoaRepository khoaRepository;

    public KhoaService(KhoaRepository khoaRepository) {
        this.khoaRepository = khoaRepository;
    }

    public List<KhoaResponse> findAll() {
        return khoaRepository.findAll().stream().map(this::toResponse).toList();
    }

    public KhoaResponse findById(Long id) {
        return toResponse(getKhoa(id));
    }

    public KhoaResponse create(KhoaRequest request) {
        Khoa khoa = new Khoa();
        apply(request, khoa);
        return toResponse(khoaRepository.save(khoa));
    }

    public KhoaResponse update(Long id, KhoaRequest request) {
        Khoa khoa = getKhoa(id);
        apply(request, khoa);
        return toResponse(khoaRepository.save(khoa));
    }

    public void delete(Long id) {
        Khoa khoa = getKhoa(id);
        khoaRepository.delete(khoa);
    }

    private Khoa getKhoa(Long id) {
        return khoaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Khoa not found with id " + id));
    }

    private void apply(KhoaRequest request, Khoa khoa) {
        khoa.setMaKhoa(request.maKhoa());
        khoa.setTenKhoa(request.tenKhoa());
        khoa.setMoTa(request.moTa());
        khoa.setActive(request.active() == null || request.active());
    }

    private KhoaResponse toResponse(Khoa khoa) {
        return new KhoaResponse(khoa.getId(), khoa.getMaKhoa(), khoa.getTenKhoa(), khoa.getMoTa(), khoa.isActive());
    }
}

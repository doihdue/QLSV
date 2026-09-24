package com.demo.be.service;

import com.demo.be.dto.lop.LopRequest;
import com.demo.be.dto.lop.LopResponse;
import com.demo.be.exception.ResourceNotFoundException;
import com.demo.be.model.Khoa;
import com.demo.be.model.Lop;
import com.demo.be.repository.KhoaRepository;
import com.demo.be.repository.LopRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class LopService {

    private final LopRepository lopRepository;
    private final KhoaRepository khoaRepository;

    public LopService(LopRepository lopRepository, KhoaRepository khoaRepository) {
        this.lopRepository = lopRepository;
        this.khoaRepository = khoaRepository;
    }

    public List<LopResponse> findAll() {
        return lopRepository.findAll().stream().map(this::toResponse).toList();
    }

    public LopResponse findById(Long id) {
        return toResponse(getLop(id));
    }

    public LopResponse create(LopRequest request) {
        Lop lop = new Lop();
        apply(request, lop);
        return toResponse(lopRepository.save(lop));
    }

    public LopResponse update(Long id, LopRequest request) {
        Lop lop = getLop(id);
        apply(request, lop);
        return toResponse(lopRepository.save(lop));
    }

    public void delete(Long id) {
        lopRepository.delete(getLop(id));
    }

    private Lop getLop(Long id) {
        return lopRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lop not found with id " + id));
    }

    private void apply(LopRequest request, Lop lop) {
        Khoa khoa = khoaRepository.findById(request.khoaId())
                .orElseThrow(() -> new ResourceNotFoundException("Khoa not found with id " + request.khoaId()));
        lop.setMaLop(request.maLop());
        lop.setTenLop(request.tenLop());
        lop.setNienKhoa(request.nienKhoa());
        lop.setSiSoToiDa(request.siSoToiDa());
        lop.setKhoa(khoa);
        lop.setActive(request.active() == null || request.active());
    }

    private LopResponse toResponse(Lop lop) {
        Khoa khoa = lop.getKhoa();
        return new LopResponse(
                lop.getId(),
                lop.getMaLop(),
                lop.getTenLop(),
                lop.getNienKhoa(),
                lop.getSiSoToiDa(),
                khoa != null ? khoa.getId() : null,
                khoa != null ? khoa.getTenKhoa() : null,
                lop.isActive()
        );
    }
}

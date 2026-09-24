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

    public String generateNextMaLop(Long khoaId, String nienKhoa) {
        Khoa khoa = khoaRepository.findById(khoaId)
                .orElseThrow(() -> new ResourceNotFoundException("Khoa not found with id " + khoaId));
        String maKhoa = khoa.getMaKhoa() != null ? khoa.getMaKhoa().trim().toUpperCase() : "";

        String year = String.valueOf(java.time.LocalDate.now().getYear());
        if (nienKhoa != null && !nienKhoa.isBlank()) {
            java.util.regex.Matcher m = java.util.regex.Pattern.compile("\\d{4}").matcher(nienKhoa);
            if (m.find()) {
                year = m.group();
            }
        }

        String prefix = "D" + year + maKhoa;
        List<Lop> existingLops = lopRepository.findByMaLopStartingWith(prefix);

        int maxSeq = 0;
        for (Lop l : existingLops) {
            String code = l.getMaLop();
            if (code != null && code.startsWith(prefix)) {
                String suffix = code.substring(prefix.length());
                try {
                    int seq = Integer.parseInt(suffix);
                    if (seq > maxSeq) {
                        maxSeq = seq;
                    }
                } catch (NumberFormatException ignored) {
                }
            }
        }

        int nextSeq = maxSeq + 1;
        return prefix + String.format("%02d", nextSeq);
    }

    private void apply(LopRequest request, Lop lop) {
        Khoa khoa = khoaRepository.findById(request.khoaId())
                .orElseThrow(() -> new ResourceNotFoundException("Khoa not found with id " + request.khoaId()));
        
        String maLop = request.maLop();
        if (maLop == null || maLop.isBlank()) {
            if (lop.getMaLop() == null || lop.getMaLop().isBlank()) {
                maLop = generateNextMaLop(request.khoaId(), request.nienKhoa());
            } else {
                maLop = lop.getMaLop();
            }
        }
        lop.setMaLop(maLop.trim());
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

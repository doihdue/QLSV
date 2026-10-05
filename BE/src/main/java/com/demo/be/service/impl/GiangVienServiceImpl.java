package com.demo.be.service.impl;

import com.demo.be.dto.giangvien.GiangVienRequest;
import com.demo.be.dto.giangvien.GiangVienResponse;
import com.demo.be.exception.ResourceNotFoundException;
import com.demo.be.model.AppUser;
import com.demo.be.model.GiangVien;
import com.demo.be.model.Khoa;
import com.demo.be.model.Role;
import com.demo.be.repository.GiangVienRepository;
import com.demo.be.repository.KhoaRepository;
import com.demo.be.repository.UserRepository;
import com.demo.be.service.GiangVienService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.util.List;
import java.util.regex.Pattern;

@Service
public class GiangVienServiceImpl implements GiangVienService {

    private final GiangVienRepository giangVienRepository;
    private final KhoaRepository khoaRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public GiangVienServiceImpl(
            GiangVienRepository giangVienRepository,
            KhoaRepository khoaRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.giangVienRepository = giangVienRepository;
        this.khoaRepository = khoaRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public List<GiangVienResponse> findAll() {
        return giangVienRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Override
    public GiangVienResponse findById(Long id) {
        return toResponse(getGiangVien(id));
    }

    @Override
    @Transactional
    public GiangVienResponse create(GiangVienRequest request) {
        GiangVien giangVien = new GiangVien();
        apply(request, giangVien);
        GiangVien saved = giangVienRepository.save(giangVien);

        // Tạo tài khoản AppUser cho giảng viên để đăng nhập ngay (mật khẩu mặc định: gv123456)
        if (userRepository.findByUsername(saved.getMaGiangVien()).isEmpty()) {
            AppUser lecturerUser = new AppUser();
            lecturerUser.setUsername(saved.getMaGiangVien());
            lecturerUser.setFullName(saved.getHoTen());
            lecturerUser.setPassword(passwordEncoder.encode("gv123456"));
            lecturerUser.setRole(Role.LECTURER);
            lecturerUser.setEnabled(saved.isActive());
            userRepository.save(lecturerUser);
        }

        return toResponse(saved);
    }

    @Override
    @Transactional
    public GiangVienResponse update(Long id, GiangVienRequest request) {
        GiangVien giangVien = getGiangVien(id);
        String oldMa = giangVien.getMaGiangVien();
        apply(request, giangVien);
        GiangVien saved = giangVienRepository.save(giangVien);

        // Đồng bộ AppUser
        userRepository.findByUsername(oldMa).ifPresent(u -> {
            u.setUsername(saved.getMaGiangVien());
            u.setFullName(saved.getHoTen());
            u.setEnabled(saved.isActive());
            userRepository.save(u);
        });

        return toResponse(saved);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        GiangVien giangVien = getGiangVien(id);
        userRepository.findByUsername(giangVien.getMaGiangVien()).ifPresent(userRepository::delete);
        giangVienRepository.delete(giangVien);
    }

    private GiangVien getGiangVien(Long id) {
        return giangVienRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("GiangVien not found with id " + id));
    }

    @Override
    public String generateNextMaGiangVien(Long khoaId) {
        Khoa khoa = khoaRepository.findById(khoaId)
                .orElseThrow(() -> new ResourceNotFoundException("Khoa not found with id " + khoaId));
        String maKhoa = (khoa.getMaKhoa() != null) ? khoa.getMaKhoa().trim().toUpperCase() : "";
        String prefix = "GV" + maKhoa;

        List<GiangVien> existing = giangVienRepository.findByMaGiangVienStartingWith(prefix);
        int maxSeq = 0;
        for (GiangVien gv : existing) {
            String code = gv.getMaGiangVien();
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
        return prefix + String.format("%03d", nextSeq);
    }

    private void apply(GiangVienRequest request, GiangVien giangVien) {
        Khoa khoa = khoaRepository.findById(request.khoaId())
                .orElseThrow(() -> new ResourceNotFoundException("Khoa not found with id " + request.khoaId()));
        giangVien.setHoTen(request.hoTen());

        String maGv = request.maGiangVien();
        if (maGv == null || maGv.isBlank()) {
            if (giangVien.getMaGiangVien() == null || giangVien.getMaGiangVien().isBlank()) {
                maGv = generateNextMaGiangVien(request.khoaId());
            } else {
                maGv = giangVien.getMaGiangVien();
            }
        }
        giangVien.setMaGiangVien(maGv.trim());

        String email = request.email();
        if (email == null || email.isBlank()) {
            if (giangVien.getEmail() == null || giangVien.getEmail().isBlank()) {
                email = generateEmailFromNameAndCode(request.hoTen(), giangVien.getMaGiangVien());
            } else {
                email = giangVien.getEmail();
            }
        }
        giangVien.setEmail(email.trim());

        giangVien.setSoDienThoai(request.soDienThoai());
        giangVien.setHocVi(request.hocVi());
        giangVien.setChuyenMon(request.chuyenMon());
        giangVien.setKhoa(khoa);
        giangVien.setActive(request.active() == null || request.active());
    }

    private String generateEmailFromNameAndCode(String fullName, String code) {
        if (fullName == null || fullName.isBlank()) {
            return (code != null ? code.toLowerCase() : "gv") + "@university.edu.vn";
        }
        String nfdNormalizedString = Normalizer.normalize(fullName.trim(), Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        String ascii = pattern.matcher(nfdNormalizedString).replaceAll("").replace('đ', 'd').replace('Đ', 'D').toLowerCase();

        String[] parts = ascii.split("\\s+");
        if (parts.length == 0) {
            return (code != null ? code.toLowerCase() : "gv") + "@university.edu.vn";
        }
        String lastName = parts[parts.length - 1];
        StringBuilder initials = new StringBuilder();
        for (int i = 0; i < parts.length - 1; i++) {
            if (!parts[i].isEmpty()) {
                initials.append(parts[i].charAt(0));
            }
        }
        String cleanCode = code != null ? code.toLowerCase().replaceAll("[^a-z0-9]", "") : "";
        return lastName + initials + "." + cleanCode + "@university.edu.vn";
    }

    private GiangVienResponse toResponse(GiangVien giangVien) {
        return new GiangVienResponse(
                giangVien.getId(),
                giangVien.getMaGiangVien(),
                giangVien.getHoTen(),
                giangVien.getEmail(),
                giangVien.getSoDienThoai(),
                giangVien.getHocVi(),
                giangVien.getChuyenMon(),
                giangVien.getKhoa() != null ? giangVien.getKhoa().getId() : null,
                giangVien.getKhoa() != null ? giangVien.getKhoa().getTenKhoa() : null,
                giangVien.isActive()
        );
    }
}

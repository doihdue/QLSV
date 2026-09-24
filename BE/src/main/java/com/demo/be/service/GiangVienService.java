package com.demo.be.service;

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
import java.util.List;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GiangVienService {

    private final GiangVienRepository giangVienRepository;
    private final KhoaRepository khoaRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public GiangVienService(
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

    public List<GiangVienResponse> findAll() {
        return giangVienRepository.findAll().stream().map(this::toResponse).toList();
    }

    public GiangVienResponse findById(Long id) {
        return toResponse(getGiangVien(id));
    }

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

    private void apply(GiangVienRequest request, GiangVien giangVien) {
        Khoa khoa = khoaRepository.findById(request.khoaId())
                .orElseThrow(() -> new ResourceNotFoundException("Khoa not found with id " + request.khoaId()));
        giangVien.setMaGiangVien(request.maGiangVien());
        giangVien.setHoTen(request.hoTen());
        giangVien.setEmail(request.email());
        giangVien.setSoDienThoai(request.soDienThoai());
        giangVien.setHocVi(request.hocVi());
        giangVien.setChuyenMon(request.chuyenMon());
        giangVien.setKhoa(khoa);
        giangVien.setActive(request.active() == null || request.active());
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

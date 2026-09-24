package com.demo.be.service;

import com.demo.be.dto.sinhvien.SinhVienRequest;
import com.demo.be.dto.sinhvien.SinhVienResponse;
import com.demo.be.dto.sinhvien.StudentProfileUpdateRequest;
import com.demo.be.exception.ResourceNotFoundException;
import com.demo.be.model.Khoa;
import com.demo.be.model.Lop;
import com.demo.be.model.SinhVien;
import com.demo.be.model.AppUser;
import com.demo.be.model.Role;
import com.demo.be.repository.DangKyMonHocRepository;
import com.demo.be.repository.LopRepository;
import com.demo.be.repository.QuanLyDiemRepository;
import com.demo.be.repository.SinhVienRepository;
import com.demo.be.repository.UserRepository;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.regex.Pattern;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SinhVienService {

    private static final DateTimeFormatter DOB_FORMATTER = DateTimeFormatter.ofPattern("ddMMyyyy");

    private final SinhVienRepository sinhVienRepository;
    private final LopRepository lopRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final DangKyMonHocRepository dangKyMonHocRepository;
    private final QuanLyDiemRepository quanLyDiemRepository;

    public SinhVienService(
            SinhVienRepository sinhVienRepository,
            LopRepository lopRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            DangKyMonHocRepository dangKyMonHocRepository,
            QuanLyDiemRepository quanLyDiemRepository
    ) {
        this.sinhVienRepository = sinhVienRepository;
        this.lopRepository = lopRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.dangKyMonHocRepository = dangKyMonHocRepository;
        this.quanLyDiemRepository = quanLyDiemRepository;
    }

    public List<SinhVienResponse> findAll() {
        return sinhVienRepository.findAll().stream().map(this::toResponse).toList();
    }

    public SinhVienResponse findById(Long id) {
        return toResponse(getSinhVien(id));
    }

    public SinhVienResponse findByMssv(String mssv) {
        return toResponse(sinhVienRepository.findByMssv(mssv)
                .orElseThrow(() -> new ResourceNotFoundException("SinhVien not found with MSSV " + mssv)));
    }

    @Transactional
    public SinhVienResponse updateProfile(String mssv, StudentProfileUpdateRequest request) {
        SinhVien sinhVien = sinhVienRepository.findByMssv(mssv)
                .orElseThrow(() -> new ResourceNotFoundException("SinhVien not found with MSSV " + mssv));
        sinhVien.setEmail(request.email());
        sinhVien.setSoDienThoai(request.soDienThoai());
        sinhVien.setDiaChi(request.diaChi());
        return toResponse(sinhVienRepository.save(sinhVien));
    }

    @Transactional
    public SinhVienResponse create(SinhVienRequest request) {
        SinhVien sinhVien = new SinhVien();
        apply(request, sinhVien);
        SinhVien saved = sinhVienRepository.save(sinhVien);

        // Tạo tài khoản AppUser cho sinh viên để đăng nhập ngay (mật khẩu mặc định: ngày sinh ddMMyyyy)
        if (userRepository.findByUsername(saved.getMssv()).isEmpty()) {
            AppUser studentUser = new AppUser();
            studentUser.setUsername(saved.getMssv());
            studentUser.setFullName(saved.getHoTen());

            String defaultPassword = saved.getNgaySinh() != null
                    ? saved.getNgaySinh().format(DOB_FORMATTER)
                    : "student123";

            studentUser.setPassword(passwordEncoder.encode(defaultPassword));
            studentUser.setRole(Role.STUDENT);
            studentUser.setEnabled(saved.isActive());
            userRepository.save(studentUser);
        }

        return toResponse(saved);
    }

    @Transactional
    public SinhVienResponse update(Long id, SinhVienRequest request) {
        SinhVien sinhVien = getSinhVien(id);
        String oldMssv = sinhVien.getMssv();
        apply(request, sinhVien);
        SinhVien saved = sinhVienRepository.save(sinhVien);

        // Đồng bộ AppUser (nếu chưa có thì tự động tạo mới)
        userRepository.findByUsername(oldMssv).ifPresentOrElse(
                u -> {
                    u.setUsername(saved.getMssv());
                    u.setFullName(saved.getHoTen());
                    u.setEnabled(saved.isActive());
                    userRepository.save(u);
                },
                () -> {
                    AppUser studentUser = new AppUser();
                    studentUser.setUsername(saved.getMssv());
                    studentUser.setFullName(saved.getHoTen());
                    String defaultPassword = saved.getNgaySinh() != null
                            ? saved.getNgaySinh().format(DOB_FORMATTER)
                            : "student123";
                    studentUser.setPassword(passwordEncoder.encode(defaultPassword));
                    studentUser.setRole(Role.STUDENT);
                    studentUser.setEnabled(saved.isActive());
                    userRepository.save(studentUser);
                }
        );

        return toResponse(saved);
    }

    @Transactional
    public String resetPassword(Long id) {
        SinhVien sinhVien = getSinhVien(id);
        String defaultPassword = sinhVien.getNgaySinh() != null
                ? sinhVien.getNgaySinh().format(DOB_FORMATTER)
                : "student123";

        AppUser user = userRepository.findByUsername(sinhVien.getMssv()).orElseGet(() -> {
            AppUser u = new AppUser();
            u.setUsername(sinhVien.getMssv());
            u.setFullName(sinhVien.getHoTen());
            u.setRole(Role.STUDENT);
            u.setEnabled(sinhVien.isActive());
            return u;
        });

        user.setPassword(passwordEncoder.encode(defaultPassword));
        userRepository.save(user);
        return defaultPassword;
    }

    @Transactional
    public void delete(Long id) {
        SinhVien sinhVien = getSinhVien(id);
        String mssv = sinhVien.getMssv();

        // Xóa các điểm và đăng ký môn học liên quan
        var registrations = dangKyMonHocRepository.findBySinhVien_Id(id);
        for (var reg : registrations) {
            quanLyDiemRepository.findByDangKyMonHoc_Id(reg.getId()).ifPresent(quanLyDiemRepository::delete);
            dangKyMonHocRepository.delete(reg);
        }

        // Xóa AppUser nếu có
        userRepository.findByUsername(mssv).ifPresent(userRepository::delete);

        sinhVienRepository.delete(sinhVien);
    }

    private SinhVien getSinhVien(Long id) {
        return sinhVienRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SinhVien not found with id " + id));
    }

    public String generateNextMssv(Long lopId, LocalDate ngayNhapHoc) {
        Lop lop = lopRepository.findById(lopId)
                .orElseThrow(() -> new ResourceNotFoundException("Lop not found with id " + lopId));
        Khoa khoa = lop.getKhoa();
        String maKhoa = (khoa != null && khoa.getMaKhoa() != null) ? khoa.getMaKhoa().trim().toUpperCase() : "";

        String year = ngayNhapHoc != null ? String.valueOf(ngayNhapHoc.getYear()) : String.valueOf(LocalDate.now().getYear());
        String prefix = year + maKhoa;

        List<SinhVien> existing = sinhVienRepository.findByMssvStartingWith(prefix);
        int maxSeq = 0;
        for (SinhVien sv : existing) {
            String code = sv.getMssv();
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

    public static String generateEmailFromNameAndMssv(String hoTen, String mssv) {
        if (mssv == null || mssv.isBlank()) {
            return "";
        }
        String cleanMssv = mssv.trim();
        if (hoTen == null || hoTen.isBlank()) {
            return "sv." + cleanMssv + "@stu.edu.vn";
        }
        String normalized = Normalizer.normalize(hoTen.trim(), Normalizer.Form.NFD);
        String withoutAccents = Pattern.compile("\\p{InCombiningDiacriticalMarks}+").matcher(normalized).replaceAll("");
        withoutAccents = withoutAccents.replace("đ", "d").replace("Đ", "d");

        String lettersOnly = withoutAccents.replaceAll("[^a-zA-Z\\s]", " ").trim().toLowerCase();
        String[] words = lettersOnly.split("\\s+");
        if (words.length == 0 || words[0].isBlank()) {
            return "sv." + cleanMssv + "@stu.edu.vn";
        }

        String prefix;
        if (words.length == 1) {
            prefix = words[0];
        } else {
            String firstName = words[words.length - 1];
            StringBuilder initials = new StringBuilder();
            for (int i = 0; i < words.length - 1; i++) {
                if (!words[i].isEmpty()) {
                    initials.append(words[i].charAt(0));
                }
            }
            prefix = firstName + initials.toString();
        }
        return prefix + "." + cleanMssv + "@stu.edu.vn";
    }

    private void apply(SinhVienRequest request, SinhVien sinhVien) {
        Lop lop = lopRepository.findById(request.lopId())
                .orElseThrow(() -> new ResourceNotFoundException("Lop not found with id " + request.lopId()));
        sinhVien.setHoTen(request.hoTen());
        sinhVien.setNgaySinh(request.ngaySinh());
        sinhVien.setGioiTinh(request.gioiTinh());

        String mssv = request.mssv();
        if (mssv == null || mssv.isBlank()) {
            if (sinhVien.getMssv() == null || sinhVien.getMssv().isBlank()) {
                mssv = generateNextMssv(request.lopId(), request.ngayNhapHoc());
            } else {
                mssv = sinhVien.getMssv();
            }
        }
        sinhVien.setMssv(mssv.trim());

        String email = request.email();
        if (email == null || email.isBlank()) {
            if (sinhVien.getEmail() == null || sinhVien.getEmail().isBlank()) {
                email = generateEmailFromNameAndMssv(request.hoTen(), sinhVien.getMssv());
            } else {
                email = sinhVien.getEmail();
            }
        }
        sinhVien.setEmail(email.trim());

        sinhVien.setSoDienThoai(request.soDienThoai());
        sinhVien.setDiaChi(request.diaChi());
        sinhVien.setNgayNhapHoc(request.ngayNhapHoc());
        sinhVien.setLop(lop);
        sinhVien.setActive(request.active() == null || request.active());
    }

    private SinhVienResponse toResponse(SinhVien sinhVien) {
        Lop lop = sinhVien.getLop();
        Khoa khoa = lop != null ? lop.getKhoa() : null;
        String khoaHoc = "";
        if (sinhVien.getNgayNhapHoc() != null) {
            khoaHoc = String.valueOf(sinhVien.getNgayNhapHoc().getYear());
        } else {
            khoaHoc = MonHocMoService.extractKhoaHocFromMssv(sinhVien.getMssv());
        }
        return new SinhVienResponse(
                sinhVien.getId(),
                sinhVien.getMssv(),
                sinhVien.getHoTen(),
                sinhVien.getNgaySinh(),
                sinhVien.getGioiTinh(),
                sinhVien.getEmail(),
                sinhVien.getSoDienThoai(),
                sinhVien.getDiaChi(),
                sinhVien.getNgayNhapHoc(),
                lop != null ? lop.getId() : null,
                lop != null ? lop.getTenLop() : null,
                khoa != null ? khoa.getId() : null,
                khoa != null ? khoa.getTenKhoa() : null,
                khoaHoc,
                sinhVien.isActive()
        );
    }
}

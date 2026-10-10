package com.demo.be.config;

import com.demo.be.model.AppUser;
import com.demo.be.model.DotRenLuyen;
import com.demo.be.model.Role;
import com.demo.be.repository.DotRenLuyenRepository;
import com.demo.be.repository.GiangVienRepository;
import com.demo.be.repository.SinhVienRepository;
import com.demo.be.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);
    private static final DateTimeFormatter DOB_FORMATTER = DateTimeFormatter.ofPattern("ddMMyyyy");

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final GiangVienRepository giangVienRepository;
    private final SinhVienRepository sinhVienRepository;
    private final DotRenLuyenRepository dotRenLuyenRepository;

    public DataInitializer(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            GiangVienRepository giangVienRepository,
            SinhVienRepository sinhVienRepository,
            DotRenLuyenRepository dotRenLuyenRepository
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.giangVienRepository = giangVienRepository;
        this.sinhVienRepository = sinhVienRepository;
        this.dotRenLuyenRepository = dotRenLuyenRepository;
    }

    @Override
    public void run(String... args) {
        initDefaultAdmin();
        initLecturerAccounts();
        initStudentAccounts();
        initDotRenLuyen();
    }

    private void initDefaultAdmin() {
        if (userRepository.findByUsername("admin").isEmpty()) {
            AppUser admin = new AppUser();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setFullName("Quản trị hệ thống");
            admin.setRole(Role.ADMIN);
            admin.setEnabled(true);
            userRepository.save(admin);
            log.info("Initialized default admin account: admin / admin123");
        }
    }

    private void initLecturerAccounts() {
        giangVienRepository.findAll().forEach(gv -> {
            if (userRepository.findByUsername(gv.getMaGiangVien()).isEmpty()) {
                AppUser user = new AppUser();
                user.setUsername(gv.getMaGiangVien());
                user.setFullName(gv.getHoTen());
                user.setPassword(passwordEncoder.encode("gv123456"));
                user.setRole(Role.LECTURER);
                user.setEnabled(gv.isActive());
                userRepository.save(user);
                log.info("Initialized lecturer account: {} / gv123456", gv.getMaGiangVien());
            }
        });
    }

    private void initStudentAccounts() {
        sinhVienRepository.findAll().forEach(sv -> {
            if (userRepository.findByUsername(sv.getMssv()).isEmpty()) {
                AppUser user = new AppUser();
                user.setUsername(sv.getMssv());
                user.setFullName(sv.getHoTen());
                String defaultPassword = sv.getNgaySinh() != null
                        ? sv.getNgaySinh().format(DOB_FORMATTER)
                        : "student123";
                user.setPassword(passwordEncoder.encode(defaultPassword));
                user.setRole(Role.STUDENT);
                user.setEnabled(sv.isActive());
                userRepository.save(user);
                log.info("Initialized student account: {} / {}", sv.getMssv(), defaultPassword);
            }
        });
    }

    private void initDotRenLuyen() {
        if (dotRenLuyenRepository.count() == 0) {
            DotRenLuyen dot1 = new DotRenLuyen(
                    "Đợt đánh giá rèn luyện Học kỳ 1 (2026-2027)",
                    "1",
                    "2026-2027",
                    LocalDate.now().minusDays(30),
                    LocalDate.now().plusDays(30),
                    true,
                    "Sinh viên toàn trường tự đánh giá kết quả rèn luyện học kỳ 1 trước hạn chót."
            );
            dotRenLuyenRepository.save(dot1);

            DotRenLuyen dot2 = new DotRenLuyen(
                    "Đợt đánh giá rèn luyện Học kỳ 2 (2025-2026)",
                    "2",
                    "2025-2026",
                    LocalDate.now().minusMonths(6),
                    LocalDate.now().minusMonths(3),
                    false,
                    "Đợt đánh giá học kỳ 2 năm học 2025-2026 đã kết thúc và lưu trữ hồ sơ."
            );
            dotRenLuyenRepository.save(dot2);
            log.info("Initialized default DotRenLuyen records for testing.");
        }
    }
}

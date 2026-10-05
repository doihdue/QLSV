package com.demo.be.service;

import com.demo.be.dto.sinhvien.SinhVienRequest;
import com.demo.be.dto.sinhvien.SinhVienResponse;
import com.demo.be.dto.sinhvien.StudentProfileUpdateRequest;

import java.text.Normalizer;
import java.time.LocalDate;
import java.util.List;
import java.util.regex.Pattern;

public interface SinhVienService {

    List<SinhVienResponse> findAll();

    SinhVienResponse findById(Long id);

    SinhVienResponse findByMssv(String mssv);

    SinhVienResponse updateProfile(String mssv, StudentProfileUpdateRequest request);

    SinhVienResponse create(SinhVienRequest request);

    SinhVienResponse update(Long id, SinhVienRequest request);

    String resetPassword(Long id);

    void delete(Long id);

    String generateNextMssv(Long lopId, LocalDate ngayNhapHoc);

    /**
     * Tự động sinh email sinh viên chuẩn (vd: Nguyen Van An, MSSV: 2023001 -> anv2023001@stu.edu.vn)
     */
    static String generateEmailFromNameAndMssv(String hoTen, String mssv) {
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
}

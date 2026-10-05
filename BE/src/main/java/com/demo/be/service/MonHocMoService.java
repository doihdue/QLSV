package com.demo.be.service;

import com.demo.be.dto.monhocmo.MonHocMoRequest;
import com.demo.be.dto.monhocmo.MonHocMoResponse;
import com.demo.be.model.Lop;
import com.demo.be.model.SinhVien;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public interface MonHocMoService {

    List<MonHocMoResponse> findByFilters(String hocKy, String namHoc, Long khoaId, String khoaHoc, Long lopId);

    List<MonHocMoResponse> findForLop(Long lopId, String hocKy, String namHoc);

    MonHocMoResponse create(MonHocMoRequest request);

    MonHocMoResponse ganGiangVien(Long id, Long giangVienId);

    List<MonHocMoResponse> findByGiangVien(String maGiangVien, String hocKy, String namHoc);

    void delete(Long id);

    List<MonHocMoResponse> getMonHocMoForStudent(String mssv, String hocKy, String namHoc);

    String extractKhoaHoc(SinhVien sv);

    String extractKhoaHoc(Lop lop);

    /**
     * Trích xuất năm khóa từ Mã Sinh Viên (ví dụ: 2023CNTT001 -> "2023", B22DCCN001 -> "2022")
     */
    static String extractKhoaHocFromMssv(String mssv) {
        if (mssv == null || mssv.isBlank()) return "";
        Matcher m4 = Pattern.compile("^(\\d{4})").matcher(mssv.trim());
        if (m4.find()) {
            return m4.group(1);
        }
        Matcher m2 = Pattern.compile("^[A-Za-z]*(\\d{2})").matcher(mssv.trim());
        if (m2.find()) {
            return "20" + m2.group(1);
        }
        return "";
    }
}

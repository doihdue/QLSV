package com.demo.be.service;

import com.demo.be.dto.dangky.DangKyMonHocRequest;
import com.demo.be.dto.dangky.DangKyMonHocResponse;
import com.demo.be.dto.dangky.StudentDangKyMonHocRequest;
import java.util.List;

public interface DangKyMonHocService {
    List<DangKyMonHocResponse> findAll();
    DangKyMonHocResponse findById(Long id);
    List<DangKyMonHocResponse> findBySinhVien(Long sinhVienId);
    List<DangKyMonHocResponse> findBySinhVienAndSemester(String mssv, String hocKy, String namHoc);
    DangKyMonHocResponse createForStudent(String mssv, StudentDangKyMonHocRequest request);
    DangKyMonHocResponse create(DangKyMonHocRequest request);
    DangKyMonHocResponse update(Long id, DangKyMonHocRequest request);
    void deleteForStudent(String mssv, Long id);
    void delete(Long id);
    List<DangKyMonHocResponse> dangKyChoLopHanhChinh(Long lopId, Long monHocId, String hocKy, String namHoc);
}

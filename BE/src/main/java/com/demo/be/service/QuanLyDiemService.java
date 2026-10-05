package com.demo.be.service;

import com.demo.be.dto.diem.AdminDuyetDiemRequest;
import com.demo.be.dto.diem.LecturerGradeBatchRequest;
import com.demo.be.dto.diem.LecturerGradeItemDto;
import com.demo.be.dto.diem.PendingApprovalClassDto;
import com.demo.be.dto.diem.QuanLyDiemRequest;
import com.demo.be.dto.diem.QuanLyDiemResponse;

import java.util.List;

public interface QuanLyDiemService {

    List<QuanLyDiemResponse> findAll();

    QuanLyDiemResponse findById(Long id);

    List<QuanLyDiemResponse> findBySinhVien(Long sinhVienId);

    List<QuanLyDiemResponse> findBySinhVienAndSemester(String mssv, String hocKy, String namHoc);

    Long findBySinhVienMssv(String mssv);

    QuanLyDiemResponse create(QuanLyDiemRequest request);

    QuanLyDiemResponse update(Long id, QuanLyDiemRequest request);

    void delete(Long id);

    List<LecturerGradeItemDto> getStudentsAndGradesForMonHocMo(Long monHocMoId);

    List<LecturerGradeItemDto> saveGradesForMonHocMo(LecturerGradeBatchRequest request);

    List<PendingApprovalClassDto> getPendingApprovalClasses();

    void approveGrades(Long monHocMoId);

    void rejectGrades(Long monHocMoId, AdminDuyetDiemRequest request);
}

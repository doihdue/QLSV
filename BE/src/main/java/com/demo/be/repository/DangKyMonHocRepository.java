package com.demo.be.repository;

import com.demo.be.model.DangKyMonHoc;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DangKyMonHocRepository extends JpaRepository<DangKyMonHoc, Long> {
    boolean existsBySinhVien_IdAndMonHoc_IdAndHocKyAndNamHoc(Long sinhVienId, Long monHocId, String hocKy, String namHoc);

    List<DangKyMonHoc> findBySinhVien_Id(Long sinhVienId);

    List<DangKyMonHoc> findBySinhVien_IdAndHocKyAndNamHoc(Long sinhVienId, String hocKy, String namHoc);

    List<DangKyMonHoc> findByMonHoc_IdAndHocKyAndNamHocAndSinhVien_Lop_Id(Long monHocId, String hocKy, String namHoc, Long lopId);

    List<DangKyMonHoc> findByMonHoc_IdAndHocKyAndNamHoc(Long monHocId, String hocKy, String namHoc);
}

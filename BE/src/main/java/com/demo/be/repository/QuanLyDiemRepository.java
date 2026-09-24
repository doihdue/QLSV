package com.demo.be.repository;

import com.demo.be.model.QuanLyDiem;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuanLyDiemRepository extends JpaRepository<QuanLyDiem, Long> {
    Optional<QuanLyDiem> findByDangKyMonHoc_Id(Long dangKyMonHocId);

    List<QuanLyDiem> findByDangKyMonHoc_SinhVien_Id(Long sinhVienId);

    List<QuanLyDiem> findByDangKyMonHoc_SinhVien_IdAndDangKyMonHoc_HocKyAndDangKyMonHoc_NamHoc(
            Long sinhVienId, String hocKy, String namHoc);

    List<QuanLyDiem> findByDangKyMonHoc_IdIn(List<Long> dangKyMonHocIds);
}


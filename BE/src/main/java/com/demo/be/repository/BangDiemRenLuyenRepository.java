package com.demo.be.repository;

import com.demo.be.model.BangDiemRenLuyen;
import com.demo.be.model.TrangThaiDrl;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BangDiemRenLuyenRepository extends JpaRepository<BangDiemRenLuyen, Long> {
    Optional<BangDiemRenLuyen> findByDotRenLuyen_IdAndSinhVien_Id(Long dotId, Long sinhVienId);
    List<BangDiemRenLuyen> findByDotRenLuyen_IdAndLop_Id(Long dotId, Long lopId);
    List<BangDiemRenLuyen> findByDotRenLuyen_Id(Long dotId);
    List<BangDiemRenLuyen> findByDotRenLuyen_IdAndTrangThai(Long dotId, TrangThaiDrl trangThai);
    List<BangDiemRenLuyen> findByDotRenLuyen_IdAndLop_IdAndTrangThai(Long dotId, Long lopId, TrangThaiDrl trangThai);
}

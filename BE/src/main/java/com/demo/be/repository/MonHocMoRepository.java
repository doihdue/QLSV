package com.demo.be.repository;

import com.demo.be.model.MonHocMo;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MonHocMoRepository extends JpaRepository<MonHocMo, Long> {

    @Query("SELECT m FROM MonHocMo m WHERE m.hocKy = :hocKy AND m.namHoc = :namHoc "
            + "AND (:khoaId IS NULL OR m.khoa.id = :khoaId) "
            + "AND (:khoaHoc IS NULL OR m.khoaHoc = :khoaHoc OR m.khoaHoc = CONCAT('B', :khoaHoc) OR m.khoaHoc = CONCAT('D', :khoaHoc) OR m.khoaHoc LIKE CONCAT('%', :khoaHoc, '%')) "
            + "AND (:lopId IS NULL OR m.lop.id = :lopId)")
    List<MonHocMo> findByFilters(
            @Param("hocKy") String hocKy,
            @Param("namHoc") String namHoc,
            @Param("khoaId") Long khoaId,
            @Param("khoaHoc") String khoaHoc,
            @Param("lopId") Long lopId
    );

    @Query("SELECT m FROM MonHocMo m WHERE m.hocKy = :hocKy AND m.namHoc = :namHoc "
            + "AND m.khoa.id = :khoaId "
            + "AND (m.khoaHoc IS NULL OR m.khoaHoc = '' "
            + "     OR m.khoaHoc = :khoaHoc "
            + "     OR (:khoaNum IS NOT NULL AND :khoaNum <> '' AND (m.khoaHoc = :khoaNum "
            + "         OR m.khoaHoc = CONCAT('B', :khoaNum) "
            + "         OR m.khoaHoc = CONCAT('D', :khoaNum) "
            + "         OR m.khoaHoc = CONCAT('K', :khoaNum) "
            + "         OR m.khoaHoc LIKE CONCAT('%', :khoaNum, '%')))) "
            + "AND (m.lop.id = :lopId OR m.lop IS NULL)")
    List<MonHocMo> findForStudent(
            @Param("hocKy") String hocKy,
            @Param("namHoc") String namHoc,
            @Param("khoaId") Long khoaId,
            @Param("khoaHoc") String khoaHoc,
            @Param("khoaNum") String khoaNum,
            @Param("lopId") Long lopId
    );

    boolean existsByMonHoc_IdAndKhoa_IdAndKhoaHocAndHocKyAndNamHoc(
            Long monHocId,
            Long khoaId,
            String khoaHoc,
            String hocKy,
            String namHoc
    );

    List<MonHocMo> findByGiangVien_MaGiangVien(String maGiangVien);

    List<MonHocMo> findByGiangVien_MaGiangVienAndHocKyAndNamHoc(String maGiangVien, String hocKy, String namHoc);
}

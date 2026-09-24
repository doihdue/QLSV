package com.demo.be.repository;

import com.demo.be.dto.thongke.SinhVienGpaProjection;
import com.demo.be.model.SinhVien;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SinhVienRepository extends JpaRepository<SinhVien, Long> {
    Optional<SinhVien> findByMssv(String mssv);
    List<SinhVien> findByLop_Id(Long lopId);
    List<SinhVien> findByMssvStartingWith(String prefix);

    /**
     * API Native Query: Thống kê kết quả học tập & xếp hạng GPA sinh viên
     * Thực hiện truy vấn kết hợp nhiều bảng (sinh_vien, lop, khoa, dang_ky_mon_hoc, mon_hoc, quan_ly_diem)
     * Tính toán tín chỉ tích lũy, điểm trung bình có trọng số theo tín chỉ, và xếp loại học lực
     */
    @Query(value = """
            SELECT 
                sv.id AS sinhVienId,
                sv.mssv AS mssv,
                sv.ho_ten AS hoTen,
                l.ten_lop AS tenLop,
                k.ten_khoa AS tenKhoa,
                COUNT(qd.id) AS soMonHoc,
                COALESCE(SUM(CASE WHEN qd.dat = 1 THEN mh.so_tin_chi ELSE 0 END), 0) AS tinChiTichLuy,
                ROUND(COALESCE(SUM(qd.diem_tong_ket * mh.so_tin_chi) / NULLIF(SUM(CASE WHEN qd.diem_tong_ket IS NOT NULL THEN mh.so_tin_chi ELSE 0 END), 0), 0), 2) AS diemTrungBinh,
                CASE 
                    WHEN COUNT(qd.id) = 0 THEN N'Chưa có điểm'
                    WHEN ROUND(COALESCE(SUM(qd.diem_tong_ket * mh.so_tin_chi) / NULLIF(SUM(CASE WHEN qd.diem_tong_ket IS NOT NULL THEN mh.so_tin_chi ELSE 0 END), 0), 0), 2) >= 8.5 THEN N'Xuất sắc'
                    WHEN ROUND(COALESCE(SUM(qd.diem_tong_ket * mh.so_tin_chi) / NULLIF(SUM(CASE WHEN qd.diem_tong_ket IS NOT NULL THEN mh.so_tin_chi ELSE 0 END), 0), 0), 2) >= 7.0 THEN N'Khá / Giỏi'
                    WHEN ROUND(COALESCE(SUM(qd.diem_tong_ket * mh.so_tin_chi) / NULLIF(SUM(CASE WHEN qd.diem_tong_ket IS NOT NULL THEN mh.so_tin_chi ELSE 0 END), 0), 0), 2) >= 5.0 THEN N'Trung bình'
                    ELSE N'Yếu / Cảnh báo'
                END AS xepLoaiHocLuc
            FROM sinh_vien sv
            JOIN lop l ON sv.lop_id = l.id
            JOIN khoa k ON l.khoa_id = k.id
            LEFT JOIN dang_ky_mon_hoc dk ON dk.sinh_vien_id = sv.id
            LEFT JOIN mon_hoc mh ON dk.mon_hoc_id = mh.id
            LEFT JOIN quan_ly_diem qd ON qd.dang_ky_mon_hoc_id = dk.id AND qd.trang_thai_duyet = 'DA_DUYET'
            WHERE (:lopId IS NULL OR sv.lop_id = :lopId)
            GROUP BY sv.id, sv.mssv, sv.ho_ten, l.ten_lop, k.ten_khoa
            ORDER BY diemTrungBinh DESC, sv.mssv ASC
            """, nativeQuery = true)
    List<SinhVienGpaProjection> findThongKeGpaNative(@Param("lopId") Long lopId);
}

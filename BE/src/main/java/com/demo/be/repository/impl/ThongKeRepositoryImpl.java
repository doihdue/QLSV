package com.demo.be.repository.impl;

import com.demo.be.dto.thongke.SinhVienGpaResponse;
import com.demo.be.repository.ThongKeRepositoryCustom;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ThongKeRepositoryImpl implements ThongKeRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Thực thi truy vấn Native SQL thống kê kết quả học tập của sinh viên
     * và ánh xạ kết quả vào SinhVienGpaResponse thông qua @SqlResultSetMapping "SinhVienGpaMapping"
     */
    @Override
    @SuppressWarnings("unchecked")
    public List<SinhVienGpaResponse> findThongKeGpaNative(Long lopId) {
        StringBuilder sql = new StringBuilder("""
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
        """);

        if (lopId != null) {
            sql.append(" WHERE sv.lop_id = :lopId ");
        }

        sql.append("""
            GROUP BY sv.id, sv.mssv, sv.ho_ten, l.ten_lop, k.ten_khoa
            ORDER BY diemTrungBinh DESC, sv.mssv ASC
        """);

        // Gọi createNativeQuery với tên @SqlResultSetMapping "SinhVienGpaMapping"
        Query query = entityManager.createNativeQuery(sql.toString(), "SinhVienGpaMapping");
        if (lopId != null) {
            query.setParameter("lopId", lopId);
        }

        return query.getResultList();
    }
}

package com.demo.be.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;

@Entity
@Table(name = "dot_dang_ky")
public class DotDangKy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 20)
    private String hocKy = "1";

    @Column(nullable = false, length = 20)
    private String namHoc = "2026-2027";

    @Column(nullable = false, length = 255, columnDefinition = "NVARCHAR(255)")
    private String tenDot = "Đợt đăng ký tín chỉ Học kỳ 1 (2026-2027)";

    @Column(nullable = false)
    private boolean dangMo = true;

    @Column
    private LocalDate ngayBatDau;

    @Column
    private LocalDate ngayKetThuc;

    public DotDangKy() {
    }

    public DotDangKy(String hocKy, String namHoc, String tenDot, boolean dangMo) {
        this.hocKy = hocKy;
        this.namHoc = namHoc;
        this.tenDot = tenDot;
        this.dangMo = dangMo;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getHocKy() {
        return hocKy;
    }

    public void setHocKy(String hocKy) {
        this.hocKy = hocKy;
    }

    public String getNamHoc() {
        return namHoc;
    }

    public void setNamHoc(String namHoc) {
        this.namHoc = namHoc;
    }

    public String getTenDot() {
        return tenDot;
    }

    public void setTenDot(String tenDot) {
        this.tenDot = tenDot;
    }

    public boolean isDangMo() {
        return dangMo;
    }

    public void setDangMo(boolean dangMo) {
        this.dangMo = dangMo;
    }

    public LocalDate getNgayBatDau() {
        return ngayBatDau;
    }

    public void setNgayBatDau(LocalDate ngayBatDau) {
        this.ngayBatDau = ngayBatDau;
    }

    public LocalDate getNgayKetThuc() {
        return ngayKetThuc;
    }

    public void setNgayKetThuc(LocalDate ngayKetThuc) {
        this.ngayKetThuc = ngayKetThuc;
    }
}

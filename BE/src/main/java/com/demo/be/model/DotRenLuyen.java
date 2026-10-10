package com.demo.be.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;

@Entity
@Table(name = "dot_ren_luyen")
public class DotRenLuyen {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150, columnDefinition = "NVARCHAR(150)")
    private String tenDot;

    @Column(nullable = false, length = 20)
    private String hocKy = "1";

    @Column(nullable = false, length = 20)
    private String namHoc = "2026-2027";

    @Column(nullable = false)
    private LocalDate ngayBatDau;

    @Column(nullable = false)
    private LocalDate ngayKetThuc;

    @Column(nullable = false)
    private boolean dangMo = true;

    @Column(length = 255, columnDefinition = "NVARCHAR(255)")
    private String ghiChu;

    public DotRenLuyen() {
    }

    public DotRenLuyen(String tenDot, String hocKy, String namHoc, LocalDate ngayBatDau, LocalDate ngayKetThuc, boolean dangMo, String ghiChu) {
        this.tenDot = tenDot;
        this.hocKy = hocKy;
        this.namHoc = namHoc;
        this.ngayBatDau = ngayBatDau;
        this.ngayKetThuc = ngayKetThuc;
        this.dangMo = dangMo;
        this.ghiChu = ghiChu;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTenDot() {
        return tenDot;
    }

    public void setTenDot(String tenDot) {
        this.tenDot = tenDot;
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

    public boolean isDangMo() {
        return dangMo;
    }

    public void setDangMo(boolean dangMo) {
        this.dangMo = dangMo;
    }

    public String getGhiChu() {
        return ghiChu;
    }

    public void setGhiChu(String ghiChu) {
        this.ghiChu = ghiChu;
    }
}

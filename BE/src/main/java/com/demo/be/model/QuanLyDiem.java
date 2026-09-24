package com.demo.be.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "quan_ly_diem")
public class QuanLyDiem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dang_ky_mon_hoc_id", nullable = false, unique = true)
    private DangKyMonHoc dangKyMonHoc;

    @Column(precision = 5, scale = 2)
    private BigDecimal diemChuyenCan;

    @Column(precision = 5, scale = 2)
    private BigDecimal diemGiuaKy;

    @Column(precision = 5, scale = 2)
    private BigDecimal diemCuoiKy;

    @Column(precision = 5, scale = 2)
    private BigDecimal diemTongKet;

    @Column(length = 50, columnDefinition = "NVARCHAR(50)")
    private String xepLoai;

    @Column(nullable = false)
    private boolean dat = false;

    @Column(length = 30, columnDefinition = "NVARCHAR(30)")
    private String trangThaiDuyet = "BAN_NHAP"; // BAN_NHAP, CHO_DUYET, DA_DUYET, TU_CHOI

    @Column(length = 255, columnDefinition = "NVARCHAR(255)")
    private String ghiChuDuyet;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public DangKyMonHoc getDangKyMonHoc() {
        return dangKyMonHoc;
    }

    public void setDangKyMonHoc(DangKyMonHoc dangKyMonHoc) {
        this.dangKyMonHoc = dangKyMonHoc;
    }

    public BigDecimal getDiemChuyenCan() {
        return diemChuyenCan;
    }

    public void setDiemChuyenCan(BigDecimal diemChuyenCan) {
        this.diemChuyenCan = diemChuyenCan;
    }

    public BigDecimal getDiemGiuaKy() {
        return diemGiuaKy;
    }

    public void setDiemGiuaKy(BigDecimal diemGiuaKy) {
        this.diemGiuaKy = diemGiuaKy;
    }

    public BigDecimal getDiemCuoiKy() {
        return diemCuoiKy;
    }

    public void setDiemCuoiKy(BigDecimal diemCuoiKy) {
        this.diemCuoiKy = diemCuoiKy;
    }

    public BigDecimal getDiemTongKet() {
        return diemTongKet;
    }

    public void setDiemTongKet(BigDecimal diemTongKet) {
        this.diemTongKet = diemTongKet;
    }

    public String getXepLoai() {
        return xepLoai;
    }

    public void setXepLoai(String xepLoai) {
        this.xepLoai = xepLoai;
    }

    public boolean isDat() {
        return dat;
    }

    public void setDat(boolean dat) {
        this.dat = dat;
    }

    public String getTrangThaiDuyet() {
        return trangThaiDuyet;
    }

    public void setTrangThaiDuyet(String trangThaiDuyet) {
        this.trangThaiDuyet = trangThaiDuyet;
    }

    public String getGhiChuDuyet() {
        return ghiChuDuyet;
    }

    public void setGhiChuDuyet(String ghiChuDuyet) {
        this.ghiChuDuyet = ghiChuDuyet;
    }
}

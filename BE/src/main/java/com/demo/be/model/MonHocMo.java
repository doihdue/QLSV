package com.demo.be.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
@Entity
@Table(name = "mon_hoc_mo")
public class MonHocMo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mon_hoc_id", nullable = false)
    private MonHoc monHoc;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "khoa_id", nullable = false)
    private Khoa khoa;

    @Column(nullable = false, length = 50, columnDefinition = "NVARCHAR(50)")
    private String khoaHoc; // Ví dụ: "22", "B22", "D22", "D23"

    @Column(nullable = false, length = 20)
    private String hocKy = "1";

    @Column(nullable = false, length = 20)
    private String namHoc = "2026-2027";

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lop_id")
    private Lop lop; // Tùy chọn: nếu chỉ định riêng cho 1 lớp hành chính cụ thể

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "giang_vien_id")
    private GiangVien giangVien; // Giáo viên bộ môn phụ trách

    @Column(length = 255, columnDefinition = "NVARCHAR(255)")
    private String ghiChu;

    public MonHocMo() {
    }

    public MonHocMo(MonHoc monHoc, Khoa khoa, String khoaHoc, String hocKy, String namHoc, Lop lop, String ghiChu) {
        this.monHoc = monHoc;
        this.khoa = khoa;
        this.khoaHoc = khoaHoc;
        this.hocKy = hocKy;
        this.namHoc = namHoc;
        this.lop = lop;
        this.ghiChu = ghiChu;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public MonHoc getMonHoc() {
        return monHoc;
    }

    public void setMonHoc(MonHoc monHoc) {
        this.monHoc = monHoc;
    }

    public Khoa getKhoa() {
        return khoa;
    }

    public void setKhoa(Khoa khoa) {
        this.khoa = khoa;
    }

    public String getKhoaHoc() {
        return khoaHoc;
    }

    public void setKhoaHoc(String khoaHoc) {
        this.khoaHoc = khoaHoc;
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

    public Lop getLop() {
        return lop;
    }

    public void setLop(Lop lop) {
        this.lop = lop;
    }

    public String getGhiChu() {
        return ghiChu;
    }

    public void setGhiChu(String ghiChu) {
        this.ghiChu = ghiChu;
    }

    public GiangVien getGiangVien() {
        return giangVien;
    }

    public void setGiangVien(GiangVien giangVien) {
        this.giangVien = giangVien;
    }
}

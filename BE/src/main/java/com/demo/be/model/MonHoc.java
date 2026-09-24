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
@Table(name = "mon_hoc")
public class MonHoc {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String maMonHoc;

    @Column(nullable = false, length = 150, columnDefinition = "NVARCHAR(150)")
    private String tenMonHoc;

    @Column(nullable = false)
    private Integer soTinChi;

    @Column(nullable = false)
    private Integer soTietLyThuyet;

    @Column(nullable = false)
    private Integer soTietThucHanh;

    @Column(length = 500, columnDefinition = "NVARCHAR(500)")
    private String moTa;

    @Column(length = 255, columnDefinition = "NVARCHAR(255)")
    private String monHocTienQuyet;

    @Column(nullable = false)
    private boolean active = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "khoa_id", nullable = false)
    private Khoa khoa;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMaMonHoc() {
        return maMonHoc;
    }

    public void setMaMonHoc(String maMonHoc) {
        this.maMonHoc = maMonHoc;
    }

    public String getTenMonHoc() {
        return tenMonHoc;
    }

    public void setTenMonHoc(String tenMonHoc) {
        this.tenMonHoc = tenMonHoc;
    }

    public Integer getSoTinChi() {
        return soTinChi;
    }

    public void setSoTinChi(Integer soTinChi) {
        this.soTinChi = soTinChi;
    }

    public Integer getSoTietLyThuyet() {
        return soTietLyThuyet;
    }

    public void setSoTietLyThuyet(Integer soTietLyThuyet) {
        this.soTietLyThuyet = soTietLyThuyet;
    }

    public Integer getSoTietThucHanh() {
        return soTietThucHanh;
    }

    public void setSoTietThucHanh(Integer soTietThucHanh) {
        this.soTietThucHanh = soTietThucHanh;
    }

    public String getMoTa() {
        return moTa;
    }

    public void setMoTa(String moTa) {
        this.moTa = moTa;
    }

    public String getMonHocTienQuyet() {
        return monHocTienQuyet;
    }

    public void setMonHocTienQuyet(String monHocTienQuyet) {
        this.monHocTienQuyet = monHocTienQuyet;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Khoa getKhoa() {
        return khoa;
    }

    public void setKhoa(Khoa khoa) {
        this.khoa = khoa;
    }
}

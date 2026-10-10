package com.demo.be.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;

@Entity
@Table(name = "bang_diem_ren_luyen", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"dot_ren_luyen_id", "sinh_vien_id"})
})
public class BangDiemRenLuyen {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dot_ren_luyen_id", nullable = false)
    private DotRenLuyen dotRenLuyen;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sinh_vien_id", nullable = false)
    private SinhVien sinhVien;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lop_id", nullable = false)
    private Lop lop;

    // === Điểm Sinh viên tự đánh giá (5 tiêu chí, tối đa 100 điểm) ===
    @Column
    private Integer diemSvMuc1 = 0; // Ý thức học tập (max 20)

    @Column
    private Integer diemSvMuc2 = 0; // Ý thức chấp hành nội quy (max 25)

    @Column
    private Integer diemSvMuc3 = 0; // Hoạt động phong trào, tình nguyện (max 20)

    @Column
    private Integer diemSvMuc4 = 0; // Phẩm chất công dân & quan hệ cộng đồng (max 25)

    @Column
    private Integer diemSvMuc5 = 0; // Công tác tập thể & thành tích đặc biệt (max 10)

    @Column
    private Integer tongDiemSv = 0; // Tổng điểm tự chấm (0 - 100)

    @Column(length = 500, columnDefinition = "NVARCHAR(500)")
    private String ghiChuSv;

    @Column
    private LocalDateTime thoiGianSvNop;

    // === Điểm Giảng viên / Cố vấn học tập đánh giá ===
    @Column
    private Integer diemGvMuc1; // max 20

    @Column
    private Integer diemGvMuc2; // max 25

    @Column
    private Integer diemGvMuc3; // max 20

    @Column
    private Integer diemGvMuc4; // max 25

    @Column
    private Integer diemGvMuc5; // max 10

    @Column
    private Integer tongDiemGv; // Tổng điểm GV chấm (0 - 100)

    @Column(length = 500, columnDefinition = "NVARCHAR(500)")
    private String nhanXetGv;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "giang_vien_id")
    private GiangVien giangVien;

    @Column
    private LocalDateTime thoiGianGvDanhGia;

    // === Điểm & Xếp loại do Admin chốt duyệt ===
    @Column
    private Integer diemTongKet;

    @Column(length = 50, columnDefinition = "NVARCHAR(50)")
    private String xepLoai; // Xuất sắc, Tốt, Khá, Trung bình, Yếu, Kém

    @Column(length = 500, columnDefinition = "NVARCHAR(500)")
    private String nhanXetAdmin;

    @Column
    private LocalDateTime thoiGianAdminDuyet;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 35)
    private TrangThaiDrl trangThai = TrangThaiDrl.CHUA_DANH_GIA;

    public BangDiemRenLuyen() {
    }

    public static String tinhXepLoai(int diem) {
        if (diem >= 90) return "Xuất sắc";
        if (diem >= 80) return "Tốt";
        if (diem >= 65) return "Khá";
        if (diem >= 50) return "Trung bình";
        if (diem >= 35) return "Yếu";
        return "Kém";
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public DotRenLuyen getDotRenLuyen() {
        return dotRenLuyen;
    }

    public void setDotRenLuyen(DotRenLuyen dotRenLuyen) {
        this.dotRenLuyen = dotRenLuyen;
    }

    public SinhVien getSinhVien() {
        return sinhVien;
    }

    public void setSinhVien(SinhVien sinhVien) {
        this.sinhVien = sinhVien;
    }

    public Lop getLop() {
        return lop;
    }

    public void setLop(Lop lop) {
        this.lop = lop;
    }

    public Integer getDiemSvMuc1() {
        return diemSvMuc1;
    }

    public void setDiemSvMuc1(Integer diemSvMuc1) {
        this.diemSvMuc1 = diemSvMuc1;
    }

    public Integer getDiemSvMuc2() {
        return diemSvMuc2;
    }

    public void setDiemSvMuc2(Integer diemSvMuc2) {
        this.diemSvMuc2 = diemSvMuc2;
    }

    public Integer getDiemSvMuc3() {
        return diemSvMuc3;
    }

    public void setDiemSvMuc3(Integer diemSvMuc3) {
        this.diemSvMuc3 = diemSvMuc3;
    }

    public Integer getDiemSvMuc4() {
        return diemSvMuc4;
    }

    public void setDiemSvMuc4(Integer diemSvMuc4) {
        this.diemSvMuc4 = diemSvMuc4;
    }

    public Integer getDiemSvMuc5() {
        return diemSvMuc5;
    }

    public void setDiemSvMuc5(Integer diemSvMuc5) {
        this.diemSvMuc5 = diemSvMuc5;
    }

    public Integer getTongDiemSv() {
        return tongDiemSv;
    }

    public void setTongDiemSv(Integer tongDiemSv) {
        this.tongDiemSv = tongDiemSv;
    }

    public String getGhiChuSv() {
        return ghiChuSv;
    }

    public void setGhiChuSv(String ghiChuSv) {
        this.ghiChuSv = ghiChuSv;
    }

    public LocalDateTime getThoiGianSvNop() {
        return thoiGianSvNop;
    }

    public void setThoiGianSvNop(LocalDateTime thoiGianSvNop) {
        this.thoiGianSvNop = thoiGianSvNop;
    }

    public Integer getDiemGvMuc1() {
        return diemGvMuc1;
    }

    public void setDiemGvMuc1(Integer diemGvMuc1) {
        this.diemGvMuc1 = diemGvMuc1;
    }

    public Integer getDiemGvMuc2() {
        return diemGvMuc2;
    }

    public void setDiemGvMuc2(Integer diemGvMuc2) {
        this.diemGvMuc2 = diemGvMuc2;
    }

    public Integer getDiemGvMuc3() {
        return diemGvMuc3;
    }

    public void setDiemGvMuc3(Integer diemGvMuc3) {
        this.diemGvMuc3 = diemGvMuc3;
    }

    public Integer getDiemGvMuc4() {
        return diemGvMuc4;
    }

    public void setDiemGvMuc4(Integer diemGvMuc4) {
        this.diemGvMuc4 = diemGvMuc4;
    }

    public Integer getDiemGvMuc5() {
        return diemGvMuc5;
    }

    public void setDiemGvMuc5(Integer diemGvMuc5) {
        this.diemGvMuc5 = diemGvMuc5;
    }

    public Integer getTongDiemGv() {
        return tongDiemGv;
    }

    public void setTongDiemGv(Integer tongDiemGv) {
        this.tongDiemGv = tongDiemGv;
    }

    public String getNhanXetGv() {
        return nhanXetGv;
    }

    public void setNhanXetGv(String nhanXetGv) {
        this.nhanXetGv = nhanXetGv;
    }

    public GiangVien getGiangVien() {
        return giangVien;
    }

    public void setGiangVien(GiangVien giangVien) {
        this.giangVien = giangVien;
    }

    public LocalDateTime getThoiGianGvDanhGia() {
        return thoiGianGvDanhGia;
    }

    public void setThoiGianGvDanhGia(LocalDateTime thoiGianGvDanhGia) {
        this.thoiGianGvDanhGia = thoiGianGvDanhGia;
    }

    public Integer getDiemTongKet() {
        return diemTongKet;
    }

    public void setDiemTongKet(Integer diemTongKet) {
        this.diemTongKet = diemTongKet;
    }

    public String getXepLoai() {
        return xepLoai;
    }

    public void setXepLoai(String xepLoai) {
        this.xepLoai = xepLoai;
    }

    public String getNhanXetAdmin() {
        return nhanXetAdmin;
    }

    public void setNhanXetAdmin(String nhanXetAdmin) {
        this.nhanXetAdmin = nhanXetAdmin;
    }

    public LocalDateTime getThoiGianAdminDuyet() {
        return thoiGianAdminDuyet;
    }

    public void setThoiGianAdminDuyet(LocalDateTime thoiGianAdminDuyet) {
        this.thoiGianAdminDuyet = thoiGianAdminDuyet;
    }

    public TrangThaiDrl getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(TrangThaiDrl trangThai) {
        this.trangThai = trangThai;
    }
}

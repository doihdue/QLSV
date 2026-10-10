package com.demo.be.dto.report;

public class GpaReportItemDto {
    private Integer stt;
    private String mssv;
    private String hoTen;
    private String tenLop;
    private String tenKhoa;
    private Integer soMonHoc;
    private Integer tinChiTichLuy;
    private Double diemTrungBinh;
    private String xepLoaiHocLuc;

    public GpaReportItemDto() {
    }

    public GpaReportItemDto(Integer stt, String mssv, String hoTen, String tenLop, String tenKhoa,
                            Integer soMonHoc, Integer tinChiTichLuy, Double diemTrungBinh, String xepLoaiHocLuc) {
        this.stt = stt;
        this.mssv = mssv;
        this.hoTen = hoTen;
        this.tenLop = tenLop;
        this.tenKhoa = tenKhoa;
        this.soMonHoc = soMonHoc;
        this.tinChiTichLuy = tinChiTichLuy;
        this.diemTrungBinh = diemTrungBinh;
        this.xepLoaiHocLuc = xepLoaiHocLuc;
    }

    public Integer getStt() {
        return stt;
    }

    public void setStt(Integer stt) {
        this.stt = stt;
    }

    public String getMssv() {
        return mssv;
    }

    public void setMssv(String mssv) {
        this.mssv = mssv;
    }

    public String getHoTen() {
        return hoTen;
    }

    public void setHoTen(String hoTen) {
        this.hoTen = hoTen;
    }

    public String getTenLop() {
        return tenLop;
    }

    public void setTenLop(String tenLop) {
        this.tenLop = tenLop;
    }

    public String getTenKhoa() {
        return tenKhoa;
    }

    public void setTenKhoa(String tenKhoa) {
        this.tenKhoa = tenKhoa;
    }

    public Integer getSoMonHoc() {
        return soMonHoc;
    }

    public void setSoMonHoc(Integer soMonHoc) {
        this.soMonHoc = soMonHoc;
    }

    public Integer getTinChiTichLuy() {
        return tinChiTichLuy;
    }

    public void setTinChiTichLuy(Integer tinChiTichLuy) {
        this.tinChiTichLuy = tinChiTichLuy;
    }

    public Double getDiemTrungBinh() {
        return diemTrungBinh;
    }

    public void setDiemTrungBinh(Double diemTrungBinh) {
        this.diemTrungBinh = diemTrungBinh;
    }

    public String getXepLoaiHocLuc() {
        return xepLoaiHocLuc;
    }

    public void setXepLoaiHocLuc(String xepLoaiHocLuc) {
        this.xepLoaiHocLuc = xepLoaiHocLuc;
    }
}

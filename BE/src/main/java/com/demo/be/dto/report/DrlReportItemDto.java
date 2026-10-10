package com.demo.be.dto.report;

public class DrlReportItemDto {
    private Integer stt;
    private String mssv;
    private String hoTen;
    private Integer diemSv;
    private Integer diemGv;
    private Integer diemTongKet;
    private String xepLoai;
    private String trangThai;

    public DrlReportItemDto() {
    }

    public DrlReportItemDto(Integer stt, String mssv, String hoTen, Integer diemSv, Integer diemGv, Integer diemTongKet, String xepLoai, String trangThai) {
        this.stt = stt;
        this.mssv = mssv;
        this.hoTen = hoTen;
        this.diemSv = diemSv;
        this.diemGv = diemGv;
        this.diemTongKet = diemTongKet;
        this.xepLoai = xepLoai;
        this.trangThai = trangThai;
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

    public Integer getDiemSv() {
        return diemSv;
    }

    public void setDiemSv(Integer diemSv) {
        this.diemSv = diemSv;
    }

    public Integer getDiemGv() {
        return diemGv;
    }

    public void setDiemGv(Integer diemGv) {
        this.diemGv = diemGv;
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

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }
}

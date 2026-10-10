package com.demo.be.model;

public enum TrangThaiDrl {
    CHUA_DANH_GIA("Chưa đánh giá"),
    LUU_NHAP_SINH_VIEN("Sinh viên lưu nháp"),
    CHO_GIANG_VIEN_DUYET("Chờ giảng viên đánh giá"),
    LUU_NHAP_GIANG_VIEN("Giảng viên đã chấm nháp"),
    CHO_ADMIN_DUYET("Chờ Admin phê duyệt"),
    DA_DUYET("Đã phê duyệt"),
    TU_CHOI("Yêu cầu đánh giá lại");

    private final String moTa;

    TrangThaiDrl(String moTa) {
        this.moTa = moTa;
    }

    public String getMoTa() {
        return moTa;
    }
}

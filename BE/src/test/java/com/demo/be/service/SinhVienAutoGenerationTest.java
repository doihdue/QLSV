package com.demo.be.service;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SinhVienAutoGenerationTest {

    @Test
    void testGenerateEmailFromNameAndMssv_UserExample() {
        String hoTen = "Đỗ Minh Duệ";
        String mssv = "2022CNTT001";
        String email = SinhVienService.generateEmailFromNameAndMssv(hoTen, mssv);
        assertEquals("duedm.2022CNTT001@stu.edu.vn", email);
    }

    @Test
    void testGenerateEmailFromNameAndMssv_OtherNames() {
        // Nguyễn Văn An -> annv
        assertEquals("annv.2023CNTT015@stu.edu.vn", SinhVienService.generateEmailFromNameAndMssv("Nguyễn Văn An", "2023CNTT015"));
        
        // Trần Thị Bích Ngọc -> ngocttb
        assertEquals("ngocttb.2024DTVT002@stu.edu.vn", SinhVienService.generateEmailFromNameAndMssv("Trần Thị Bích Ngọc", "2024DTVT002"));
        
        // Single word name -> an
        assertEquals("an.2022CNTT001@stu.edu.vn", SinhVienService.generateEmailFromNameAndMssv("An", "2022CNTT001"));
        
        // Name with multiple spaces
        assertEquals("duedm.2022CNTT001@stu.edu.vn", SinhVienService.generateEmailFromNameAndMssv("   Đỗ   Minh   Duệ   ", "2022CNTT001"));
    }

    @Test
    void testGenerateEmailForLecturer() {
        String hoTen = "Đỗ Minh Duệ";
        String maGv = "GVCNTT001";
        String email = SinhVienService.generateEmailFromNameAndMssv(hoTen, maGv);
        assertEquals("duedm.GVCNTT001@stu.edu.vn", email);
    }
}

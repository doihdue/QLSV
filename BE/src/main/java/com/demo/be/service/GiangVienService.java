package com.demo.be.service;

import com.demo.be.dto.giangvien.GiangVienRequest;
import com.demo.be.dto.giangvien.GiangVienResponse;
import java.util.List;

public interface GiangVienService {
    List<GiangVienResponse> findAll();
    GiangVienResponse findById(Long id);
    GiangVienResponse create(GiangVienRequest request);
    GiangVienResponse update(Long id, GiangVienRequest request);
    void delete(Long id);
    String generateNextMaGiangVien(Long khoaId);
}

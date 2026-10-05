package com.demo.be.service;

import com.demo.be.dto.monhoc.MonHocRequest;
import com.demo.be.dto.monhoc.MonHocResponse;
import java.util.List;

public interface MonHocService {
    List<MonHocResponse> findAll();
    MonHocResponse findById(Long id);
    List<MonHocResponse> findActive();
    MonHocResponse create(MonHocRequest request);
    MonHocResponse update(Long id, MonHocRequest request);
    void delete(Long id);
}

package com.demo.be.service;

import com.demo.be.dto.thongke.SinhVienGpaResponse;
import java.util.List;

public interface ThongKeService {
    List<SinhVienGpaResponse> getThongKeGpa(Long lopId);
}

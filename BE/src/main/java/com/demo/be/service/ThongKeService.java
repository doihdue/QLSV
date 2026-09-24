package com.demo.be.service;

import com.demo.be.dto.thongke.SinhVienGpaResponse;
import com.demo.be.repository.SinhVienRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ThongKeService {

    private final SinhVienRepository sinhVienRepository;

    public ThongKeService(SinhVienRepository sinhVienRepository) {
        this.sinhVienRepository = sinhVienRepository;
    }

    /**
     * Lấy bảng thống kê kết quả học tập và xếp loại học lực của sinh viên
     * Gọi qua Native SQL Query định nghĩa trong SinhVienRepository
     *
     * @param lopId (Tùy chọn) Lọc theo lớp học, nếu null sẽ lấy toàn bộ sinh viên
     * @return Danh sách SinhVienGpaResponse được sắp xếp theo GPA giảm dần
     */
    @Transactional(readOnly = true)
    public List<SinhVienGpaResponse> getThongKeGpa(Long lopId) {
        return sinhVienRepository.findThongKeGpaNative(lopId).stream()
                .map(SinhVienGpaResponse::fromProjection)
                .toList();
    }
}

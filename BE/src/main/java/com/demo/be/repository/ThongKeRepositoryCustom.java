package com.demo.be.repository;

import com.demo.be.dto.thongke.SinhVienGpaResponse;
import java.util.List;

public interface ThongKeRepositoryCustom {
    /**
     * Thống kê kết quả học tập & xếp hạng GPA sinh viên bằng Native SQL và @SqlResultSetMapping
     *
     * @param lopId ID lớp học cần lọc (null nếu lấy tất cả)
     * @return Danh sách sinh viên kèm GPA và xếp loại học lực
     */
    List<SinhVienGpaResponse> findThongKeGpaNative(Long lopId);
}

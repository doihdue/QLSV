package com.demo.be.dto.diem;

import java.util.List;

public record LecturerGradeBatchRequest(
        Long monHocMoId,
        boolean submitForApproval,
        List<LecturerGradeSaveRequest> grades
) {
}

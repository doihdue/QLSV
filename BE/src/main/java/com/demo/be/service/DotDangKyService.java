package com.demo.be.service;

import com.demo.be.dto.dangky.DotDangKyRequest;
import com.demo.be.dto.dangky.DotDangKyResponse;
import com.demo.be.model.DotDangKy;

public interface DotDangKyService {
    DotDangKy getCurrent();
    void scheduledAutoSync();
    DotDangKyResponse getCurrentResponse();
    DotDangKyResponse updateCurrent(DotDangKyRequest request);
    DotDangKyResponse toggleOpen(boolean open);
}

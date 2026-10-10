package com.demo.be.service;

import com.demo.be.dto.renluyen.DotRenLuyenRequest;
import com.demo.be.dto.renluyen.DotRenLuyenResponse;
import java.util.List;

public interface DotRenLuyenService {
    List<DotRenLuyenResponse> findAll();
    DotRenLuyenResponse getCurrent();
    DotRenLuyenResponse getById(Long id);
    DotRenLuyenResponse create(DotRenLuyenRequest request);
    DotRenLuyenResponse update(Long id, DotRenLuyenRequest request);
    DotRenLuyenResponse toggle(Long id, boolean open);
}

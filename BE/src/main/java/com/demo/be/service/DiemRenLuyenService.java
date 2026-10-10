package com.demo.be.service;

import com.demo.be.dto.renluyen.AdminPheDuyetDrlRequest;
import com.demo.be.dto.renluyen.BangDiemRenLuyenResponse;
import com.demo.be.dto.renluyen.LecturerDanhGiaDrlRequest;
import com.demo.be.dto.renluyen.StudentDanhGiaDrlRequest;
import com.demo.be.dto.renluyen.ThongKeDrlResponse;

import java.util.List;

public interface DiemRenLuyenService {

    // Sinh viên
    BangDiemRenLuyenResponse getMyScore(String username, Long dotId);
    BangDiemRenLuyenResponse studentSubmit(String username, StudentDanhGiaDrlRequest request);

    // Giảng viên
    List<BangDiemRenLuyenResponse> getScoresForClass(Long lopId, Long dotId);
    BangDiemRenLuyenResponse getScoreById(Long id);
    BangDiemRenLuyenResponse lecturerEvaluate(String username, LecturerDanhGiaDrlRequest request);
    int lecturerSubmitWholeClassToAdmin(String username, Long lopId, Long dotId);

    // Admin
    List<BangDiemRenLuyenResponse> getScoresForAdmin(Long dotId, Long lopId, String trangThai);
    ThongKeDrlResponse getThongKeDrl(Long dotId, Long lopId);
    BangDiemRenLuyenResponse adminApproveOrReject(AdminPheDuyetDrlRequest request);
    int adminApproveWholeClass(Long dotId, Long lopId);
}

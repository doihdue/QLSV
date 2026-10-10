package com.demo.be.service.impl;

import com.demo.be.dto.renluyen.AdminPheDuyetDrlRequest;
import com.demo.be.dto.renluyen.BangDiemRenLuyenResponse;
import com.demo.be.dto.renluyen.LecturerDanhGiaDrlRequest;
import com.demo.be.dto.renluyen.StudentDanhGiaDrlRequest;
import com.demo.be.dto.renluyen.ThongKeDrlResponse;
import com.demo.be.exception.ResourceNotFoundException;
import com.demo.be.model.BangDiemRenLuyen;
import com.demo.be.model.DotRenLuyen;
import com.demo.be.model.GiangVien;
import com.demo.be.model.Lop;
import com.demo.be.model.SinhVien;
import com.demo.be.model.TrangThaiDrl;
import com.demo.be.repository.BangDiemRenLuyenRepository;
import com.demo.be.repository.DotRenLuyenRepository;
import com.demo.be.repository.GiangVienRepository;
import com.demo.be.repository.LopRepository;
import com.demo.be.repository.SinhVienRepository;
import com.demo.be.service.DiemRenLuyenService;
import com.demo.be.service.ThongBaoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public class DiemRenLuyenServiceImpl implements DiemRenLuyenService {

    private final BangDiemRenLuyenRepository bangDiemRenLuyenRepository;
    private final DotRenLuyenRepository dotRenLuyenRepository;
    private final SinhVienRepository sinhVienRepository;
    private final GiangVienRepository giangVienRepository;
    private final LopRepository lopRepository;
    private final ThongBaoService thongBaoService;

    public DiemRenLuyenServiceImpl(
            BangDiemRenLuyenRepository bangDiemRenLuyenRepository,
            DotRenLuyenRepository dotRenLuyenRepository,
            SinhVienRepository sinhVienRepository,
            GiangVienRepository giangVienRepository,
            LopRepository lopRepository,
            ThongBaoService thongBaoService
    ) {
        this.bangDiemRenLuyenRepository = bangDiemRenLuyenRepository;
        this.dotRenLuyenRepository = dotRenLuyenRepository;
        this.sinhVienRepository = sinhVienRepository;
        this.giangVienRepository = giangVienRepository;
        this.lopRepository = lopRepository;
        this.thongBaoService = thongBaoService;
    }

    private DotRenLuyen resolveDotRenLuyen(Long dotId) {
        if (dotId != null) {
            return dotRenLuyenRepository.findById(dotId)
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đợt rèn luyện với ID " + dotId));
        }
        return dotRenLuyenRepository.findFirstByDangMoTrueOrderByIdDesc()
                .orElseThrow(() -> new ResourceNotFoundException("Hiện tại không có đợt rèn luyện nào đang mở"));
    }

    private SinhVien resolveSinhVien(String username) {
        return sinhVienRepository.findByMssv(username)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thông tin sinh viên với tài khoản: " + username));
    }

    private GiangVien resolveGiangVien(String username) {
        return giangVienRepository.findByMaGiangVien(username)
                .orElse(null);
    }

    @Override
    @Transactional
    public BangDiemRenLuyenResponse getMyScore(String username, Long dotId) {
        SinhVien sv = resolveSinhVien(username);
        DotRenLuyen dot = resolveDotRenLuyen(dotId);

        BangDiemRenLuyen sheet = bangDiemRenLuyenRepository
                .findByDotRenLuyen_IdAndSinhVien_Id(dot.getId(), sv.getId())
                .orElseGet(() -> {
                    BangDiemRenLuyen newSheet = new BangDiemRenLuyen();
                    newSheet.setDotRenLuyen(dot);
                    newSheet.setSinhVien(sv);
                    newSheet.setLop(sv.getLop());
                    newSheet.setTrangThai(TrangThaiDrl.CHUA_DANH_GIA);
                    return bangDiemRenLuyenRepository.save(newSheet);
                });

        return toResponse(sheet);
    }

    @Override
    @Transactional
    public BangDiemRenLuyenResponse studentSubmit(String username, StudentDanhGiaDrlRequest request) {
        SinhVien sv = resolveSinhVien(username);
        DotRenLuyen dot = resolveDotRenLuyen(request.dotId());

        if (!dot.isDangMo()) {
            throw new IllegalStateException("Đợt đánh giá rèn luyện hiện đã đóng. Bạn không thể nộp hoặc chỉnh sửa phiếu.");
        }

        LocalDate today = LocalDate.now();
        if (dot.getNgayBatDau() != null && today.isBefore(dot.getNgayBatDau())) {
            throw new IllegalStateException("Chưa đến thời gian mở đợt đánh giá rèn luyện (Bắt đầu từ: " + dot.getNgayBatDau() + ").");
        }
        if (dot.getNgayKetThuc() != null && today.isAfter(dot.getNgayKetThuc())) {
            throw new IllegalStateException("Đã quá hạn chót đánh giá rèn luyện (Hạn chót: " + dot.getNgayKetThuc() + ").");
        }

        BangDiemRenLuyen sheet = bangDiemRenLuyenRepository
                .findByDotRenLuyen_IdAndSinhVien_Id(dot.getId(), sv.getId())
                .orElseGet(() -> {
                    BangDiemRenLuyen newSheet = new BangDiemRenLuyen();
                    newSheet.setDotRenLuyen(dot);
                    newSheet.setSinhVien(sv);
                    newSheet.setLop(sv.getLop());
                    return newSheet;
                });

        if (sheet.getTrangThai() == TrangThaiDrl.CHO_ADMIN_DUYET || sheet.getTrangThai() == TrangThaiDrl.DA_DUYET) {
            throw new IllegalStateException("Phiếu điểm rèn luyện đã được gửi lên ban giám hiệu/đã duyệt, không thể thay đổi.");
        }

        sheet.setDiemSvMuc1(request.diemSvMuc1());
        sheet.setDiemSvMuc2(request.diemSvMuc2());
        sheet.setDiemSvMuc3(request.diemSvMuc3());
        sheet.setDiemSvMuc4(request.diemSvMuc4());
        sheet.setDiemSvMuc5(request.diemSvMuc5());

        int totalSv = request.diemSvMuc1() + request.diemSvMuc2() + request.diemSvMuc3() + request.diemSvMuc4() + request.diemSvMuc5();
        sheet.setTongDiemSv(totalSv);
        sheet.setGhiChuSv(request.ghiChuSv());

        boolean isSubmit = "SUBMIT".equalsIgnoreCase(request.action());
        if (isSubmit) {
            sheet.setTrangThai(TrangThaiDrl.CHO_GIANG_VIEN_DUYET);
            sheet.setThoiGianSvNop(LocalDateTime.now());
            thongBaoService.createNotification(
                    sv.getMssv(),
                    "Đã nộp phiếu điểm rèn luyện",
                    "Bạn đã nộp thành công phiếu tự đánh giá rèn luyện đợt '" + dot.getTenDot() + "' với " + totalSv + " điểm. Đang chờ giảng viên đánh giá.",
                    "REN_LUYEN",
                    "/student/portal?tab=ren-luyen"
            );
        } else {
            sheet.setTrangThai(TrangThaiDrl.LUU_NHAP_SINH_VIEN);
        }

        BangDiemRenLuyen saved = bangDiemRenLuyenRepository.save(sheet);
        return toResponse(saved);
    }

    @Override
    @Transactional
    public List<BangDiemRenLuyenResponse> getScoresForClass(Long lopId, Long dotId) {
        DotRenLuyen dot = resolveDotRenLuyen(dotId);
        Lop lop = lopRepository.findById(lopId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy lớp với ID " + lopId));

        List<SinhVien> sinhViens = sinhVienRepository.findByLop_Id(lopId);
        List<BangDiemRenLuyen> existingSheets = bangDiemRenLuyenRepository.findByDotRenLuyen_IdAndLop_Id(dot.getId(), lopId);

        List<BangDiemRenLuyenResponse> results = new ArrayList<>();
        for (SinhVien sv : sinhViens) {
            Optional<BangDiemRenLuyen> match = existingSheets.stream()
                    .filter(s -> s.getSinhVien().getId().equals(sv.getId()))
                    .findFirst();

            if (match.isPresent()) {
                results.add(toResponse(match.get()));
            } else {
                // Tự động khởi tạo phiếu CHUA_DANH_GIA để GV thấy đầy đủ danh sách sinh viên
                BangDiemRenLuyen newSheet = new BangDiemRenLuyen();
                newSheet.setDotRenLuyen(dot);
                newSheet.setSinhVien(sv);
                newSheet.setLop(lop);
                newSheet.setTrangThai(TrangThaiDrl.CHUA_DANH_GIA);
                BangDiemRenLuyen saved = bangDiemRenLuyenRepository.save(newSheet);
                results.add(toResponse(saved));
            }
        }

        results.sort(Comparator.comparing(BangDiemRenLuyenResponse::mssv));
        return results;
    }

    @Override
    public BangDiemRenLuyenResponse getScoreById(Long id) {
        BangDiemRenLuyen sheet = bangDiemRenLuyenRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phiếu điểm rèn luyện với ID " + id));
        return toResponse(sheet);
    }

    @Override
    @Transactional
    public BangDiemRenLuyenResponse lecturerEvaluate(String username, LecturerDanhGiaDrlRequest request) {
        BangDiemRenLuyen sheet = bangDiemRenLuyenRepository.findById(request.bangDiemId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phiếu điểm rèn luyện với ID " + request.bangDiemId()));

        GiangVien gv = resolveGiangVien(username);

        sheet.setDiemGvMuc1(request.diemGvMuc1());
        sheet.setDiemGvMuc2(request.diemGvMuc2());
        sheet.setDiemGvMuc3(request.diemGvMuc3());
        sheet.setDiemGvMuc4(request.diemGvMuc4());
        sheet.setDiemGvMuc5(request.diemGvMuc5());

        int totalGv = request.diemGvMuc1() + request.diemGvMuc2() + request.diemGvMuc3() + request.diemGvMuc4() + request.diemGvMuc5();
        sheet.setTongDiemGv(totalGv);
        sheet.setNhanXetGv(request.nhanXetGv());
        sheet.setGiangVien(gv);
        sheet.setThoiGianGvDanhGia(LocalDateTime.now());

        boolean isSubmitToAdmin = "SUBMIT_ADMIN".equalsIgnoreCase(request.action());
        if (isSubmitToAdmin) {
            sheet.setTrangThai(TrangThaiDrl.CHO_ADMIN_DUYET);
            // Gửi thông báo cho sinh viên
            thongBaoService.createNotification(
                    sheet.getSinhVien().getMssv(),
                    "Giảng viên đã đánh giá điểm rèn luyện",
                    "Giảng viên đã đánh giá " + totalGv + " điểm và gửi lên Ban Đào tạo/Admin phê duyệt.",
                    "REN_LUYEN",
                    "/student/portal?tab=ren-luyen"
            );
        } else {
            sheet.setTrangThai(TrangThaiDrl.LUU_NHAP_GIANG_VIEN);
        }

        BangDiemRenLuyen saved = bangDiemRenLuyenRepository.save(sheet);
        return toResponse(saved);
    }

    @Override
    @Transactional
    public int lecturerSubmitWholeClassToAdmin(String username, Long lopId, Long dotId) {
        DotRenLuyen dot = resolveDotRenLuyen(dotId);
        List<BangDiemRenLuyen> sheets = bangDiemRenLuyenRepository.findByDotRenLuyen_IdAndLop_Id(dot.getId(), lopId);
        GiangVien gv = resolveGiangVien(username);

        int count = 0;
        for (BangDiemRenLuyen sheet : sheets) {
            // Cho phép gửi lên Admin nếu GV đã chấm (tongDiemGv != null) hoặc SV đã nộp
            if (sheet.getTrangThai() == TrangThaiDrl.CHO_GIANG_VIEN_DUYET || sheet.getTrangThai() == TrangThaiDrl.LUU_NHAP_GIANG_VIEN) {
                if (sheet.getTongDiemGv() == null) {
                    // Nếu GV chưa chấm chi tiết, kế thừa tạm điểm SV tự chấm
                    sheet.setDiemGvMuc1(sheet.getDiemSvMuc1());
                    sheet.setDiemGvMuc2(sheet.getDiemSvMuc2());
                    sheet.setDiemGvMuc3(sheet.getDiemSvMuc3());
                    sheet.setDiemGvMuc4(sheet.getDiemSvMuc4());
                    sheet.setDiemGvMuc5(sheet.getDiemSvMuc5());
                    sheet.setTongDiemGv(sheet.getTongDiemSv());
                }
                sheet.setGiangVien(gv);
                sheet.setThoiGianGvDanhGia(LocalDateTime.now());
                sheet.setTrangThai(TrangThaiDrl.CHO_ADMIN_DUYET);
                bangDiemRenLuyenRepository.save(sheet);
                count++;

                thongBaoService.createNotification(
                        sheet.getSinhVien().getMssv(),
                        "Điểm rèn luyện đã được gửi Admin duyệt",
                        "Phiếu điểm rèn luyện của lớp đã được giảng viên chuyển lên Admin để xét duyệt chính thức.",
                        "REN_LUYEN",
                        "/student/portal?tab=ren-luyen"
                );
            }
        }
        return count;
    }

    @Override
    public List<BangDiemRenLuyenResponse> getScoresForAdmin(Long dotId, Long lopId, String trangThai) {
        DotRenLuyen dot = resolveDotRenLuyen(dotId);
        TrangThaiDrl ttEnum = null;
        if (trangThai != null && !trangThai.isBlank() && !"ALL".equalsIgnoreCase(trangThai)) {
            try {
                ttEnum = TrangThaiDrl.valueOf(trangThai.trim());
            } catch (Exception ignored) {
            }
        }

        List<BangDiemRenLuyen> sheets;
        if (lopId != null && ttEnum != null) {
            sheets = bangDiemRenLuyenRepository.findByDotRenLuyen_IdAndLop_IdAndTrangThai(dot.getId(), lopId, ttEnum);
        } else if (lopId != null) {
            sheets = bangDiemRenLuyenRepository.findByDotRenLuyen_IdAndLop_Id(dot.getId(), lopId);
        } else if (ttEnum != null) {
            sheets = bangDiemRenLuyenRepository.findByDotRenLuyen_IdAndTrangThai(dot.getId(), ttEnum);
        } else {
            sheets = bangDiemRenLuyenRepository.findByDotRenLuyen_Id(dot.getId());
        }

        return sheets.stream()
                .map(this::toResponse)
                .sorted(Comparator.comparing(BangDiemRenLuyenResponse::mssv))
                .toList();
    }

    @Override
    public ThongKeDrlResponse getThongKeDrl(Long dotId, Long lopId) {
        DotRenLuyen dot = resolveDotRenLuyen(dotId);
        List<BangDiemRenLuyen> sheets = (lopId != null)
                ? bangDiemRenLuyenRepository.findByDotRenLuyen_IdAndLop_Id(dot.getId(), lopId)
                : bangDiemRenLuyenRepository.findByDotRenLuyen_Id(dot.getId());

        long tong = sheets.size();
        long chuaDanhGia = sheets.stream().filter(s -> s.getTrangThai() == TrangThaiDrl.CHUA_DANH_GIA).count();
        long choGv = sheets.stream().filter(s -> s.getTrangThai() == TrangThaiDrl.CHO_GIANG_VIEN_DUYET || s.getTrangThai() == TrangThaiDrl.LUU_NHAP_GIANG_VIEN).count();
        long choAdmin = sheets.stream().filter(s -> s.getTrangThai() == TrangThaiDrl.CHO_ADMIN_DUYET).count();
        long daDuyet = sheets.stream().filter(s -> s.getTrangThai() == TrangThaiDrl.DA_DUYET).count();
        long tuChoi = sheets.stream().filter(s -> s.getTrangThai() == TrangThaiDrl.TU_CHOI).count();

        long xs = sheets.stream().filter(s -> s.getTrangThai() == TrangThaiDrl.DA_DUYET && "Xuất sắc".equalsIgnoreCase(s.getXepLoai())).count();
        long tot = sheets.stream().filter(s -> s.getTrangThai() == TrangThaiDrl.DA_DUYET && "Tốt".equalsIgnoreCase(s.getXepLoai())).count();
        long kha = sheets.stream().filter(s -> s.getTrangThai() == TrangThaiDrl.DA_DUYET && "Khá".equalsIgnoreCase(s.getXepLoai())).count();
        long tb = sheets.stream().filter(s -> s.getTrangThai() == TrangThaiDrl.DA_DUYET && "Trung bình".equalsIgnoreCase(s.getXepLoai())).count();
        long yeu = sheets.stream().filter(s -> s.getTrangThai() == TrangThaiDrl.DA_DUYET && "Yếu".equalsIgnoreCase(s.getXepLoai())).count();
        long kem = sheets.stream().filter(s -> s.getTrangThai() == TrangThaiDrl.DA_DUYET && "Kém".equalsIgnoreCase(s.getXepLoai())).count();

        double avg = sheets.stream()
                .filter(s -> s.getTrangThai() == TrangThaiDrl.DA_DUYET && s.getDiemTongKet() != null)
                .mapToInt(BangDiemRenLuyen::getDiemTongKet)
                .average()
                .orElse(0.0);

        return new ThongKeDrlResponse(
                tong,
                chuaDanhGia,
                choGv,
                choAdmin,
                daDuyet,
                tuChoi,
                xs,
                tot,
                kha,
                tb,
                yeu,
                kem,
                Math.round(avg * 100.0) / 100.0
        );
    }

    @Override
    @Transactional
    public BangDiemRenLuyenResponse adminApproveOrReject(AdminPheDuyetDrlRequest request) {
        BangDiemRenLuyen sheet = bangDiemRenLuyenRepository.findById(request.bangDiemId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phiếu điểm rèn luyện với ID " + request.bangDiemId()));

        boolean isApprove = "APPROVE".equalsIgnoreCase(request.action());
        if (isApprove) {
            sheet.setTrangThai(TrangThaiDrl.DA_DUYET);
            int finalScore;
            if (request.diemDieuChinh() != null) {
                finalScore = request.diemDieuChinh();
            } else if (sheet.getTongDiemGv() != null) {
                finalScore = sheet.getTongDiemGv();
            } else if (sheet.getTongDiemSv() != null) {
                finalScore = sheet.getTongDiemSv();
            } else {
                finalScore = 0;
            }

            sheet.setDiemTongKet(finalScore);
            sheet.setXepLoai(BangDiemRenLuyen.tinhXepLoai(finalScore));
            sheet.setNhanXetAdmin(request.nhanXetAdmin());
            sheet.setThoiGianAdminDuyet(LocalDateTime.now());

            thongBaoService.createNotification(
                    sheet.getSinhVien().getMssv(),
                    "Điểm rèn luyện đã được phê duyệt",
                    "Điểm rèn luyện chính thức của bạn là: " + finalScore + " điểm (Xếp loại: " + sheet.getXepLoai() + ").",
                    "REN_LUYEN",
                    "/student/portal?tab=ren-luyen"
            );
        } else {
            sheet.setTrangThai(TrangThaiDrl.TU_CHOI);
            sheet.setNhanXetAdmin(request.nhanXetAdmin());
            sheet.setThoiGianAdminDuyet(LocalDateTime.now());

            thongBaoService.createNotification(
                    sheet.getSinhVien().getMssv(),
                    "Phiếu rèn luyện bị từ chối / yêu cầu đánh giá lại",
                    "Lý do từ chối: " + (request.nhanXetAdmin() != null ? request.nhanXetAdmin() : "Vui lòng kiểm tra lại thông tin."),
                    "REN_LUYEN",
                    "/student/portal?tab=ren-luyen"
            );
        }

        BangDiemRenLuyen saved = bangDiemRenLuyenRepository.save(sheet);
        return toResponse(saved);
    }

    @Override
    @Transactional
    public int adminApproveWholeClass(Long dotId, Long lopId) {
        DotRenLuyen dot = resolveDotRenLuyen(dotId);
        List<BangDiemRenLuyen> sheets = bangDiemRenLuyenRepository.findByDotRenLuyen_IdAndLop_Id(dot.getId(), lopId);

        int count = 0;
        for (BangDiemRenLuyen sheet : sheets) {
            if (sheet.getTrangThai() == TrangThaiDrl.CHO_ADMIN_DUYET || sheet.getTrangThai() == TrangThaiDrl.CHO_GIANG_VIEN_DUYET) {
                sheet.setTrangThai(TrangThaiDrl.DA_DUYET);
                int finalScore = sheet.getTongDiemGv() != null ? sheet.getTongDiemGv() : (sheet.getTongDiemSv() != null ? sheet.getTongDiemSv() : 0);
                sheet.setDiemTongKet(finalScore);
                sheet.setXepLoai(BangDiemRenLuyen.tinhXepLoai(finalScore));
                sheet.setThoiGianAdminDuyet(LocalDateTime.now());
                bangDiemRenLuyenRepository.save(sheet);
                count++;

                thongBaoService.createNotification(
                        sheet.getSinhVien().getMssv(),
                        "Điểm rèn luyện đã được phê duyệt",
                        "Điểm rèn luyện chính thức của bạn là: " + finalScore + " điểm (Xếp loại: " + sheet.getXepLoai() + ").",
                        "REN_LUYEN",
                        "/student/portal?tab=ren-luyen"
                );
            }
        }
        return count;
    }

    private BangDiemRenLuyenResponse toResponse(BangDiemRenLuyen s) {
        return new BangDiemRenLuyenResponse(
                s.getId(),
                s.getDotRenLuyen() != null ? s.getDotRenLuyen().getId() : null,
                s.getDotRenLuyen() != null ? s.getDotRenLuyen().getTenDot() : "",
                s.getDotRenLuyen() != null ? s.getDotRenLuyen().getHocKy() : "",
                s.getDotRenLuyen() != null ? s.getDotRenLuyen().getNamHoc() : "",
                s.getSinhVien() != null ? s.getSinhVien().getId() : null,
                s.getSinhVien() != null ? s.getSinhVien().getMssv() : "",
                s.getSinhVien() != null ? s.getSinhVien().getHoTen() : "",
                s.getLop() != null ? s.getLop().getId() : null,
                s.getLop() != null ? s.getLop().getTenLop() : "",
                s.getLop() != null && s.getLop().getKhoa() != null ? s.getLop().getKhoa().getTenKhoa() : "",

                s.getDiemSvMuc1(),
                s.getDiemSvMuc2(),
                s.getDiemSvMuc3(),
                s.getDiemSvMuc4(),
                s.getDiemSvMuc5(),
                s.getTongDiemSv(),
                s.getGhiChuSv(),
                s.getThoiGianSvNop(),

                s.getDiemGvMuc1(),
                s.getDiemGvMuc2(),
                s.getDiemGvMuc3(),
                s.getDiemGvMuc4(),
                s.getDiemGvMuc5(),
                s.getTongDiemGv(),
                s.getNhanXetGv(),
                s.getGiangVien() != null ? s.getGiangVien().getHoTen() : null,
                s.getThoiGianGvDanhGia(),

                s.getDiemTongKet(),
                s.getXepLoai(),
                s.getNhanXetAdmin(),
                s.getThoiGianAdminDuyet(),

                s.getTrangThai() != null ? s.getTrangThai().name() : TrangThaiDrl.CHUA_DANH_GIA.name(),
                s.getTrangThai() != null ? s.getTrangThai().getMoTa() : "Chưa đánh giá"
        );
    }
}

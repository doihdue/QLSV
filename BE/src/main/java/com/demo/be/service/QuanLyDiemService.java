package com.demo.be.service;

import com.demo.be.dto.diem.AdminDuyetDiemRequest;
import com.demo.be.dto.diem.LecturerGradeBatchRequest;
import com.demo.be.dto.diem.LecturerGradeItemDto;
import com.demo.be.dto.diem.PendingApprovalClassDto;
import com.demo.be.dto.diem.QuanLyDiemRequest;
import com.demo.be.dto.diem.QuanLyDiemResponse;
import com.demo.be.exception.ResourceNotFoundException;
import com.demo.be.model.DangKyMonHoc;
import com.demo.be.model.MonHoc;
import com.demo.be.model.MonHocMo;
import com.demo.be.model.QuanLyDiem;
import com.demo.be.model.SinhVien;
import com.demo.be.repository.DangKyMonHocRepository;
import com.demo.be.repository.MonHocMoRepository;
import com.demo.be.repository.QuanLyDiemRepository;
import com.demo.be.repository.SinhVienRepository;
import com.demo.be.dto.message.NotificationEventMessage;
import com.demo.be.producer.NotificationProducer;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class QuanLyDiemService {

    private final QuanLyDiemRepository quanLyDiemRepository;
    private final DangKyMonHocRepository dangKyMonHocRepository;
    private final SinhVienRepository sinhVienRepository;
    private final MonHocMoRepository monHocMoRepository;
    private final NotificationProducer notificationProducer;

    public QuanLyDiemService(
            QuanLyDiemRepository quanLyDiemRepository,
            DangKyMonHocRepository dangKyMonHocRepository,
            SinhVienRepository sinhVienRepository,
            MonHocMoRepository monHocMoRepository,
            NotificationProducer notificationProducer
    ) {
        this.quanLyDiemRepository = quanLyDiemRepository;
        this.dangKyMonHocRepository = dangKyMonHocRepository;
        this.sinhVienRepository = sinhVienRepository;
        this.monHocMoRepository = monHocMoRepository;
        this.notificationProducer = notificationProducer;
    }

    public List<QuanLyDiemResponse> findAll() {
        return quanLyDiemRepository.findAll().stream().map(this::toResponse).toList();
    }

    public QuanLyDiemResponse findById(Long id) {
        return toResponse(getQuanLyDiem(id));
    }

    public List<QuanLyDiemResponse> findBySinhVien(Long sinhVienId) {
        return quanLyDiemRepository.findByDangKyMonHoc_SinhVien_Id(sinhVienId).stream()
                .filter(d -> "DA_DUYET".equalsIgnoreCase(d.getTrangThaiDuyet()))
                .map(this::toResponse).toList();
    }

    public List<QuanLyDiemResponse> findBySinhVienAndSemester(String mssv, String hocKy, String namHoc) {
        Long sinhVienId = findBySinhVienMssv(mssv);
        List<QuanLyDiem> list;
        if (hocKy == null || hocKy.isBlank() || namHoc == null || namHoc.isBlank()) {
            list = quanLyDiemRepository.findByDangKyMonHoc_SinhVien_Id(sinhVienId);
        } else {
            list = quanLyDiemRepository.findByDangKyMonHoc_SinhVien_IdAndDangKyMonHoc_HocKyAndDangKyMonHoc_NamHoc(sinhVienId, hocKy, namHoc);
        }
        return list.stream()
                .filter(d -> "DA_DUYET".equalsIgnoreCase(d.getTrangThaiDuyet()))
                .map(this::toResponse).toList();
    }

    public Long findBySinhVienMssv(String mssv) {
        return sinhVienRepository.findByMssv(mssv)
                .orElseThrow(() -> new ResourceNotFoundException("SinhVien not found with MSSV " + mssv))
                .getId();
    }

    @Transactional
    public QuanLyDiemResponse create(QuanLyDiemRequest request) {
        if (quanLyDiemRepository.findByDangKyMonHoc_Id(request.dangKyMonHocId()).isPresent()) {
            throw new IllegalStateException("Grade already exists for this registration");
        }

        QuanLyDiem quanLyDiem = new QuanLyDiem();
        apply(request, quanLyDiem);
        return toResponse(quanLyDiemRepository.save(quanLyDiem));
    }

    @Transactional
    public QuanLyDiemResponse update(Long id, QuanLyDiemRequest request) {
        QuanLyDiem quanLyDiem = getQuanLyDiem(id);
        apply(request, quanLyDiem);
        return toResponse(quanLyDiemRepository.save(quanLyDiem));
    }

    public void delete(Long id) {
        quanLyDiemRepository.delete(getQuanLyDiem(id));
    }

    /**
     * Lấy danh sách sinh viên và điểm số trong một môn học mở của lớp cho Giảng viên / Admin
     */
    public List<LecturerGradeItemDto> getStudentsAndGradesForMonHocMo(Long monHocMoId) {
        MonHocMo mhm = monHocMoRepository.findById(monHocMoId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy môn học mở với ID: " + monHocMoId));

        List<DangKyMonHoc> registrations = getRegistrationsForMonHocMo(mhm);

        return registrations.stream().map(reg -> {
            SinhVien sv = reg.getSinhVien();
            var diemOpt = quanLyDiemRepository.findByDangKyMonHoc_Id(reg.getId());
            if (diemOpt.isPresent()) {
                QuanLyDiem d = diemOpt.get();
                return new LecturerGradeItemDto(
                        reg.getId(),
                        sv.getId(),
                        sv.getMssv(),
                        sv.getHoTen(),
                        sv.getLop() != null ? sv.getLop().getTenLop() : "",
                        d.getId(),
                        d.getDiemChuyenCan(),
                        d.getDiemGiuaKy(),
                        d.getDiemCuoiKy(),
                        d.getDiemTongKet(),
                        d.getXepLoai(),
                        d.isDat(),
                        d.getTrangThaiDuyet() != null ? d.getTrangThaiDuyet() : "BAN_NHAP",
                        d.getGhiChuDuyet()
                );
            } else {
                return new LecturerGradeItemDto(
                        reg.getId(),
                        sv.getId(),
                        sv.getMssv(),
                        sv.getHoTen(),
                        sv.getLop() != null ? sv.getLop().getTenLop() : "",
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        false,
                        "BAN_NHAP",
                        null
                );
            }
        }).toList();
    }

    /**
     * Giảng viên lưu điểm theo lô (Lưu nháp hoặc Gửi duyệt)
     */
    @Transactional
    public List<LecturerGradeItemDto> saveGradesForMonHocMo(LecturerGradeBatchRequest request) {
        String targetTrangThai = request.submitForApproval() ? "CHO_DUYET" : "BAN_NHAP";

        for (var item : request.grades()) {
            if (item.dangKyMonHocId() == null) continue;
            DangKyMonHoc reg = dangKyMonHocRepository.findById(item.dangKyMonHocId())
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đăng ký môn học: " + item.dangKyMonHocId()));

            QuanLyDiem diem = quanLyDiemRepository.findByDangKyMonHoc_Id(item.dangKyMonHocId())
                    .orElseGet(() -> {
                        QuanLyDiem newDiem = new QuanLyDiem();
                        newDiem.setDangKyMonHoc(reg);
                        return newDiem;
                    });

            BigDecimal cc = normalize(item.diemChuyenCan());
            BigDecimal gk = normalize(item.diemGiuaKy());
            BigDecimal ck = normalize(item.diemCuoiKy());
            BigDecimal tongKet = calculateFinalScore(cc, gk, ck);

            diem.setDiemChuyenCan(cc);
            diem.setDiemGiuaKy(gk);
            diem.setDiemCuoiKy(ck);
            diem.setDiemTongKet(tongKet);
            diem.setXepLoai(determineRanking(tongKet));
            diem.setDat(tongKet.compareTo(BigDecimal.valueOf(5.0)) >= 0);
            diem.setTrangThaiDuyet(targetTrangThai);
            if (request.submitForApproval()) {
                diem.setGhiChuDuyet(null);
            }

            quanLyDiemRepository.save(diem);
        }

        if (request.submitForApproval()) {
            publishGradeSubmittedEvent(request.monHocMoId());
        }

        return getStudentsAndGradesForMonHocMo(request.monHocMoId());
    }

    /**
     * Lấy danh sách các lớp môn học đang chờ Admin duyệt điểm
     */
    public List<PendingApprovalClassDto> getPendingApprovalClasses() {
        List<MonHocMo> allMo = monHocMoRepository.findAll();
        List<PendingApprovalClassDto> result = new ArrayList<>();

        for (MonHocMo m : allMo) {
            List<DangKyMonHoc> regs = getRegistrationsForMonHocMo(m);
            if (regs.isEmpty()) continue;

            List<Long> regIds = regs.stream().map(DangKyMonHoc::getId).toList();
            List<QuanLyDiem> diems = quanLyDiemRepository.findByDangKyMonHoc_IdIn(regIds);

            boolean hasPending = diems.stream().anyMatch(d -> "CHO_DUYET".equalsIgnoreCase(d.getTrangThaiDuyet()));
            if (hasPending) {
                String ghiChu = diems.stream().map(QuanLyDiem::getGhiChuDuyet).filter(Objects::nonNull).findFirst().orElse(null);
                long gradedCount = diems.stream().filter(d -> d.getDiemTongKet() != null).count();
                int total = regs.size();
                String maLop = m.getLop() != null ? m.getLop().getMaLop() : "";
                String tenLop = m.getLop() != null ? m.getLop().getTenLop() : (m.getKhoaHoc() != null ? ("Toàn khóa " + m.getKhoaHoc()) : "Tất cả lớp");

                result.add(new PendingApprovalClassDto(
                        m.getId(),
                        m.getMonHoc() != null ? m.getMonHoc().getTenMonHoc() : "",
                        m.getMonHoc() != null ? m.getMonHoc().getMaMonHoc() : "",
                        m.getMonHoc() != null ? m.getMonHoc().getSoTinChi() : 0,
                        tenLop,
                        maLop,
                        m.getKhoaHoc(),
                        m.getHocKy(),
                        m.getNamHoc(),
                        m.getGiangVien() != null ? m.getGiangVien().getHoTen() : "Chưa phân công",
                        m.getGiangVien() != null ? m.getGiangVien().getMaGiangVien() : "",
                        total,
                        (int) gradedCount,
                        total,
                        "CHO_DUYET",
                        ghiChu
                ));
            }
        }
        return result;
    }

    /**
     * Admin duyệt công bố điểm cho cả lớp môn học
     */
    @Transactional
    public void approveGrades(Long monHocMoId) {
        MonHocMo m = monHocMoRepository.findById(monHocMoId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy môn học mở ID: " + monHocMoId));
        List<DangKyMonHoc> regs = getRegistrationsForMonHocMo(m);
        List<Long> regIds = regs.stream().map(DangKyMonHoc::getId).toList();
        List<QuanLyDiem> diems = quanLyDiemRepository.findByDangKyMonHoc_IdIn(regIds);

        for (QuanLyDiem d : diems) {
            d.setTrangThaiDuyet("DA_DUYET");
            d.setGhiChuDuyet(null);
            quanLyDiemRepository.save(d);
        }

        // Bắn sự kiện GRADE_APPROVED vào RabbitMQ ngay trong tầng Service
        publishGradeApprovalEvent(monHocMoId, "GRADE_APPROVED", null);
    }

    /**
     * Admin từ chối bảng điểm (kèm lý do) để giảng viên sửa lại
     */
    @Transactional
    public void rejectGrades(Long monHocMoId, AdminDuyetDiemRequest request) {
        MonHocMo m = monHocMoRepository.findById(monHocMoId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy môn học mở ID: " + monHocMoId));
        List<DangKyMonHoc> regs = getRegistrationsForMonHocMo(m);
        List<Long> regIds = regs.stream().map(DangKyMonHoc::getId).toList();
        List<QuanLyDiem> diems = quanLyDiemRepository.findByDangKyMonHoc_IdIn(regIds);

        String reason = (request != null && request.getEffectiveReason() != null)
                ? request.getEffectiveReason()
                : "Admin từ chối - Vui lòng kiểm tra lại điểm";

        for (QuanLyDiem d : diems) {
            d.setTrangThaiDuyet("TU_CHOI");
            d.setGhiChuDuyet(reason);
            quanLyDiemRepository.save(d);
        }

        // Bắn sự kiện GRADE_REJECTED vào RabbitMQ ngay trong tầng Service
        publishGradeApprovalEvent(monHocMoId, "GRADE_REJECTED", reason);
    }

    private void publishGradeSubmittedEvent(Long monHocMoId) {
        try {
            monHocMoRepository.findById(monHocMoId).ifPresent(mo -> {
                NotificationEventMessage msg = NotificationEventMessage.builder()
                        .eventType("GRADE_SUBMITTED")
                        .monHocMoId(monHocMoId)
                        .maMonHoc(mo.getMonHoc() != null ? mo.getMonHoc().getMaMonHoc() : "")
                        .tenMonHoc(mo.getMonHoc() != null ? mo.getMonHoc().getTenMonHoc() : "")
                        .maLop(mo.getLop() != null ? mo.getLop().getMaLop() : "")
                        .tenLop(mo.getLop() != null ? mo.getLop().getTenLop() : "")
                        .maGiangVien(mo.getGiangVien() != null ? mo.getGiangVien().getMaGiangVien() : "")
                        .tenGiangVien(mo.getGiangVien() != null ? mo.getGiangVien().getHoTen() : "")
                        .build();

                notificationProducer.sendNotification(msg);
            });
        } catch (Exception e) {
            log.warn("Lỗi khi phát sự kiện giảng viên nộp bảng điểm qua RabbitMQ: {}", e.getMessage());
        }
    }

    private void publishGradeApprovalEvent(Long monHocMoId, String eventType, String reason) {
        try {
            monHocMoRepository.findById(monHocMoId).ifPresent(mo -> {
                List<LecturerGradeItemDto> students = getStudentsAndGradesForMonHocMo(monHocMoId);
                List<String> mssvList = students.stream().map(LecturerGradeItemDto::sinhVienMssv).toList();

                NotificationEventMessage msg = NotificationEventMessage.builder()
                        .eventType(eventType)
                        .monHocMoId(monHocMoId)
                        .maMonHoc(mo.getMonHoc() != null ? mo.getMonHoc().getMaMonHoc() : "")
                        .tenMonHoc(mo.getMonHoc() != null ? mo.getMonHoc().getTenMonHoc() : "")
                        .maLop(mo.getLop() != null ? mo.getLop().getMaLop() : "")
                        .tenLop(mo.getLop() != null ? mo.getLop().getTenLop() : "")
                        .maGiangVien(mo.getGiangVien() != null ? mo.getGiangVien().getMaGiangVien() : "")
                        .tenGiangVien(mo.getGiangVien() != null ? mo.getGiangVien().getHoTen() : "")
                        .studentMssvList(mssvList)
                        .reason(reason)
                        .build();

                notificationProducer.sendNotification(msg);
            });
        } catch (Exception e) {
            log.warn("Lỗi khi phát sự kiện duyệt/từ chối điểm qua RabbitMQ: {}", e.getMessage());
        }
    }

    private List<DangKyMonHoc> getRegistrationsForMonHocMo(MonHocMo m) {
        if (m.getLop() != null) {
            return dangKyMonHocRepository.findByMonHoc_IdAndHocKyAndNamHocAndSinhVien_Lop_Id(
                    m.getMonHoc().getId(), m.getHocKy(), m.getNamHoc(), m.getLop().getId());
        }
        return dangKyMonHocRepository.findByMonHoc_IdAndHocKyAndNamHoc(
                m.getMonHoc().getId(), m.getHocKy(), m.getNamHoc());
    }

    private QuanLyDiem getQuanLyDiem(Long id) {
        return quanLyDiemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("QuanLyDiem not found with id " + id));
    }

    private void apply(QuanLyDiemRequest request, QuanLyDiem quanLyDiem) {
        DangKyMonHoc dangKyMonHoc = dangKyMonHocRepository.findById(request.dangKyMonHocId())
                .orElseThrow(() -> new ResourceNotFoundException("DangKyMonHoc not found with id " + request.dangKyMonHocId()));

        BigDecimal diemChuyenCan = normalize(request.diemChuyenCan());
        BigDecimal diemGiuaKy = normalize(request.diemGiuaKy());
        BigDecimal diemCuoiKy = normalize(request.diemCuoiKy());
        BigDecimal tongKet = calculateFinalScore(diemChuyenCan, diemGiuaKy, diemCuoiKy);

        quanLyDiem.setDangKyMonHoc(dangKyMonHoc);
        quanLyDiem.setDiemChuyenCan(diemChuyenCan);
        quanLyDiem.setDiemGiuaKy(diemGiuaKy);
        quanLyDiem.setDiemCuoiKy(diemCuoiKy);
        quanLyDiem.setDiemTongKet(tongKet);
        quanLyDiem.setXepLoai(determineRanking(tongKet));
        quanLyDiem.setDat(tongKet.compareTo(BigDecimal.valueOf(5.0)) >= 0);
        if (quanLyDiem.getTrangThaiDuyet() == null) {
            quanLyDiem.setTrangThaiDuyet("DA_DUYET"); // Khi admin tạo trực tiếp thì mặc định là DA_DUYET
        }
    }

    private BigDecimal normalize(BigDecimal value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateFinalScore(BigDecimal diemChuyenCan, BigDecimal diemGiuaKy, BigDecimal diemCuoiKy) {
        return diemChuyenCan.multiply(BigDecimal.valueOf(0.1))
                .add(diemGiuaKy.multiply(BigDecimal.valueOf(0.3)))
                .add(diemCuoiKy.multiply(BigDecimal.valueOf(0.6)))
                .setScale(2, RoundingMode.HALF_UP);
    }

    private String determineRanking(BigDecimal score) {
        if (score.compareTo(BigDecimal.valueOf(8.5)) >= 0) {
            return "Xuất sắc";
        }
        if (score.compareTo(BigDecimal.valueOf(7.0)) >= 0) {
            return "Khá";
        }
        if (score.compareTo(BigDecimal.valueOf(5.0)) >= 0) {
            return "Trung bình";
        }
        return "Không đạt";
    }

    private QuanLyDiemResponse toResponse(QuanLyDiem quanLyDiem) {
        DangKyMonHoc dangKyMonHoc = quanLyDiem.getDangKyMonHoc();
        SinhVien sinhVien = dangKyMonHoc != null ? dangKyMonHoc.getSinhVien() : null;
        MonHoc monHoc = dangKyMonHoc != null ? dangKyMonHoc.getMonHoc() : null;
        return new QuanLyDiemResponse(
                quanLyDiem.getId(),
                dangKyMonHoc != null ? dangKyMonHoc.getId() : null,
                sinhVien != null ? sinhVien.getId() : null,
                sinhVien != null ? sinhVien.getMssv() : null,
                sinhVien != null ? sinhVien.getHoTen() : null,
                monHoc != null ? monHoc.getId() : null,
                monHoc != null ? monHoc.getMaMonHoc() : null,
                monHoc != null ? monHoc.getTenMonHoc() : null,
                monHoc != null ? monHoc.getSoTinChi() : null,
                dangKyMonHoc != null ? dangKyMonHoc.getHocKy() : null,
                dangKyMonHoc != null ? dangKyMonHoc.getNamHoc() : null,
                quanLyDiem.getDiemChuyenCan(),
                quanLyDiem.getDiemGiuaKy(),
                quanLyDiem.getDiemCuoiKy(),
                quanLyDiem.getDiemTongKet(),
                quanLyDiem.getXepLoai(),
                quanLyDiem.isDat(),
                quanLyDiem.getTrangThaiDuyet(),
                quanLyDiem.getGhiChuDuyet()
        );
    }
}

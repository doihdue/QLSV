package com.demo.be.service;

import com.demo.be.dto.dangky.DangKyMonHocRequest;
import com.demo.be.dto.dangky.DangKyMonHocResponse;
import com.demo.be.exception.ResourceNotFoundException;
import com.demo.be.model.DangKyMonHoc;
import com.demo.be.model.MonHoc;
import com.demo.be.model.SinhVien;
import com.demo.be.repository.DangKyMonHocRepository;
import com.demo.be.repository.MonHocRepository;
import com.demo.be.repository.QuanLyDiemRepository;
import com.demo.be.repository.SinhVienRepository;
import java.time.LocalDateTime;
import java.util.List;
import com.demo.be.dto.dangky.StudentDangKyMonHocRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DangKyMonHocService {

    private final DangKyMonHocRepository dangKyMonHocRepository;
    private final SinhVienRepository sinhVienRepository;
    private final MonHocRepository monHocRepository;
    private final QuanLyDiemRepository quanLyDiemRepository;
    private final DotDangKyService dotDangKyService;

    public DangKyMonHocService(
            DangKyMonHocRepository dangKyMonHocRepository,
            SinhVienRepository sinhVienRepository,
            MonHocRepository monHocRepository,
            QuanLyDiemRepository quanLyDiemRepository,
            DotDangKyService dotDangKyService
    ) {
        this.dangKyMonHocRepository = dangKyMonHocRepository;
        this.sinhVienRepository = sinhVienRepository;
        this.monHocRepository = monHocRepository;
        this.quanLyDiemRepository = quanLyDiemRepository;
        this.dotDangKyService = dotDangKyService;
    }

    public List<DangKyMonHocResponse> findAll() {
        return dangKyMonHocRepository.findAll().stream().map(this::toResponse).toList();
    }

    public DangKyMonHocResponse findById(Long id) {
        return toResponse(getDangKyMonHoc(id));
    }

    public List<DangKyMonHocResponse> findBySinhVien(Long sinhVienId) {
        return dangKyMonHocRepository.findBySinhVien_Id(sinhVienId).stream().map(this::toResponse).toList();
    }

    public List<DangKyMonHocResponse> findBySinhVienAndSemester(String mssv, String hocKy, String namHoc) {
        SinhVien sinhVien = getSinhVienByMssv(mssv);
        if (hocKy == null || hocKy.isBlank() || namHoc == null || namHoc.isBlank()) {
            return findBySinhVien(sinhVien.getId());
        }
        return dangKyMonHocRepository.findBySinhVien_IdAndHocKyAndNamHoc(sinhVien.getId(), hocKy, namHoc)
                .stream().map(this::toResponse).toList();
    }

    @Transactional
    public DangKyMonHocResponse createForStudent(String mssv, StudentDangKyMonHocRequest request) {
        var currentDot = dotDangKyService.getCurrent();
        if (!currentDot.isDangMo()) {
            throw new IllegalStateException("Cổng đăng ký tín chỉ hiện đang đóng. Vui lòng liên hệ Phòng Đào tạo.");
        }
        if (!currentDot.getHocKy().equals(request.hocKy()) || !currentDot.getNamHoc().equals(request.namHoc())) {
            throw new IllegalStateException("Chỉ được phép đăng ký học phần thuộc đợt đang mở (Học kỳ "
                    + currentDot.getHocKy() + " - " + currentDot.getNamHoc() + "). Các học kỳ khác không được phép đăng ký.");
        }

        SinhVien sinhVien = getSinhVienByMssv(mssv);
        DangKyMonHocRequest fullRequest = new DangKyMonHocRequest(
                sinhVien.getId(), request.monHocId(), request.hocKy(), request.namHoc(), "DANG_KY");
        return create(fullRequest);
    }

    @Transactional
    public DangKyMonHocResponse create(DangKyMonHocRequest request) {
        if (dangKyMonHocRepository.existsBySinhVien_IdAndMonHoc_IdAndHocKyAndNamHoc(
                request.sinhVienId(), request.monHocId(), request.hocKy(), request.namHoc())) {
            throw new IllegalStateException("Student already registered this subject in the same semester");
        }

        DangKyMonHoc dangKyMonHoc = new DangKyMonHoc();
        apply(request, dangKyMonHoc);
        return toResponse(dangKyMonHocRepository.save(dangKyMonHoc));
    }

    @Transactional
    public DangKyMonHocResponse update(Long id, DangKyMonHocRequest request) {
        DangKyMonHoc dangKyMonHoc = getDangKyMonHoc(id);
        apply(request, dangKyMonHoc);
        return toResponse(dangKyMonHocRepository.save(dangKyMonHoc));
    }

    @Transactional
    public void deleteForStudent(String mssv, Long id) {
        DangKyMonHoc dangKyMonHoc = getDangKyMonHoc(id);
        if (dangKyMonHoc.getSinhVien() == null || !dangKyMonHoc.getSinhVien().getMssv().equals(mssv)) {
            throw new IllegalStateException("Bạn không có quyền hủy đăng ký môn học của sinh viên khác");
        }

        var currentDot = dotDangKyService.getCurrent();
        if (!currentDot.isDangMo()) {
            throw new IllegalStateException("Cổng đăng ký tín chỉ hiện đang đóng. Không thể hủy học phần.");
        }
        if (!currentDot.getHocKy().equals(dangKyMonHoc.getHocKy()) || !currentDot.getNamHoc().equals(dangKyMonHoc.getNamHoc())) {
            throw new IllegalStateException("Không thể hủy môn học của các học kỳ trước đó đã kết thúc.");
        }

        if (quanLyDiemRepository.findByDangKyMonHoc_Id(id).isPresent()) {
            throw new IllegalStateException("Không thể hủy môn học đã có kết quả đánh giá điểm");
        }
        dangKyMonHocRepository.delete(dangKyMonHoc);
    }

    @Transactional
    public void delete(Long id) {
        DangKyMonHoc dangKyMonHoc = getDangKyMonHoc(id);
        quanLyDiemRepository.findByDangKyMonHoc_Id(id).ifPresent(quanLyDiemRepository::delete);
        dangKyMonHocRepository.delete(dangKyMonHoc);
    }

    private DangKyMonHoc getDangKyMonHoc(Long id) {
        return dangKyMonHocRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("DangKyMonHoc not found with id " + id));
    }

    private SinhVien getSinhVienByMssv(String mssv) {
        return sinhVienRepository.findByMssv(mssv)
                .orElseThrow(() -> new ResourceNotFoundException("SinhVien not found with MSSV " + mssv));
    }

    private void apply(DangKyMonHocRequest request, DangKyMonHoc dangKyMonHoc) {
        SinhVien sinhVien = sinhVienRepository.findById(request.sinhVienId())
                .orElseThrow(() -> new ResourceNotFoundException("SinhVien not found with id " + request.sinhVienId()));
        MonHoc monHoc = monHocRepository.findById(request.monHocId())
                .orElseThrow(() -> new ResourceNotFoundException("MonHoc not found with id " + request.monHocId()));
        dangKyMonHoc.setSinhVien(sinhVien);
        dangKyMonHoc.setMonHoc(monHoc);
        dangKyMonHoc.setHocKy(request.hocKy());
        dangKyMonHoc.setNamHoc(request.namHoc());
        dangKyMonHoc.setNgayDangKy(LocalDateTime.now());
        dangKyMonHoc.setTrangThai(request.trangThai() == null || request.trangThai().isBlank()
                ? "DANG_KY"
                : request.trangThai());
    }

    @Transactional
    public List<DangKyMonHocResponse> dangKyChoLopHanhChinh(Long lopId, Long monHocId, String hocKy, String namHoc) {
        MonHoc monHoc = monHocRepository.findById(monHocId)
                .orElseThrow(() -> new ResourceNotFoundException("MonHoc not found with id " + monHocId));

        List<SinhVien> sinhViens = sinhVienRepository.findByLop_Id(lopId);
        for (SinhVien sv : sinhViens) {
            if (!dangKyMonHocRepository.existsBySinhVien_IdAndMonHoc_IdAndHocKyAndNamHoc(
                    sv.getId(), monHocId, hocKy, namHoc)) {
                DangKyMonHoc dk = new DangKyMonHoc();
                dk.setSinhVien(sv);
                dk.setMonHoc(monHoc);
                dk.setHocKy(hocKy);
                dk.setNamHoc(namHoc);
                dk.setNgayDangKy(LocalDateTime.now());
                dk.setTrangThai("DANG_KY");
                dangKyMonHocRepository.save(dk);
            }
        }
        return findAll();
    }

    private DangKyMonHocResponse toResponse(DangKyMonHoc dangKyMonHoc) {
        SinhVien sinhVien = dangKyMonHoc.getSinhVien();
        MonHoc monHoc = dangKyMonHoc.getMonHoc();
        com.demo.be.model.Lop lop = sinhVien != null ? sinhVien.getLop() : null;
        return new DangKyMonHocResponse(
                dangKyMonHoc.getId(),
                sinhVien != null ? sinhVien.getId() : null,
                sinhVien != null ? sinhVien.getMssv() : null,
                sinhVien != null ? sinhVien.getHoTen() : null,
                lop != null ? lop.getId() : null,
                lop != null ? lop.getMaLop() : null,
                lop != null ? lop.getTenLop() : null,
                monHoc != null ? monHoc.getId() : null,
                monHoc != null ? monHoc.getMaMonHoc() : null,
                monHoc != null ? monHoc.getTenMonHoc() : null,
                monHoc != null ? monHoc.getSoTinChi() : null,
                dangKyMonHoc.getHocKy(),
                dangKyMonHoc.getNamHoc(),
                dangKyMonHoc.getNgayDangKy(),
                dangKyMonHoc.getTrangThai()
        );
    }
}

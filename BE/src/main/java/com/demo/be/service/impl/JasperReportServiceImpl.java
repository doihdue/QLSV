package com.demo.be.service.impl;

import com.demo.be.dto.report.DrlReportItemDto;
import com.demo.be.exception.ResourceNotFoundException;
import com.demo.be.model.BangDiemRenLuyen;
import com.demo.be.model.DotRenLuyen;
import com.demo.be.model.Lop;
import com.demo.be.model.SinhVien;
import com.demo.be.repository.BangDiemRenLuyenRepository;
import com.demo.be.repository.DotRenLuyenRepository;
import com.demo.be.repository.LopRepository;
import com.demo.be.repository.SinhVienRepository;
import com.demo.be.service.JasperReportService;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import com.demo.be.dto.report.GpaReportItemDto;
import com.demo.be.dto.thongke.SinhVienGpaResponse;
import com.demo.be.service.ThongKeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class JasperReportServiceImpl implements JasperReportService {

    private static final Logger log = LoggerFactory.getLogger(JasperReportServiceImpl.class);

    private final DotRenLuyenRepository dotRenLuyenRepository;
    private final LopRepository lopRepository;
    private final BangDiemRenLuyenRepository bangDiemRenLuyenRepository;
    private final SinhVienRepository sinhVienRepository;
    private final ThongKeService thongKeService;

    public JasperReportServiceImpl(
            DotRenLuyenRepository dotRenLuyenRepository,
            LopRepository lopRepository,
            BangDiemRenLuyenRepository bangDiemRenLuyenRepository,
            SinhVienRepository sinhVienRepository,
            ThongKeService thongKeService
    ) {
        this.dotRenLuyenRepository = dotRenLuyenRepository;
        this.lopRepository = lopRepository;
        this.bangDiemRenLuyenRepository = bangDiemRenLuyenRepository;
        this.sinhVienRepository = sinhVienRepository;
        this.thongKeService = thongKeService;
    }

    @Override
    public File generateDrlReport(Long dotId, Long lopId, String outputPath) throws Exception {
        log.info("[JASPER REPORT] Bắt đầu tạo báo cáo điểm rèn luyện (dotId={}, lopId={}) -> {}", dotId, lopId, outputPath);

        DotRenLuyen dot = dotRenLuyenRepository.findById(dotId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đợt rèn luyện với ID " + dotId));
        Lop lop = lopRepository.findById(lopId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy lớp với ID " + lopId));

        List<SinhVien> sinhViens = sinhVienRepository.findByLop_Id(lopId);
        List<BangDiemRenLuyen> sheets = bangDiemRenLuyenRepository.findByDotRenLuyen_IdAndLop_Id(dotId, lopId);

        List<DrlReportItemDto> items = new ArrayList<>();
        int stt = 1;
        for (SinhVien sv : sinhViens) {
            Optional<BangDiemRenLuyen> sheetOpt = sheets.stream()
                    .filter(s -> s.getSinhVien().getId().equals(sv.getId()))
                    .findFirst();

            if (sheetOpt.isPresent()) {
                BangDiemRenLuyen s = sheetOpt.get();
                items.add(new DrlReportItemDto(
                        stt++,
                        sv.getMssv(),
                        sv.getHoTen(),
                        s.getTongDiemSv(),
                        s.getTongDiemGv(),
                        s.getDiemTongKet(),
                        s.getXepLoai(),
                        s.getTrangThai() != null ? s.getTrangThai().getMoTa() : "Chưa đánh giá"
                ));
            } else {
                items.add(new DrlReportItemDto(
                        stt++,
                        sv.getMssv(),
                        sv.getHoTen(),
                        null,
                        null,
                        null,
                        null,
                        "Chưa đánh giá"
                ));
            }
        }

        items.sort(Comparator.comparing(DrlReportItemDto::getMssv));
        // Reset STT sau khi sort
        for (int i = 0; i < items.size(); i++) {
            items.get(i).setStt(i + 1);
        }

        double avg = items.stream()
                .filter(i -> i.getDiemTongKet() != null)
                .mapToInt(DrlReportItemDto::getDiemTongKet)
                .average()
                .orElse(0.0);

        Map<String, Object> params = new HashMap<>();
        params.put("TEN_DOT", dot.getTenDot());
        params.put("HOC_KY_NAM_HOC", "Học kỳ " + dot.getHocKy() + " - Năm học " + dot.getNamHoc());
        params.put("TEN_LOP", lop.getTenLop() + " (" + lop.getMaLop() + ")");
        params.put("KHOA", lop.getKhoa() != null ? lop.getKhoa().getTenKhoa() : "Khoa Đào tạo");
        params.put("TONG_SO_SV", items.size());
        params.put("DIEM_TRUNG_BINH", Math.round(avg * 100.0) / 100.0);
        params.put("NGAY_XUAT", "ngày " + LocalDate.now().getDayOfMonth() + " tháng " + LocalDate.now().getMonthValue() + " năm " + LocalDate.now().getYear());
        params.put("NGUOI_XUAT", "Hệ thống Quản lý Sinh viên");

        InputStream templateStream = getClass().getResourceAsStream("/reports/diem_ren_luyen_report.jrxml");
        if (templateStream == null) {
            throw new IllegalStateException("Không tìm thấy tệp mẫu báo cáo JasperReports: /reports/diem_ren_luyen_report.jrxml");
        }

        JasperReport jasperReport = JasperCompileManager.compileReport(templateStream);
        JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(items);
        JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, params, dataSource);

        File outputFile = new File(outputPath);
        if (outputFile.getParentFile() != null) {
            outputFile.getParentFile().mkdirs();
        }

        JasperExportManager.exportReportToPdfFile(jasperPrint, outputPath);
        log.info("[JASPER REPORT] Tạo báo cáo PDF thành công tại: {}", outputPath);
        return outputFile;
    }

    @Override
    public File generateGpaReport(Long lopId, String outputPath) throws Exception {
        log.info("[JASPER REPORT] Bắt đầu tạo báo cáo thống kê GPA (lopId={}) -> {}", lopId, outputPath);

        List<SinhVienGpaResponse> rawData = thongKeService.getThongKeGpa(lopId);

        String tieuDeBoLoc = "Toàn trường";
        if (lopId != null) {
            Optional<Lop> lopOpt = lopRepository.findById(lopId);
            if (lopOpt.isPresent()) {
                Lop lop = lopOpt.get();
                tieuDeBoLoc = "Lớp " + lop.getTenLop() + " (" + lop.getMaLop() + ")";
            }
        }

        List<GpaReportItemDto> items = new ArrayList<>();
        int stt = 1;
        for (SinhVienGpaResponse r : rawData) {
            items.add(new GpaReportItemDto(
                    stt++,
                    r.mssv(),
                    r.hoTen(),
                    r.tenLop(),
                    r.tenKhoa(),
                    r.soMonHoc(),
                    r.tinChiTichLuy(),
                    r.diemTrungBinh(),
                    r.xepLoaiHocLuc()
            ));
        }

        double avgGpa = items.stream()
                .filter(i -> i.getDiemTrungBinh() != null)
                .mapToDouble(GpaReportItemDto::getDiemTrungBinh)
                .average()
                .orElse(0.0);

        String topStudentStr = "Chưa có dữ liệu";
        Optional<GpaReportItemDto> topOpt = items.stream()
                .filter(i -> i.getDiemTrungBinh() != null)
                .max(Comparator.comparingDouble(GpaReportItemDto::getDiemTrungBinh));
        if (topOpt.isPresent()) {
            GpaReportItemDto top = topOpt.get();
            topStudentStr = top.getHoTen() + " (" + top.getMssv() + ") - GPA: " + top.getDiemTrungBinh();
        }

        Map<String, Object> params = new HashMap<>();
        params.put("TIEU_DE_BO_LOC", tieuDeBoLoc);
        params.put("TONG_SO_SV", items.size());
        params.put("DIEM_TRUNG_BINH_CHUNG", Math.round(avgGpa * 100.0) / 100.0);
        params.put("SV_XUAT_SAC", topStudentStr);
        params.put("NGAY_XUAT", "ngày " + LocalDate.now().getDayOfMonth() + " tháng " + LocalDate.now().getMonthValue() + " năm " + LocalDate.now().getYear());
        params.put("NGUOI_XUAT", "Hệ thống Quản lý Sinh viên");

        InputStream templateStream = getClass().getResourceAsStream("/reports/thong_ke_gpa_report.jrxml");
        if (templateStream == null) {
            throw new IllegalStateException("Không tìm thấy tệp mẫu báo cáo JasperReports: /reports/thong_ke_gpa_report.jrxml");
        }

        JasperReport jasperReport = JasperCompileManager.compileReport(templateStream);
        JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(items);
        JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, params, dataSource);

        File outputFile = new File(outputPath);
        if (outputFile.getParentFile() != null) {
            outputFile.getParentFile().mkdirs();
        }

        JasperExportManager.exportReportToPdfFile(jasperPrint, outputPath);
        log.info("[JASPER REPORT] Tạo báo cáo GPA PDF thành công tại: {}", outputPath);
        return outputFile;
    }
}

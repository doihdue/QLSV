package com.demo.be.service;

import com.demo.be.dto.report.DrlReportItemDto;
import com.demo.be.model.DotRenLuyen;
import com.demo.be.model.Khoa;
import com.demo.be.model.Lop;
import com.demo.be.model.SinhVien;
import com.demo.be.repository.BangDiemRenLuyenRepository;
import com.demo.be.repository.DotRenLuyenRepository;
import com.demo.be.repository.LopRepository;
import com.demo.be.repository.SinhVienRepository;
import com.demo.be.service.impl.JasperReportServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.nio.file.Files;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JasperReportServiceTest {

    @Mock
    private DotRenLuyenRepository dotRenLuyenRepository;

    @Mock
    private LopRepository lopRepository;

    @Mock
    private BangDiemRenLuyenRepository bangDiemRenLuyenRepository;

    @Mock
    private SinhVienRepository sinhVienRepository;

    @Mock
    private ThongKeService thongKeService;

    @InjectMocks
    private JasperReportServiceImpl jasperReportService;

    @Test
    void testGenerateDrlReport_Success() throws Exception {
        Long dotId = 1L;
        Long lopId = 1L;

        DotRenLuyen dot = new DotRenLuyen();
        dot.setId(dotId);
        dot.setTenDot("Đợt đánh giá rèn luyện HK1 2026-2027");
        dot.setHocKy("1");
        dot.setNamHoc("2026-2027");

        Khoa khoa = new Khoa();
        khoa.setTenKhoa("Công nghệ Thông tin");

        Lop lop = new Lop();
        lop.setId(lopId);
        lop.setTenLop("Kỹ thuật phần mềm 1");
        lop.setMaLop("KTPM01");
        lop.setKhoa(khoa);

        SinhVien sv = new SinhVien();
        sv.setId(10L);
        sv.setMssv("SV2026001");
        sv.setHoTen("Nguyễn Văn An");
        sv.setLop(lop);

        when(dotRenLuyenRepository.findById(dotId)).thenReturn(Optional.of(dot));
        when(lopRepository.findById(lopId)).thenReturn(Optional.of(lop));
        when(sinhVienRepository.findByLop_Id(lopId)).thenReturn(List.of(sv));
        when(bangDiemRenLuyenRepository.findByDotRenLuyen_IdAndLop_Id(dotId, lopId)).thenReturn(Collections.emptyList());

        File tempFile = File.createTempFile("test_report_", ".pdf");
        tempFile.deleteOnExit();

        File result = jasperReportService.generateDrlReport(dotId, lopId, tempFile.getAbsolutePath());

        File storageDir = new File("reports_storage");
        if (!storageDir.exists()) storageDir.mkdirs();
        File sampleDrlPdf = new File(storageDir, "BaoCao_Mau_DiemRenLuyen.pdf");
        Files.copy(result.toPath(), sampleDrlPdf.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);

        Files.deleteIfExists(tempFile.toPath());
    }

    @Test
    void testGenerateGpaReport_Success() throws Exception {
        Long lopId = 1L;

        Lop lop = new Lop();
        lop.setId(lopId);
        lop.setTenLop("Kỹ thuật phần mềm 1");
        lop.setMaLop("KTPM01");

        com.demo.be.dto.thongke.SinhVienGpaResponse gpaItem1 = new com.demo.be.dto.thongke.SinhVienGpaResponse(
                10L, "SV2026001", "Nguyễn Văn An", "Kỹ thuật phần mềm 1", "CNTT", 5, 15, 8.8, "Xuất sắc"
        );
        com.demo.be.dto.thongke.SinhVienGpaResponse gpaItem2 = new com.demo.be.dto.thongke.SinhVienGpaResponse(
                11L, "SV2026002", "Trần Thị Bích", "Kỹ thuật phần mềm 1", "CNTT", 5, 15, 7.9, "Khá / Giỏi"
        );

        when(lopRepository.findById(lopId)).thenReturn(Optional.of(lop));
        when(thongKeService.getThongKeGpa(lopId)).thenReturn(List.of(gpaItem1, gpaItem2));

        File tempFile = File.createTempFile("test_gpa_report_", ".pdf");
        tempFile.deleteOnExit();

        File result = jasperReportService.generateGpaReport(lopId, tempFile.getAbsolutePath());

        assertTrue(result.exists());
        assertTrue(result.length() > 0, "GPA PDF generated should have content > 0 bytes");

        File storageDir = new File("reports_storage");
        if (!storageDir.exists()) storageDir.mkdirs();
        File sampleGpaPdf = new File(storageDir, "BaoCao_Mau_ThongKe_GPA.pdf");
        Files.copy(result.toPath(), sampleGpaPdf.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);

        Files.deleteIfExists(tempFile.toPath());
    }
}

package com.demo.be.service;

import com.demo.be.dto.monhocmo.MonHocMoRequest;
import com.demo.be.dto.monhocmo.MonHocMoResponse;
import com.demo.be.exception.ResourceNotFoundException;
import com.demo.be.model.Khoa;
import com.demo.be.model.Lop;
import com.demo.be.model.MonHoc;
import com.demo.be.model.MonHocMo;
import com.demo.be.model.SinhVien;
import com.demo.be.repository.KhoaRepository;
import com.demo.be.repository.LopRepository;
import com.demo.be.repository.MonHocMoRepository;
import com.demo.be.repository.MonHocRepository;
import com.demo.be.repository.SinhVienRepository;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MonHocMoService {

    private final MonHocMoRepository monHocMoRepository;
    private final MonHocRepository monHocRepository;
    private final KhoaRepository khoaRepository;
    private final LopRepository lopRepository;
    private final SinhVienRepository sinhVienRepository;
    private final DotDangKyService dotDangKyService;
    private final com.demo.be.repository.GiangVienRepository giangVienRepository;

    private static final Pattern KHOA_HOC_PATTERN = Pattern.compile("^([A-Za-z]+\\d+)", Pattern.CASE_INSENSITIVE);

    public MonHocMoService(
            MonHocMoRepository monHocMoRepository,
            MonHocRepository monHocRepository,
            KhoaRepository khoaRepository,
            LopRepository lopRepository,
            SinhVienRepository sinhVienRepository,
            DotDangKyService dotDangKyService,
            com.demo.be.repository.GiangVienRepository giangVienRepository
    ) {
        this.monHocMoRepository = monHocMoRepository;
        this.monHocRepository = monHocRepository;
        this.khoaRepository = khoaRepository;
        this.lopRepository = lopRepository;
        this.sinhVienRepository = sinhVienRepository;
        this.dotDangKyService = dotDangKyService;
        this.giangVienRepository = giangVienRepository;
    }

    public List<MonHocMoResponse> findByFilters(String hocKy, String namHoc, Long khoaId, String khoaHoc, Long lopId) {
        if (hocKy == null || hocKy.isBlank() || namHoc == null || namHoc.isBlank()) {
            var currentDot = dotDangKyService.getCurrent();
            hocKy = (hocKy != null && !hocKy.isBlank()) ? hocKy : currentDot.getHocKy();
            namHoc = (namHoc != null && !namHoc.isBlank()) ? namHoc : currentDot.getNamHoc();
        }
        return monHocMoRepository.findByFilters(hocKy, namHoc, khoaId, (khoaHoc != null && !khoaHoc.isBlank()) ? khoaHoc : null, lopId)
                .stream().map(this::toResponse).toList();
    }

    @Transactional
    public MonHocMoResponse create(MonHocMoRequest request) {
        if (monHocMoRepository.existsByMonHoc_IdAndKhoa_IdAndKhoaHocAndHocKyAndNamHoc(
                request.monHocId(), request.khoaId(), request.khoaHoc(), request.hocKy(), request.namHoc())) {
            throw new IllegalStateException("Môn học này đã được mở cho khóa " + request.khoaHoc()
                    + " thuộc khoa này trong Học kỳ " + request.hocKy() + " (" + request.namHoc() + ")");
        }

        MonHoc monHoc = monHocRepository.findById(request.monHocId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy môn học với ID: " + request.monHocId()));

        Khoa khoa = khoaRepository.findById(request.khoaId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy khoa với ID: " + request.khoaId()));

        Lop lop = null;
        if (request.lopId() != null) {
            lop = lopRepository.findById(request.lopId()).orElse(null);
        }

        String rawKhoaHoc = request.khoaHoc() != null ? request.khoaHoc().trim() : "";
        String cleanKhoaHoc = rawKhoaHoc.replaceAll("\\D+", "");
        String savedKhoaHoc = cleanKhoaHoc.isEmpty() ? rawKhoaHoc.toUpperCase() : cleanKhoaHoc;

        MonHocMo monHocMo = new MonHocMo(
                monHoc,
                khoa,
                savedKhoaHoc,
                request.hocKy(),
                request.namHoc(),
                lop,
                request.ghiChu()
        );

        if (request.giangVienId() != null) {
            var gv = giangVienRepository.findById(request.giangVienId())
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy giảng viên với ID: " + request.giangVienId()));
            monHocMo.setGiangVien(gv);
        }

        return toResponse(monHocMoRepository.save(monHocMo));
    }

    @Transactional
    public MonHocMoResponse ganGiangVien(Long id, Long giangVienId) {
        MonHocMo monHocMo = monHocMoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy môn học mở với ID: " + id));
        if (giangVienId != null) {
            var gv = giangVienRepository.findById(giangVienId)
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy giảng viên với ID: " + giangVienId));
            monHocMo.setGiangVien(gv);
        } else {
            monHocMo.setGiangVien(null);
        }
        return toResponse(monHocMoRepository.save(monHocMo));
    }

    public List<MonHocMoResponse> findByGiangVien(String maGiangVien, String hocKy, String namHoc) {
        if (hocKy != null && !hocKy.isBlank() && namHoc != null && !namHoc.isBlank()) {
            return monHocMoRepository.findByGiangVien_MaGiangVienAndHocKyAndNamHoc(maGiangVien, hocKy, namHoc)
                    .stream().map(this::toResponse).toList();
        }
        return monHocMoRepository.findByGiangVien_MaGiangVien(maGiangVien)
                .stream().map(this::toResponse).toList();
    }

    @Transactional
    public void delete(Long id) {
        if (!monHocMoRepository.existsById(id)) {
            throw new ResourceNotFoundException("Không tìm thấy môn học mở với ID: " + id);
        }
        monHocMoRepository.deleteById(id);
    }

    private static final Pattern MSSV_KHOA_PATTERN = Pattern.compile("^[A-Za-z]*(\\d{2})");

    /**
     * Trích xuất số khóa từ Mã Sinh Viên (ví dụ: B22DCCN001 -> "22", B21DCAT045 -> "21")
     */
    public static String extractKhoaHocFromMssv(String mssv) {
        if (mssv == null || mssv.isBlank()) return "";
        Matcher m = MSSV_KHOA_PATTERN.matcher(mssv.trim());
        if (m.find()) {
            return m.group(1); // "22"
        }
        return "";
    }

    /**
     * Lấy danh sách các môn học mở cho Lớp hành chính & Khóa học của sinh viên
     */
    public List<MonHocMoResponse> getMonHocMoForStudent(String mssv, String hocKy, String namHoc) {
        SinhVien sv = sinhVienRepository.findByMssv(mssv)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sinh viên với MSSV: " + mssv));

        if (hocKy == null || hocKy.isBlank() || namHoc == null || namHoc.isBlank()) {
            var currentDot = dotDangKyService.getCurrent();
            hocKy = (hocKy != null && !hocKy.isBlank()) ? hocKy : currentDot.getHocKy();
            namHoc = (namHoc != null && !namHoc.isBlank()) ? namHoc : currentDot.getNamHoc();
        }

        Lop lop = sv.getLop();
        if (lop == null) {
            return List.of();
        }

        Long khoaId = lop.getKhoa() != null ? lop.getKhoa().getId() : null;
        if (khoaId == null) {
            return List.of();
        }

        // Nhận diện khóa theo MSSV: B22... -> khóa 22
        String khoaNum = extractKhoaHocFromMssv(sv.getMssv());
        String khoaHoc = !khoaNum.isEmpty() ? ("B" + khoaNum) : extractKhoaHoc(lop);

        List<MonHocMo> list = monHocMoRepository.findForStudent(hocKy, namHoc, khoaId, khoaHoc, khoaNum, lop.getId());

        // Nếu chưa cấu hình riêng cho khóa này, tìm tất cả môn mở của khoa trong kỳ
        if (list.isEmpty()) {
            list = monHocMoRepository.findByFilters(hocKy, namHoc, khoaId, null, null);
        }

        return list.stream().map(this::toResponse).toList();
    }

    public String extractKhoaHoc(SinhVien sv) {
        if (sv == null) return "22";
        String fromMssv = extractKhoaHocFromMssv(sv.getMssv());
        if (!fromMssv.isEmpty()) {
            return fromMssv;
        }
        return extractKhoaHoc(sv.getLop());
    }

    public String extractKhoaHoc(Lop lop) {
        if (lop == null) return "22";
        if (lop.getMaLop() != null) {
            Matcher m = KHOA_HOC_PATTERN.matcher(lop.getMaLop().trim());
            if (m.find()) {
                return m.group(1).toUpperCase();
            }
        }
        if (lop.getNienKhoa() != null) {
            String nk = lop.getNienKhoa().trim();
            if (nk.startsWith("20") && nk.length() >= 4) {
                return nk.substring(2, 4);
            }
            return nk.toUpperCase();
        }
        return "22";
    }

    private MonHocMoResponse toResponse(MonHocMo m) {
        String respKhoaHoc = m.getKhoaHoc() != null ? m.getKhoaHoc().replaceAll("\\D+", "") : "";
        if (respKhoaHoc.isEmpty() && m.getKhoaHoc() != null) {
            respKhoaHoc = m.getKhoaHoc();
        }

        return new MonHocMoResponse(
                m.getId(),
                m.getMonHoc() != null ? m.getMonHoc().getId() : null,
                m.getMonHoc() != null ? m.getMonHoc().getMaMonHoc() : "",
                m.getMonHoc() != null ? m.getMonHoc().getTenMonHoc() : "",
                m.getMonHoc() != null ? m.getMonHoc().getSoTinChi() : 0,
                m.getMonHoc() != null ? m.getMonHoc().getSoTietLyThuyet() : 0,
                m.getMonHoc() != null ? m.getMonHoc().getSoTietThucHanh() : 0,
                m.getMonHoc() != null ? m.getMonHoc().getMonHocTienQuyet() : "",
                m.getKhoa() != null ? m.getKhoa().getId() : null,
                m.getKhoa() != null ? m.getKhoa().getTenKhoa() : "",
                respKhoaHoc,
                m.getHocKy(),
                m.getNamHoc(),
                m.getLop() != null ? m.getLop().getId() : null,
                m.getLop() != null ? m.getLop().getTenLop() : null,
                m.getLop() != null ? m.getLop().getMaLop() : null,
                m.getGiangVien() != null ? m.getGiangVien().getId() : null,
                m.getGiangVien() != null ? m.getGiangVien().getMaGiangVien() : null,
                m.getGiangVien() != null ? m.getGiangVien().getHoTen() : null,
                m.getGhiChu()
        );
    }
}

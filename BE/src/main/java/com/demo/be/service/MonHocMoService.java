package com.demo.be.service;

import com.demo.be.dto.monhocmo.MonHocMoRequest;
import com.demo.be.dto.monhocmo.MonHocMoResponse;
import com.demo.be.exception.ResourceNotFoundException;
import com.demo.be.model.GiangVien;
import com.demo.be.model.Khoa;
import com.demo.be.model.Lop;
import com.demo.be.model.MonHoc;
import com.demo.be.model.MonHocMo;
import com.demo.be.model.SinhVien;
import com.demo.be.repository.GiangVienRepository;
import com.demo.be.repository.KhoaRepository;
import com.demo.be.repository.LopRepository;
import com.demo.be.repository.MonHocMoRepository;
import com.demo.be.repository.MonHocRepository;
import com.demo.be.repository.SinhVienRepository;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
    private final GiangVienRepository giangVienRepository;

    private static final Pattern KHOA_HOC_PATTERN = Pattern.compile("^([A-Za-z]+\\d+)", Pattern.CASE_INSENSITIVE);

    public MonHocMoService(
            MonHocMoRepository monHocMoRepository,
            MonHocRepository monHocRepository,
            KhoaRepository khoaRepository,
            LopRepository lopRepository,
            SinhVienRepository sinhVienRepository,
            DotDangKyService dotDangKyService,
            GiangVienRepository giangVienRepository
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
        var rawList = monHocMoRepository.findByFilters(hocKy, namHoc, khoaId, (khoaHoc != null && !khoaHoc.isBlank()) ? khoaHoc : null, lopId);
        if (lopId == null) {
            // Khi xem tổng quan các môn mở theo Khoa & Khóa (chưa chọn lớp cụ thể), gom nhóm để mỗi môn hiển thị 1 dòng đại diện
            java.util.Map<Long, MonHocMo> uniqueMap = new java.util.LinkedHashMap<>();
            for (MonHocMo m : rawList) {
                if (m.getLop() == null) {
                    uniqueMap.put(m.getMonHoc().getId(), m);
                }
            }
            for (MonHocMo m : rawList) {
                if (!uniqueMap.containsKey(m.getMonHoc().getId())) {
                    uniqueMap.put(m.getMonHoc().getId(), m);
                }
            }
            return uniqueMap.values().stream().map(this::toResponse).toList();
        }
        return rawList.stream().map(this::toResponse).toList();
    }

    /**
     * Lấy danh sách các môn học mở cho một Lớp hành chính cụ thể để phân công Giảng viên
     */
    public List<MonHocMoResponse> findForLop(Long lopId, String hocKy, String namHoc) {
        if (hocKy == null || hocKy.isBlank() || namHoc == null || namHoc.isBlank()) {
            var currentDot = dotDangKyService.getCurrent();
            hocKy = (hocKy != null && !hocKy.isBlank()) ? hocKy : currentDot.getHocKy();
            namHoc = (namHoc != null && !namHoc.isBlank()) ? namHoc : currentDot.getNamHoc();
        }

        Lop lop = lopRepository.findById(lopId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy lớp với ID: " + lopId));
        Long khoaId = lop.getKhoa() != null ? lop.getKhoa().getId() : null;
        String khoaHoc = extractKhoaHoc(lop);
        String shortKhoa = (khoaHoc != null && khoaHoc.length() == 4) ? khoaHoc.substring(2) : khoaHoc;

        // 1. Môn học đã mở và gán riêng cho lớp này
        List<MonHocMo> classMoList = monHocMoRepository.findByLop_IdAndHocKyAndNamHoc(lopId, hocKy, namHoc);

        // 2. Môn học mở chung theo Khoa & Khóa (lop IS NULL)
        List<MonHocMo> cohortMoList = monHocMoRepository.findCohortOpenings(khoaId, khoaHoc, shortKhoa, hocKy, namHoc);

        java.util.Map<Long, MonHocMo> map = new java.util.LinkedHashMap<>();
        for (MonHocMo m : classMoList) {
            map.put(m.getMonHoc().getId(), m);
        }
        for (MonHocMo cm : cohortMoList) {
            if (!map.containsKey(cm.getMonHoc().getId())) {
                // Tự động tạo bản ghi riêng cho lớp này để phân công Giảng viên
                MonHocMo classMo = new MonHocMo(
                        cm.getMonHoc(), cm.getKhoa(), cm.getKhoaHoc(), hocKy, namHoc, lop, cm.getGhiChu());
                classMo = monHocMoRepository.save(classMo);
                map.put(classMo.getMonHoc().getId(), classMo);
            }
        }

        return map.values().stream().map(this::toResponse).toList();
    }

    @Transactional
    public MonHocMoResponse create(MonHocMoRequest request) {
        String rawKhoaHoc = request.khoaHoc() != null ? request.khoaHoc().trim() : "";
        String cleanKhoaHoc = rawKhoaHoc.replaceAll("\\D+", "");
        String savedKhoaHoc = cleanKhoaHoc.isEmpty() ? rawKhoaHoc.toUpperCase() : cleanKhoaHoc;

        MonHoc monHoc = monHocRepository.findById(request.monHocId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy môn học với ID: " + request.monHocId()));

        Khoa khoa = khoaRepository.findById(request.khoaId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy khoa với ID: " + request.khoaId()));

        GiangVien gv = null;
        if (request.giangVienId() != null) {
            gv = giangVienRepository.findById(request.giangVienId())
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy giảng viên với ID: " + request.giangVienId()));
        }

        // Trường hợp 1: Chỉ định mở riêng cho một lớp hành chính cụ thể
        if (request.lopId() != null) {
            if (monHocMoRepository.existsByMonHoc_IdAndLop_IdAndHocKyAndNamHoc(
                    request.monHocId(), request.lopId(), request.hocKy(), request.namHoc())) {
                Lop existingLop = lopRepository.findById(request.lopId()).orElse(null);
                String lopTen = existingLop != null ? existingLop.getTenLop() : ("ID " + request.lopId());
                throw new IllegalStateException("Môn học này đã được mở cho lớp " + lopTen
                        + " trong Học kỳ " + request.hocKy() + " (" + request.namHoc() + ") rồi.");
            }
            Lop lop = lopRepository.findById(request.lopId()).orElse(null);
            MonHocMo monHocMo = new MonHocMo(
                    monHoc, khoa, savedKhoaHoc, request.hocKy(), request.namHoc(), lop, request.ghiChu());
            if (gv != null) {
                monHocMo.setGiangVien(gv);
            }
            return toResponse(monHocMoRepository.save(monHocMo));
        }

        // Trường hợp 2: Mở theo Khoa & Khóa (Tất cả các lớp trong Khoa & Khóa đều có môn này)
        // Luôn lưu bản ghi master của Khóa (lop = null)
        MonHocMo cohortMo = null;
        if (!monHocMoRepository.existsByMonHoc_IdAndKhoa_IdAndKhoaHocAndHocKyAndNamHocAndLopIsNull(
                request.monHocId(), request.khoaId(), savedKhoaHoc, request.hocKy(), request.namHoc())) {
            cohortMo = new MonHocMo(
                    monHoc, khoa, savedKhoaHoc, request.hocKy(), request.namHoc(), null, request.ghiChu());
            cohortMo = monHocMoRepository.save(cohortMo);
        }

        // Tự động mở môn cho từng lớp hiện có thuộc Khoa và Khóa đó để sẵn sàng phân công Giảng viên
        List<Lop> allKhoaLops = lopRepository.findByKhoa_Id(request.khoaId());
        List<Lop> matchingLops = allKhoaLops.stream().filter(l -> {
            if (savedKhoaHoc.isBlank()) return true;
            String lKhoa = extractKhoaHoc(l);
            if (lKhoa.equals(savedKhoaHoc)) return true;
            boolean matchMa = l.getMaLop() != null && (l.getMaLop().contains(savedKhoaHoc)
                    || (savedKhoaHoc.length() >= 4 && l.getMaLop().contains(savedKhoaHoc.substring(2))));
            boolean matchNienKhoa = l.getNienKhoa() != null && (l.getNienKhoa().contains(savedKhoaHoc)
                    || (savedKhoaHoc.length() >= 4 && l.getNienKhoa().contains(savedKhoaHoc.substring(2))));
            return matchMa || matchNienKhoa;
        }).toList();

        MonHocMo firstClassMo = null;
        for (Lop l : matchingLops) {
            if (!monHocMoRepository.existsByMonHoc_IdAndLop_IdAndHocKyAndNamHoc(
                    request.monHocId(), l.getId(), request.hocKy(), request.namHoc())) {
                MonHocMo mhm = new MonHocMo(
                        monHoc, khoa, savedKhoaHoc, request.hocKy(), request.namHoc(), l, request.ghiChu());
                if (gv != null) {
                    mhm.setGiangVien(gv);
                }
                MonHocMo saved = monHocMoRepository.save(mhm);
                if (firstClassMo == null) {
                    firstClassMo = saved;
                }
            }
        }

        if (cohortMo == null && firstClassMo == null) {
            throw new IllegalStateException("Môn học này đã được mở cho Khóa "
                    + request.khoaHoc() + " trong Học kỳ " + request.hocKy() + " (" + request.namHoc() + ") rồi.");
        }
        return toResponse(cohortMo != null ? cohortMo : firstClassMo);
    }

    @Transactional
    public MonHocMoResponse ganGiangVien(Long id, Long giangVienId) {
        MonHocMo monHocMo = monHocMoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy môn học mở với ID: " + id));
        GiangVien gv = null;
        if (giangVienId != null) {
            gv = giangVienRepository.findById(giangVienId)
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy giảng viên với ID: " + giangVienId));
            monHocMo.setGiangVien(gv);
        } else {
            monHocMo.setGiangVien(null);
        }
        MonHocMo saved = monHocMoRepository.save(monHocMo);

        // Nếu gán cho toàn khóa (lop == null), tự động đồng bộ cho từng lớp cụ thể
        if (monHocMo.getLop() == null) {
            List<Lop> allKhoaLops = lopRepository.findByKhoa_Id(monHocMo.getKhoa().getId());
            String savedKhoaHoc = monHocMo.getKhoaHoc() != null ? monHocMo.getKhoaHoc().trim() : "";
            for (Lop l : allKhoaLops) {
                String lKhoa = extractKhoaHoc(l);
                if (savedKhoaHoc.isBlank() || lKhoa.equals(savedKhoaHoc)
                        || (l.getMaLop() != null && l.getMaLop().contains(savedKhoaHoc))) {
                    var classMoOpt = monHocMoRepository.findFirstByMonHoc_IdAndLop_IdAndHocKyAndNamHoc(
                            monHocMo.getMonHoc().getId(), l.getId(), monHocMo.getHocKy(), monHocMo.getNamHoc());
                    if (classMoOpt.isPresent()) {
                        MonHocMo classMo = classMoOpt.get();
                        classMo.setGiangVien(gv);
                        monHocMoRepository.save(classMo);
                    } else {
                        MonHocMo newClassMo = new MonHocMo(
                                monHocMo.getMonHoc(), monHocMo.getKhoa(), savedKhoaHoc,
                                monHocMo.getHocKy(), monHocMo.getNamHoc(), l, monHocMo.getGhiChu());
                        newClassMo.setGiangVien(gv);
                        monHocMoRepository.save(newClassMo);
                    }
                }
            }
        }

        return toResponse(saved);
    }

    @Transactional
    public List<MonHocMoResponse> findByGiangVien(String maGiangVien, String hocKy, String namHoc) {
        List<MonHocMo> list;
        if (hocKy != null && !hocKy.isBlank() && namHoc != null && !namHoc.isBlank()) {
            list = monHocMoRepository.findByGiangVien_MaGiangVienAndHocKyAndNamHoc(maGiangVien, hocKy, namHoc);
        } else {
            list = monHocMoRepository.findByGiangVien_MaGiangVien(maGiangVien);
        }

        // Đảm bảo giảng viên luôn nhận danh sách lớp môn học theo từng LỚP HÀNH CHÍNH cụ thể (lop != null)
        Map<String, MonHocMo> uniqueClassMap = new LinkedHashMap<>();

        for (MonHocMo m : list) {
            if (m.getLop() != null) {
                String key = m.getMonHoc().getId() + "_" + m.getLop().getId() + "_" + m.getHocKy() + "_" + m.getNamHoc();
                uniqueClassMap.put(key, m);
            } else {
                // Bản ghi lop == null (Toàn khóa): mở rộng ra các lớp con cụ thể
                List<Lop> allKhoaLops = lopRepository.findByKhoa_Id(m.getKhoa().getId());
                String savedKhoaHoc = m.getKhoaHoc() != null ? m.getKhoaHoc().trim() : "";
                for (Lop l : allKhoaLops) {
                    String lKhoa = extractKhoaHoc(l);
                    if (savedKhoaHoc.isBlank() || lKhoa.equals(savedKhoaHoc)
                            || (l.getMaLop() != null && l.getMaLop().contains(savedKhoaHoc))) {
                        String key = m.getMonHoc().getId() + "_" + l.getId() + "_" + m.getHocKy() + "_" + m.getNamHoc();
                        if (!uniqueClassMap.containsKey(key)) {
                            var existingOpt = monHocMoRepository.findFirstByMonHoc_IdAndLop_IdAndHocKyAndNamHoc(
                                    m.getMonHoc().getId(), l.getId(), m.getHocKy(), m.getNamHoc());
                            if (existingOpt.isPresent()) {
                                MonHocMo ex = existingOpt.get();
                                if (ex.getGiangVien() == null) {
                                    ex.setGiangVien(m.getGiangVien());
                                    ex = monHocMoRepository.save(ex);
                                }
                                if (ex.getGiangVien() != null && ex.getGiangVien().getMaGiangVien().equals(maGiangVien)) {
                                    uniqueClassMap.put(key, ex);
                                }
                            } else {
                                MonHocMo newClassMo = new MonHocMo(
                                        m.getMonHoc(), m.getKhoa(), savedKhoaHoc,
                                        m.getHocKy(), m.getNamHoc(), l, m.getGhiChu());
                                newClassMo.setGiangVien(m.getGiangVien());
                                newClassMo = monHocMoRepository.save(newClassMo);
                                uniqueClassMap.put(key, newClassMo);
                            }
                        }
                    }
                }
            }
        }

        return uniqueClassMap.values().stream()
                .sorted(Comparator.comparing((MonHocMo a) -> a.getMonHoc() != null ? a.getMonHoc().getTenMonHoc() : "")
                        .thenComparing(a -> a.getLop() != null ? a.getLop().getTenLop() : ""))
                .map(this::toResponse).toList();
    }

    @Transactional
    public void delete(Long id) {
        MonHocMo m = monHocMoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy môn học mở với ID: " + id));
        if (m.getLop() == null) {
            List<MonHocMo> related = monHocMoRepository.findByFilters(
                    m.getHocKy(), m.getNamHoc(), m.getKhoa().getId(), m.getKhoaHoc(), null);
            for (MonHocMo rel : related) {
                if (rel.getMonHoc().getId().equals(m.getMonHoc().getId())) {
                    monHocMoRepository.delete(rel);
                }
            }
        } else {
            monHocMoRepository.delete(m);
        }
    }

    private static final Pattern MSSV_KHOA_PATTERN = Pattern.compile("^[A-Za-z]*(\\d{2})");

    /**
     * Trích xuất năm khóa từ Mã Sinh Viên (ví dụ: 2023CNTT001 -> "2023", B22DCCN001 -> "2022")
     */
    public static String extractKhoaHocFromMssv(String mssv) {
        if (mssv == null || mssv.isBlank()) return "";
        Matcher m4 = Pattern.compile("^(\\d{4})").matcher(mssv.trim());
        if (m4.find()) {
            return m4.group(1);
        }
        Matcher m2 = Pattern.compile("^[A-Za-z]*(\\d{2})").matcher(mssv.trim());
        if (m2.find()) {
            return "20" + m2.group(1);
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

        // Nhận diện khóa theo năm nhập học: ví dụ 2023 -> Khóa 2023
        String khoaHoc = extractKhoaHoc(sv);
        String shortKhoa = khoaHoc.length() == 4 ? khoaHoc.substring(2) : khoaHoc;

        List<MonHocMo> list = monHocMoRepository.findForStudent(hocKy, namHoc, khoaId, khoaHoc, shortKhoa, lop.getId());

        // Nếu chưa cấu hình riêng cho lớp này, tìm các môn mở chung cho toàn Khóa của Khoa trong kỳ
        if (list.isEmpty()) {
            list = monHocMoRepository.findByFilters(hocKy, namHoc, khoaId, khoaHoc, null);
            if (list.isEmpty() && !shortKhoa.equals(khoaHoc)) {
                list = monHocMoRepository.findByFilters(hocKy, namHoc, khoaId, shortKhoa, null);
            }
        }

        // Lọc môn duy nhất cho sinh viên, ưu tiên bản ghi có gán lớp của sinh viên (chứa giảng viên phụ trách của lớp)
        java.util.Map<Long, MonHocMo> studentMoMap = new java.util.LinkedHashMap<>();
        for (MonHocMo m : list) {
            if (m.getLop() != null && m.getLop().getId().equals(lop.getId())) {
                studentMoMap.put(m.getMonHoc().getId(), m);
            }
        }
        for (MonHocMo m : list) {
            if (!studentMoMap.containsKey(m.getMonHoc().getId())) {
                studentMoMap.put(m.getMonHoc().getId(), m);
            }
        }

        return studentMoMap.values().stream().map(this::toResponse).toList();
    }

    public String extractKhoaHoc(SinhVien sv) {
        if (sv == null) return "2023";
        if (sv.getNgayNhapHoc() != null) {
            return String.valueOf(sv.getNgayNhapHoc().getYear());
        }
        String fromMssv = extractKhoaHocFromMssv(sv.getMssv());
        if (!fromMssv.isEmpty()) {
            return fromMssv;
        }
        return extractKhoaHoc(sv.getLop());
    }

    public String extractKhoaHoc(Lop lop) {
        if (lop == null) return "2023";
        if (lop.getNienKhoa() != null) {
            Matcher m = Pattern.compile("(\\d{4})").matcher(lop.getNienKhoa().trim());
            if (m.find()) {
                return m.group(1);
            }
        }
        if (lop.getMaLop() != null) {
            Matcher m = Pattern.compile("(\\d{4})").matcher(lop.getMaLop().trim());
            if (m.find()) {
                return m.group(1);
            }
        }
        return "2023";
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

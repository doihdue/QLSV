package com.demo.be.service;

import com.demo.be.dto.message.NotificationEventMessage;
import com.demo.be.model.ThongBao;
import com.demo.be.repository.ThongBaoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ThongBaoService {

    private final ThongBaoRepository thongBaoRepository;

    public List<ThongBao> getMyNotifications(String username) {
        return thongBaoRepository.findByRecipientUsernameOrderByThoiGianTaoDesc(username);
    }

    public long getUnreadCount(String username) {
        return thongBaoRepository.countByRecipientUsernameAndDaDocFalse(username);
    }

    @Transactional
    public void markAsRead(Long id, String username) {
        thongBaoRepository.findById(id).ifPresent(tb -> {
            if (tb.getRecipientUsername().equalsIgnoreCase(username)) {
                tb.setDaDoc(true);
                thongBaoRepository.save(tb);
            }
        });
    }

    @Transactional
    public void markAllAsRead(String username) {
        thongBaoRepository.markAllAsReadByUsername(username);
    }

    @Transactional
    public void createNotification(String recipient, String title, String content, String type, String link) {
        ThongBao tb = ThongBao.builder()
                .recipientUsername(recipient)
                .tieuDe(title)
                .noiDung(content)
                .loaiThongBao(type)
                .thoiGianTao(LocalDateTime.now())
                .daDoc(false)
                .lienKet(link)
                .build();
        thongBaoRepository.save(tb);
    }

    @Transactional
    public void processNotificationEvent(NotificationEventMessage event) {
        if (event == null || event.getEventType() == null) return;

        String lopText = (event.getMaLop() != null && !event.getMaLop().isBlank()) ? ("Lớp " + event.getMaLop()) : "Toàn khóa";

        switch (event.getEventType()) {
            case "GRADE_APPROVED":
                // Gửi thông báo cho từng sinh viên trong danh sách
                if (event.getStudentMssvList() != null) {
                    for (String mssv : event.getStudentMssvList()) {
                        createNotification(
                                mssv,
                                "📢 Điểm mới môn " + event.getTenMonHoc(),
                                "Bảng điểm học phần " + event.getTenMonHoc() + " (" + lopText + ") đã được công bố chính thức. Bạn hãy vào tra cứu kết quả!",
                                "DIEM_MOI",
                                "/tra-cuu"
                        );
                    }
                    log.info("Đã tạo {} thông báo điểm mới cho sinh viên.", event.getStudentMssvList().size());
                }

                // Gửi thông báo cho giảng viên bộ môn
                if (event.getMaGiangVien() != null && !event.getMaGiangVien().isBlank()) {
                    createNotification(
                            event.getMaGiangVien(),
                            "✅ Bảng điểm đã được phê duyệt",
                            "Bảng điểm học phần " + event.getTenMonHoc() + " (" + lopText + ") do bạn phụ trách đã được Admin phê duyệt và công bố cho sinh viên.",
                            "DUYET_DIEM",
                            "/giang-vien/lop-mon-hoc"
                    );
                }
                break;

            case "GRADE_REJECTED":
                if (event.getMaGiangVien() != null && !event.getMaGiangVien().isBlank()) {
                    createNotification(
                            event.getMaGiangVien(),
                            "⚠️ Bảng điểm bị từ chối phê duyệt",
                            "Bảng điểm môn " + event.getTenMonHoc() + " (" + lopText + ") đã bị Admin từ chối với lý do: \"" +
                                    (event.getReason() != null ? event.getReason() : "Cần rà soát lại") +
                                    "\". Vui lòng kiểm tra và gửi duyệt lại.",
                            "TU_CHOI_DIEM",
                            "/giang-vien/lop-mon-hoc"
                    );
                }
                break;

            case "GRADE_SUBMITTED":
                createNotification(
                        "admin",
                        "📝 Bảng điểm mới chờ duyệt",
                        "Giảng viên " + (event.getTenGiangVien() != null ? event.getTenGiangVien() : event.getMaGiangVien()) +
                                " vừa gửi bảng điểm môn " + event.getTenMonHoc() + " (" + lopText + ") chờ phê duyệt.",
                        "CHO_DUYET",
                        "/admin/diem-dangky"
                );
                break;

            default:
                log.warn("Sự kiện chưa được xử lý: {}", event.getEventType());
                break;
        }
    }
}

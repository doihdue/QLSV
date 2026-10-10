import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { ChangeDetectorRef, Component, inject, OnInit } from '@angular/core';
import { FormBuilder, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { ConfirmDialogService } from '../../../services/confirm-dialog.service';
import { BangDiemRenLuyen, DotRenLuyen, DotRenLuyenPayload, DrlService, ThongKeDrl } from '../../../services/drl.service';
import { ReportJobResponse, ReportService } from '../../../services/report.service';
import { ToastService } from '../../../services/toast.service';

export type LopSimple = {
  id: number;
  maLop: string;
  tenLop: string;
};

@Component({
  selector: 'app-admin-diem-ren-luyen',
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule],
  templateUrl: './admin-diem-ren-luyen.component.html',
  styleUrl: './admin-diem-ren-luyen.component.scss',
})
export class AdminDiemRenLuyenComponent implements OnInit {
  private readonly drlService = inject(DrlService);
  private readonly reportService = inject(ReportService);
  private readonly http = inject(HttpClient);
  private readonly fb = inject(FormBuilder);
  private readonly cd = inject(ChangeDetectorRef);
  private readonly toast = inject(ToastService);
  private readonly confirm = inject(ConfirmDialogService);

  // Tabs: 'evaluations' | 'periods'
  protected activeTab: 'evaluations' | 'periods' = 'evaluations';

  // Export State
  protected exporting = false;
  protected showHistoryModal = false;
  protected reportHistory: ReportJobResponse[] = [];
  protected loadingHistory = false;

  // Filters & Data
  protected dots: DotRenLuyen[] = [];
  protected lops: LopSimple[] = [];
  protected selectedDotId: number | null = null;
  protected selectedLopId: number | null = null;
  protected selectedTrangThai = 'ALL';

  protected sheets: BangDiemRenLuyen[] = [];
  protected thongKe: ThongKeDrl | null = null;
  protected loading = false;
  protected loadingStats = false;

  // Selected sheet for Approval modal
  protected selectedSheet: BangDiemRenLuyen | null = null;
  protected showApproveModal = false;
  protected adminDiemDieuChinh: number | null = null;
  protected adminNhanXet = '';
  protected approving = false;

  // Dot Modal
  protected showDotModal = false;
  protected editingDotId: number | null = null;
  protected savingDot = false;

  protected dotForm = this.fb.group({
    tenDot: ['', [Validators.required, Validators.maxLength(150)]],
    hocKy: ['1', [Validators.required]],
    namHoc: ['2026-2027', [Validators.required]],
    ngayBatDau: ['', [Validators.required]],
    ngayKetThuc: ['', [Validators.required]],
    dangMo: [true],
    ghiChu: [''],
  });

  ngOnInit(): void {
    this.loadDots();
    this.loadLops();
  }

  protected loadDots(): void {
    this.drlService.getAllDots().subscribe({
      next: (res) => {
        this.dots = res;
        if (!this.selectedDotId && res.length > 0) {
          const current = res.find((d) => d.dangMo) || res[0];
          this.selectedDotId = current.id;
        }
        this.loadSheetsAndStats();
        this.cd.detectChanges();
      },
      error: () => {
        this.toast.error('Lỗi', 'Không thể tải danh sách đợt rèn luyện.');
      },
    });
  }

  protected loadLops(): void {
    this.http.get<LopSimple[]>('http://localhost:8080/api/lop').subscribe({
      next: (res) => {
        this.lops = res;
        this.cd.detectChanges();
      },
      error: () => {},
    });
  }

  protected onDotChange(): void {
    this.loadSheetsAndStats();
  }

  protected onLopChange(): void {
    this.loadSheetsAndStats();
  }

  protected onTrangThaiChange(): void {
    this.loadSheets();
  }

  protected loadSheetsAndStats(): void {
    this.loadSheets();
    this.loadThongKe();
  }

  protected loadSheets(): void {
    if (!this.selectedDotId) return;
    this.loading = true;
    this.drlService.getScoresForAdmin(this.selectedDotId, this.selectedLopId || undefined, this.selectedTrangThai).subscribe({
      next: (res) => {
        this.sheets = res;
        this.loading = false;
        this.cd.detectChanges();
      },
      error: () => {
        this.loading = false;
        this.toast.error('Lỗi', 'Không thể tải danh sách bảng điểm rèn luyện.');
        this.cd.detectChanges();
      },
    });
  }

  protected loadThongKe(): void {
    if (!this.selectedDotId) return;
    this.loadingStats = true;
    this.drlService.getThongKe(this.selectedDotId, this.selectedLopId || undefined).subscribe({
      next: (res) => {
        this.thongKe = res;
        this.loadingStats = false;
        this.cd.detectChanges();
      },
      error: () => {
        this.loadingStats = false;
      },
    });
  }

  // === Modal Phê Duyệt ===
  protected openApproveModal(sheet: BangDiemRenLuyen): void {
    this.selectedSheet = sheet;
    this.adminDiemDieuChinh = sheet.diemTongKet !== null ? sheet.diemTongKet : (sheet.tongDiemGv !== null ? sheet.tongDiemGv : sheet.tongDiemSv);
    this.adminNhanXet = sheet.nhanXetAdmin || '';
    this.showApproveModal = true;
    this.cd.detectChanges();
  }

  protected closeApproveModal(): void {
    this.showApproveModal = false;
    this.selectedSheet = null;
  }

  protected submitApproval(action: 'APPROVE' | 'REJECT'): void {
    if (!this.selectedSheet) return;

    if (action === 'REJECT' && !this.adminNhanXet.trim()) {
      this.toast.warning('Yêu cầu', 'Vui lòng nhập lý do từ chối / yêu cầu đánh giá lại vào ô nhận xét.');
      return;
    }

    this.approving = true;
    this.drlService
      .adminApproveOrReject({
        bangDiemId: this.selectedSheet.id,
        action,
        nhanXetAdmin: this.adminNhanXet.trim() || undefined,
        diemDieuChinh: action === 'APPROVE' ? (this.adminDiemDieuChinh !== null ? Number(this.adminDiemDieuChinh) : undefined) : undefined,
      })
      .subscribe({
        next: (updated) => {
          this.approving = false;
          this.toast.success('Thành công', action === 'APPROVE' ? 'Đã phê duyệt điểm rèn luyện.' : 'Đã từ chối và gửi phản hồi cho sinh viên/giảng viên.');
          this.closeApproveModal();
          this.loadSheetsAndStats();
        },
        error: (err) => {
          this.approving = false;
          const msg = err.error?.message || 'Có lỗi xảy ra khi xét duyệt điểm.';
          this.toast.error('Lỗi', msg);
        },
      });
  }

  // === Batch Approve Whole Class ===
  protected approveWholeClass(): void {
    if (!this.selectedLopId) {
      this.toast.warning('Cảnh báo', 'Vui lòng chọn cụ thể một Lớp trước khi bấm duyệt cả lớp.');
      return;
    }
    const lopName = this.lops.find((l) => l.id === this.selectedLopId)?.tenLop || 'lớp này';

    this.confirm.confirm({
      title: 'Xác nhận duyệt toàn bộ lớp',
      message: `Bạn có chắc chắn muốn phê duyệt toàn bộ các phiếu điểm đang chờ duyệt của "${lopName}"? Hệ thống sẽ lấy điểm của Giảng viên làm điểm chính thức và tự động xếp loại.`,
      confirmText: 'Phê duyệt toàn bộ',
      cancelText: 'Hủy bỏ',
    }).then((confirmed) => {
      if (!confirmed) return;
      this.loading = true;
      this.drlService.adminApproveWholeClass(this.selectedLopId!, this.selectedDotId || undefined).subscribe({
        next: (res) => {
          this.loading = false;
          this.toast.success('Thành công', res.message);
          this.loadSheetsAndStats();
        },
        error: (err) => {
          this.loading = false;
          this.toast.error('Lỗi', err.error?.message || 'Không thể phê duyệt cả lớp.');
        },
      });
    });
  }

  // === Đợt Rèn Luyện Management ===
  protected openNewDotModal(): void {
    this.editingDotId = null;
    this.dotForm.reset({
      tenDot: 'Đợt đánh giá rèn luyện Học kỳ 1 (2026-2027)',
      hocKy: '1',
      namHoc: '2026-2027',
      ngayBatDau: new Date().toISOString().substring(0, 10),
      ngayKetThuc: new Date(Date.now() + 30 * 86400000).toISOString().substring(0, 10),
      dangMo: true,
      ghiChu: 'Sinh viên hoàn thành phiếu tự đánh giá đúng hạn.',
    });
    this.showDotModal = true;
  }

  protected openEditDotModal(dot: DotRenLuyen): void {
    this.editingDotId = dot.id;
    this.dotForm.patchValue({
      tenDot: dot.tenDot,
      hocKy: dot.hocKy,
      namHoc: dot.namHoc,
      ngayBatDau: dot.ngayBatDau,
      ngayKetThuc: dot.ngayKetThuc,
      dangMo: dot.dangMo,
      ghiChu: dot.ghiChu || '',
    });
    this.showDotModal = true;
  }

  protected closeDotModal(): void {
    this.showDotModal = false;
    this.editingDotId = null;
  }

  protected saveDot(): void {
    if (this.dotForm.invalid) {
      this.dotForm.markAllAsTouched();
      this.toast.warning('Dữ liệu không hợp lệ', 'Vui lòng kiểm tra lại thông tin đợt rèn luyện.');
      return;
    }

    const val = this.dotForm.value;
    if (val.ngayBatDau && val.ngayKetThuc && val.ngayBatDau >= val.ngayKetThuc) {
      this.toast.warning('Lỗi thời gian', 'Thời gian trước phải nhỏ hơn thời gian sau (Ngày bắt đầu phải trước ngày kết thúc).');
      return;
    }

    const payload: DotRenLuyenPayload = {
      tenDot: val.tenDot!,
      hocKy: val.hocKy!,
      namHoc: val.namHoc!,
      ngayBatDau: val.ngayBatDau!,
      ngayKetThuc: val.ngayKetThuc!,
      dangMo: !!val.dangMo,
      ghiChu: val.ghiChu || undefined,
    };

    this.savingDot = true;
    const req$ = this.editingDotId
      ? this.drlService.updateDot(this.editingDotId, payload)
      : this.drlService.createDot(payload);

    req$.subscribe({
      next: () => {
        this.savingDot = false;
        this.toast.success('Thành công', this.editingDotId ? 'Cập nhật đợt rèn luyện thành công.' : 'Tạo mới đợt rèn luyện thành công.');
        this.closeDotModal();
        this.loadDots();
      },
      error: (err) => {
        this.savingDot = false;
        this.toast.error('Lỗi', err.error?.message || 'Có lỗi khi lưu đợt rèn luyện.');
      },
    });
  }

  protected toggleDotStatus(dot: DotRenLuyen): void {
    const nextState = !dot.dangMo;
    this.drlService.toggleDot(dot.id, nextState).subscribe({
      next: (res) => {
        dot.dangMo = res.dangMo;
        this.toast.success('Thành công', `${nextState ? 'Đã mở' : 'Đã đóng'} đợt rèn luyện: ${dot.tenDot}`);
        this.cd.detectChanges();
      },
      error: () => {
        this.toast.error('Lỗi', 'Không thể thay đổi trạng thái đợt rèn luyện.');
      },
    });
  }

  // === Asynchronous JasperReports + RabbitMQ Export ===
  protected exportPdfReport(): void {
    if (!this.selectedLopId) {
      this.toast.warning('Yêu cầu', 'Vui lòng chọn cụ thể một Lớp hành chính trước khi xuất báo cáo PDF.');
      return;
    }
    if (!this.selectedDotId) return;

    this.exporting = true;
    this.cd.markForCheck();
    this.cd.detectChanges();

    this.reportService.requestExportDrl(this.selectedDotId, this.selectedLopId).subscribe({
      next: (res) => {
        this.pollExportJob(res.jobId, 0);
      },
      error: (err) => {
        this.exporting = false;
        this.toast.error('Lỗi', err.error?.message || 'Không thể gửi yêu cầu xuất báo cáo.');
      },
    });
  }

  private pollTimeout: any = null;

  private pollExportJob(jobId: string, attempt: number): void {
    if (this.pollTimeout) {
      clearTimeout(this.pollTimeout);
      this.pollTimeout = null;
    }

    if (attempt > 30) {
      this.exporting = false;
      this.toast.warning('Thông báo', 'Quá trình xuất báo cáo đang tiếp tục xử lý ngầm. Bạn có thể xem và tải về trong mục Lịch sử báo cáo.');
      this.cd.markForCheck();
      this.cd.detectChanges();
      return;
    }

    this.pollTimeout = setTimeout(() => {
      this.reportService.getStatus(jobId).subscribe({
        next: (job) => {
          if (job.status === 'COMPLETED') {
            this.toast.success('Thành công', 'Báo cáo PDF đã được tạo hoàn tất! Đang tự động tải về máy...');
            this.downloadReportFile(job.jobId, job.fileName || 'BaoCao_DRL.pdf');
          } else if (job.status === 'FAILED') {
            this.exporting = false;
            this.toast.error('Lỗi', job.errorMessage || 'Lỗi khi tạo báo cáo JasperReports.');
            this.cd.markForCheck();
            this.cd.detectChanges();
          } else {
            this.pollExportJob(jobId, attempt + 1);
          }
        },
        error: () => {
          this.exporting = false;
          this.cd.markForCheck();
          this.cd.detectChanges();
        },
      });
    }, 1200);
  }

  protected downloadReportFile(jobId: string, fileName: string): void {
    this.reportService.downloadBlob(jobId).subscribe({
      next: (blob) => {
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = fileName;
        document.body.appendChild(a);
        a.click();
        document.body.removeChild(a);
        setTimeout(() => window.URL.revokeObjectURL(url), 1000);

        this.exporting = false;
        this.cd.markForCheck();
        this.cd.detectChanges();
      },
      error: () => {
        this.exporting = false;
        this.toast.error('Lỗi', 'Không thể tải file báo cáo.');
        this.cd.markForCheck();
        this.cd.detectChanges();
      },
    });
  }

  protected openHistoryModal(): void {
    this.showHistoryModal = true;
    this.loadingHistory = true;
    this.reportService.getMyHistory().subscribe({
      next: (list) => {
        this.reportHistory = list;
        this.loadingHistory = false;
        this.cd.detectChanges();
      },
      error: () => {
        this.loadingHistory = false;
      },
    });
  }

  protected closeHistoryModal(): void {
    this.showHistoryModal = false;
  }

  // === Helpers ===
  protected getStatusClass(status: string): string {
    switch (status) {
      case 'DA_DUYET':
        return 'badge-success';
      case 'CHO_ADMIN_DUYET':
        return 'badge-warning';
      case 'CHO_GIANG_VIEN_DUYET':
        return 'badge-info';
      case 'TU_CHOI':
        return 'badge-danger';
      case 'LUU_NHAP_GIANG_VIEN':
      case 'LUU_NHAP_SINH_VIEN':
        return 'badge-purple';
      default:
        return 'badge-secondary';
    }
  }

  protected getXepLoaiClass(xepLoai?: string): string {
    if (!xepLoai) return '';
    switch (xepLoai) {
      case 'Xuất sắc':
        return 'rank-xuat-sac';
      case 'Tốt':
        return 'rank-tot';
      case 'Khá':
        return 'rank-kha';
      case 'Trung bình':
        return 'rank-tb';
      case 'Yếu':
      case 'Kém':
        return 'rank-yeu';
      default:
        return '';
    }
  }
}

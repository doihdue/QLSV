import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { ChangeDetectorRef, Component, inject, OnInit } from '@angular/core';
import { FormBuilder, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { ConfirmDialogService } from '../../../services/confirm-dialog.service';
import { BangDiemRenLuyen, DotRenLuyen, DrlService, LecturerDanhGiaRequest } from '../../../services/drl.service';
import { ReportService } from '../../../services/report.service';
import { ToastService } from '../../../services/toast.service';

export type LopItem = {
  id: number;
  maLop: string;
  tenLop: string;
};

@Component({
  selector: 'app-lecturer-diem-ren-luyen',
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule],
  templateUrl: './lecturer-diem-ren-luyen.component.html',
  styleUrl: './lecturer-diem-ren-luyen.component.scss',
})
export class LecturerDiemRenLuyenComponent implements OnInit {
  private readonly drlService = inject(DrlService);
  private readonly reportService = inject(ReportService);
  private readonly http = inject(HttpClient);
  private readonly fb = inject(FormBuilder);
  private readonly cd = inject(ChangeDetectorRef);
  private readonly toast = inject(ToastService);
  private readonly confirm = inject(ConfirmDialogService);

  protected dots: DotRenLuyen[] = [];
  protected lops: LopItem[] = [];
  protected selectedDotId: number | null = null;
  protected selectedLopId: number | null = null;

  protected sheets: BangDiemRenLuyen[] = [];
  protected loading = false;
  protected submittingClass = false;
  protected exporting = false;

  // Evaluation Modal
  protected showModal = false;
  protected currentSheet: BangDiemRenLuyen | null = null;
  protected saving = false;

  protected evalForm = this.fb.group({
    diemGvMuc1: [0, [Validators.required, Validators.min(0), Validators.max(20)]],
    diemGvMuc2: [0, [Validators.required, Validators.min(0), Validators.max(25)]],
    diemGvMuc3: [0, [Validators.required, Validators.min(0), Validators.max(20)]],
    diemGvMuc4: [0, [Validators.required, Validators.min(0), Validators.max(25)]],
    diemGvMuc5: [0, [Validators.required, Validators.min(0), Validators.max(10)]],
    nhanXetGv: [''],
  });

  ngOnInit(): void {
    this.loadDots();
    this.loadLops();
  }

  protected loadDots(): void {
    this.drlService.getAllDots().subscribe({
      next: (dots) => {
        this.dots = dots;
        if (dots.length > 0) {
          const open = dots.find((d) => d.dangMo) || dots[0];
          this.selectedDotId = open.id;
        }
        this.checkAndLoadScores();
        this.cd.detectChanges();
      },
      error: () => {
        this.toast.error('Lỗi', 'Không thể tải danh sách đợt rèn luyện.');
      },
    });
  }

  protected loadLops(): void {
    this.http.get<LopItem[]>('http://localhost:8080/api/lop').subscribe({
      next: (lops) => {
        this.lops = lops;
        if (lops.length > 0 && !this.selectedLopId) {
          this.selectedLopId = lops[0].id;
        }
        this.checkAndLoadScores();
        this.cd.detectChanges();
      },
      error: () => {},
    });
  }

  protected onDotOrLopChange(): void {
    this.checkAndLoadScores();
  }

  protected checkAndLoadScores(): void {
    if (!this.selectedLopId || !this.selectedDotId) return;
    this.loading = true;
    this.drlService.getScoresForClass(this.selectedLopId, this.selectedDotId).subscribe({
      next: (res) => {
        this.sheets = res;
        this.loading = false;
        this.cd.detectChanges();
      },
      error: () => {
        this.loading = false;
        this.toast.error('Lỗi', 'Không thể tải danh sách bảng điểm của lớp.');
        this.cd.detectChanges();
      },
    });
  }

  // Summary counts
  protected get totalStudents(): number {
    return this.sheets.length;
  }

  protected get svSubmittedCount(): number {
    return this.sheets.filter(
      (s) => s.trangThai !== 'CHUA_DANH_GIA' && s.trangThai !== 'LUU_NHAP_SINH_VIEN'
    ).length;
  }

  protected get gvGradedCount(): number {
    return this.sheets.filter((s) => s.tongDiemGv !== null).length;
  }

  protected get adminApprovedCount(): number {
    return this.sheets.filter((s) => s.trangThai === 'DA_DUYET').length;
  }

  // Modal Open
  protected openEvaluateModal(sheet: BangDiemRenLuyen): void {
    this.currentSheet = sheet;
    // Set default points to existing gv scores, or inherit student points if gv hasn't scored yet
    const m1 = sheet.diemGvMuc1 !== null ? sheet.diemGvMuc1 : (sheet.diemSvMuc1 ?? 0);
    const m2 = sheet.diemGvMuc2 !== null ? sheet.diemGvMuc2 : (sheet.diemSvMuc2 ?? 0);
    const m3 = sheet.diemGvMuc3 !== null ? sheet.diemGvMuc3 : (sheet.diemSvMuc3 ?? 0);
    const m4 = sheet.diemGvMuc4 !== null ? sheet.diemGvMuc4 : (sheet.diemSvMuc4 ?? 0);
    const m5 = sheet.diemGvMuc5 !== null ? sheet.diemGvMuc5 : (sheet.diemSvMuc5 ?? 0);

    this.evalForm.reset({
      diemGvMuc1: m1,
      diemGvMuc2: m2,
      diemGvMuc3: m3,
      diemGvMuc4: m4,
      diemGvMuc5: m5,
      nhanXetGv: sheet.nhanXetGv || '',
    });

    this.showModal = true;
    this.cd.detectChanges();
  }

  protected closeModal(): void {
    this.showModal = false;
    this.currentSheet = null;
  }

  protected copyStudentPoints(): void {
    if (!this.currentSheet) return;
    this.evalForm.patchValue({
      diemGvMuc1: this.currentSheet.diemSvMuc1 ?? 0,
      diemGvMuc2: this.currentSheet.diemSvMuc2 ?? 0,
      diemGvMuc3: this.currentSheet.diemSvMuc3 ?? 0,
      diemGvMuc4: this.currentSheet.diemSvMuc4 ?? 0,
      diemGvMuc5: this.currentSheet.diemSvMuc5 ?? 0,
    });
    this.toast.info('Đã sao chép', 'Đã sao chép toàn bộ điểm sinh viên tự chấm sang cột giảng viên.');
  }

  protected get currentLiveTotal(): number {
    const val = this.evalForm.value;
    return (
      Number(val.diemGvMuc1 || 0) +
      Number(val.diemGvMuc2 || 0) +
      Number(val.diemGvMuc3 || 0) +
      Number(val.diemGvMuc4 || 0) +
      Number(val.diemGvMuc5 || 0)
    );
  }

  protected get currentLiveRank(): string {
    const t = this.currentLiveTotal;
    if (t >= 90) return 'Xuất sắc';
    if (t >= 80) return 'Tốt';
    if (t >= 65) return 'Khá';
    if (t >= 50) return 'Trung bình';
    if (t >= 35) return 'Yếu';
    return 'Kém';
  }

  protected saveEvaluation(action: 'SAVE_DRAFT' | 'SUBMIT_ADMIN'): void {
    if (!this.currentSheet) return;

    if (this.evalForm.invalid) {
      this.evalForm.markAllAsTouched();
      this.toast.warning('Dữ liệu không hợp lệ', 'Vui lòng kiểm tra lại điểm từng tiêu chí (không vượt quá mức tối đa).');
      return;
    }

    const val = this.evalForm.value;
    const req: LecturerDanhGiaRequest = {
      bangDiemId: this.currentSheet.id,
      diemGvMuc1: Number(val.diemGvMuc1),
      diemGvMuc2: Number(val.diemGvMuc2),
      diemGvMuc3: Number(val.diemGvMuc3),
      diemGvMuc4: Number(val.diemGvMuc4),
      diemGvMuc5: Number(val.diemGvMuc5),
      nhanXetGv: val.nhanXetGv ? val.nhanXetGv.trim() : undefined,
      action,
    };

    this.saving = true;
    this.drlService.lecturerEvaluate(req).subscribe({
      next: () => {
        this.saving = false;
        this.toast.success(
          'Thành công',
          action === 'SUBMIT_ADMIN'
            ? 'Đã đánh giá và chuyển phiếu lên Admin xét duyệt.'
            : 'Đã lưu nháp bảng điểm giảng viên.'
        );
        this.closeModal();
        this.checkAndLoadScores();
      },
      error: (err) => {
        this.saving = false;
        this.toast.error('Lỗi', err.error?.message || 'Không thể lưu đánh giá.');
      },
    });
  }

  // Submit Whole Class to Admin
  protected submitWholeClass(): void {
    if (!this.selectedLopId) return;
    const lopName = this.lops.find((l) => l.id === this.selectedLopId)?.tenLop || 'lớp này';

    this.confirm.confirm({
      title: 'Gửi toàn bộ lớp lên Admin duyệt',
      message: `Bạn có chắc chắn muốn gửi kết quả rèn luyện của "${lopName}" lên Ban Đào tạo / Admin phê duyệt? Những phiếu chưa chấm chi tiết sẽ tự động kế thừa điểm sinh viên đã tự chấm.`,
      confirmText: 'Gửi lên Admin',
      cancelText: 'Hủy bỏ',
    }).then((ok) => {
      if (!ok) return;
      this.submittingClass = true;
      this.drlService.lecturerSubmitClass(this.selectedLopId!, this.selectedDotId || undefined).subscribe({
        next: (res) => {
          this.submittingClass = false;
          this.toast.success('Thành công', res.message);
          this.checkAndLoadScores();
        },
        error: (err) => {
          this.submittingClass = false;
          this.toast.error('Lỗi', err.error?.message || 'Không thể chuyển danh sách lớp.');
        },
      });
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
      this.toast.warning('Thông báo', 'Quá trình xuất báo cáo đang tiếp tục xử lý ngầm.');
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
}

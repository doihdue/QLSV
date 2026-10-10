import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, inject, OnInit } from '@angular/core';
import { FormBuilder, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { ConfirmDialogService } from '../../../services/confirm-dialog.service';
import { BangDiemRenLuyen, DotRenLuyen, DrlService, StudentDanhGiaRequest } from '../../../services/drl.service';
import { ToastService } from '../../../services/toast.service';

@Component({
  selector: 'app-student-diem-ren-luyen',
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule],
  templateUrl: './student-diem-ren-luyen.component.html',
  styleUrl: './student-diem-ren-luyen.component.scss',
})
export class StudentDiemRenLuyenComponent implements OnInit {
  private readonly drlService = inject(DrlService);
  private readonly fb = inject(FormBuilder);
  private readonly cd = inject(ChangeDetectorRef);
  private readonly toast = inject(ToastService);
  private readonly confirm = inject(ConfirmDialogService);

  protected dots: DotRenLuyen[] = [];
  protected selectedDotId: number | null = null;
  protected currentDot: DotRenLuyen | null = null;

  protected sheet: BangDiemRenLuyen | null = null;
  protected loading = false;
  protected submitting = false;

  protected form = this.fb.group({
    diemSvMuc1: [20, [Validators.required, Validators.min(0), Validators.max(20)]],
    diemSvMuc2: [25, [Validators.required, Validators.min(0), Validators.max(25)]],
    diemSvMuc3: [18, [Validators.required, Validators.min(0), Validators.max(20)]],
    diemSvMuc4: [25, [Validators.required, Validators.min(0), Validators.max(25)]],
    diemSvMuc5: [8, [Validators.required, Validators.min(0), Validators.max(10)]],
    ghiChuSv: [''],
  });

  ngOnInit(): void {
    this.loadDots();
  }

  protected loadDots(): void {
    this.loading = true;
    this.drlService.getAllDots().subscribe({
      next: (dots) => {
        this.dots = dots;
        if (dots.length > 0) {
          const open = dots.find((d) => d.dangMo) || dots[0];
          this.selectedDotId = open.id;
          this.currentDot = open;
          this.loadScore();
        } else {
          this.loading = false;
        }
        this.cd.detectChanges();
      },
      error: () => {
        this.loading = false;
        this.toast.error('Lỗi', 'Không thể tải danh sách đợt rèn luyện.');
      },
    });
  }

  protected onDotChange(): void {
    this.currentDot = this.dots.find((d) => d.id === this.selectedDotId) || null;
    this.loadScore();
  }

  protected loadScore(): void {
    if (!this.selectedDotId) return;
    this.loading = true;
    this.drlService.getMyScore(this.selectedDotId).subscribe({
      next: (res) => {
        this.sheet = res;
        this.loading = false;

        // Populate form if student already scored
        if (res.tongDiemSv !== null && res.tongDiemSv > 0) {
          this.form.patchValue({
            diemSvMuc1: res.diemSvMuc1 ?? 0,
            diemSvMuc2: res.diemSvMuc2 ?? 0,
            diemSvMuc3: res.diemSvMuc3 ?? 0,
            diemSvMuc4: res.diemSvMuc4 ?? 0,
            diemSvMuc5: res.diemSvMuc5 ?? 0,
            ghiChuSv: res.ghiChuSv || '',
          });
        }
        this.cd.detectChanges();
      },
      error: () => {
        this.loading = false;
        this.toast.error('Lỗi', 'Không thể tải phiếu rèn luyện của bạn.');
        this.cd.detectChanges();
      },
    });
  }

  // Quick button helpers
  protected setMucScore(field: 'diemSvMuc1' | 'diemSvMuc2' | 'diemSvMuc3' | 'diemSvMuc4' | 'diemSvMuc5', score: number): void {
    if (this.isFormReadOnly) return;
    this.form.patchValue({ [field]: score });
  }

  protected get liveTotalScore(): number {
    const val = this.form.value;
    return (
      Number(val.diemSvMuc1 || 0) +
      Number(val.diemSvMuc2 || 0) +
      Number(val.diemSvMuc3 || 0) +
      Number(val.diemSvMuc4 || 0) +
      Number(val.diemSvMuc5 || 0)
    );
  }

  protected get liveRank(): string {
    const s = this.liveTotalScore;
    if (s >= 90) return 'Xuất sắc';
    if (s >= 80) return 'Tốt';
    if (s >= 65) return 'Khá';
    if (s >= 50) return 'Trung bình';
    if (s >= 35) return 'Yếu';
    return 'Kém';
  }

  protected get isFormReadOnly(): boolean {
    if (!this.currentDot || !this.currentDot.dangMo) return true;
    if (!this.sheet) return false;
    // When submitted to Admin or already approved, student cannot edit
    return (
      this.sheet.trangThai === 'CHO_ADMIN_DUYET' ||
      this.sheet.trangThai === 'DA_DUYET'
    );
  }

  protected submitEvaluation(action: 'SAVE_DRAFT' | 'SUBMIT'): void {
    if (this.isFormReadOnly) return;

    if (this.form.invalid) {
      this.form.markAllAsTouched();
      this.toast.warning('Dữ liệu không hợp lệ', 'Vui lòng kiểm tra lại điểm các mục.');
      return;
    }

    const doSubmit = () => {
      const val = this.form.value;
      const req: StudentDanhGiaRequest = {
        dotId: this.selectedDotId!,
        diemSvMuc1: Number(val.diemSvMuc1),
        diemSvMuc2: Number(val.diemSvMuc2),
        diemSvMuc3: Number(val.diemSvMuc3),
        diemSvMuc4: Number(val.diemSvMuc4),
        diemSvMuc5: Number(val.diemSvMuc5),
        ghiChuSv: val.ghiChuSv ? val.ghiChuSv.trim() : undefined,
        action,
      };

      this.submitting = true;
      this.drlService.studentSubmit(req).subscribe({
        next: (saved) => {
          this.submitting = false;
          this.sheet = saved;
          this.toast.success(
            'Thành công',
            action === 'SUBMIT'
              ? 'Đã nộp phiếu tự đánh giá rèn luyện cho Giảng viên xét duyệt!'
              : 'Đã lưu nháp phiếu rèn luyện thành công.'
          );
          this.cd.detectChanges();
        },
        error: (err) => {
          this.submitting = false;
          this.toast.error('Lỗi', err.error?.message || 'Không thể lưu phiếu đánh giá.');
        },
      });
    };

    if (action === 'SUBMIT') {
      this.confirm.confirm({
        title: 'Nộp phiếu đánh giá rèn luyện',
        message: `Bạn đang tự đánh giá tổng cộng ${this.liveTotalScore} điểm (Xếp loại dự kiến: ${this.liveRank}). Bạn có chắc chắn muốn nộp phiếu cho Giảng viên / Cố vấn học tập không?`,
        confirmText: 'Xác nhận nộp',
        cancelText: 'Xem lại',
      }).then((ok) => {
        if (ok) doSubmit();
      });
    } else {
      doSubmit();
    }
  }

  protected getStatusBadgeClass(status?: string): string {
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

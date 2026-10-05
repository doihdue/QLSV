import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { ChangeDetectorRef, Component, inject, OnInit } from '@angular/core';
import { AbstractControl, FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, ValidationErrors, ValidatorFn, Validators } from '@angular/forms';
import { forkJoin, of } from 'rxjs';
import { catchError } from 'rxjs/operators';

import { MonHocMo } from '../../../services/student.service';
import { PaginationComponent } from '../../../components/pagination/pagination.component';
import { ToastService } from '../../../services/toast.service';
import { ConfirmDialogService } from '../../../services/confirm-dialog.service';

export type MonHocOption = {
  id: number;
  maMonHoc: string;
  tenMonHoc: string;
  soTinChi: number;
  khoaId?: number;
  khoaTen?: string;
};

export type KhoaOption = {
  id: number;
  maKhoa: string;
  tenKhoa: string;
};

export type GiangVienOption = {
  id: number;
  maGiangVien: string;
  hoTen: string;
};

export type LopOption = {
  id: number;
  maLop: string;
  tenLop: string;
  nienKhoa: string;
  khoaId?: number;
  khoaTen?: string;
};

export type SinhVienOption = {
  id: number;
  mssv: string;
  hoTen: string;
  lopId?: number;
  lopTen?: string;
  khoaHoc?: string;
};

export type DangKyItem = {
  id: number;
  sinhVienId: number;
  sinhVienMssv: string;
  sinhVienHoTen: string;
  lopId?: number;
  lopMa?: string;
  lopTen?: string;
  monHocId: number;
  monHocMa: string;
  monHocTen: string;
  soTinChi: number;
  hocKy: string;
  namHoc: string;
  ngayDangKy: string;
  trangThai: string;
};

@Component({
  selector: 'app-admin-diem-dangky',
  imports: [CommonModule, FormsModule, ReactiveFormsModule, PaginationComponent],
  templateUrl: './admin-diem-dangky.component.html',
  styleUrl: './admin-diem-dangky.component.scss',
})
export class AdminDiemDangKyComponent implements OnInit {
  private readonly http = inject(HttpClient);
  private readonly fb = inject(FormBuilder);
  private readonly cd = inject(ChangeDetectorRef);
  private readonly toastService = inject(ToastService);
  private readonly confirmDialog = inject(ConfirmDialogService);

  protected activeTab: 'duyet' | 'dangky' | 'monhocmo' = 'duyet';

  // Master options
  protected monHocs: MonHocOption[] = [];
  protected sinhViens: SinhVienOption[] = [];
  protected khoas: KhoaOption[] = [];
  protected lops: LopOption[] = [];
  protected giangViens: GiangVienOption[] = [];
  protected readonly cohortOptions = ['2020', '2021', '2022', '2023', '2024', '2025', '2026'];

  protected readonly semesters = [
    { id: '1', name: 'Học kỳ 1' },
    { id: '2', name: 'Học kỳ 2' },
    { id: '3', name: 'Học kỳ hè' },
  ];
  protected readonly academicYears = ['2026-2027', '2025-2026', '2024-2025'];

  // Tab: Phê duyệt bảng điểm
  protected pendingClasses: any[] = [];
  protected selectedPendingClass: any = null;
  protected pendingClassStudents: any[] = [];
  protected loadingPending = false;
  protected loadingPendingDetails = false;
  protected pendingPage = 1;
  protected pendingPageSize = 15;

  // Tab: Quản lý đăng ký filters
  protected filterRegHocKy: string = '1';
  protected filterRegNamHoc: string = '2026-2027';
  protected filterRegMonHocId: string = '';
  protected searchRegStudent: string = '';
  protected regPage = 1;
  protected regPageSize = 15;

  // Tab: Mở môn theo Khóa & Lớp hành chính filters
  protected moNamHoc: string = '2026-2027';
  protected moHocKy: string = '1';
  protected moKhoaId: string = '';
  protected moKhoaHoc: string = '2023';
  protected moLopId: string = '';
  protected monHocMoList: MonHocMo[] = [];
  protected moPage = 1;
  protected moPageSize = 15;

  // Modal: Mở môn theo Khoa & Khóa
  protected showAddMoModal = false;
  protected readonly addMoForm = this.fb.group({
    monHocId: ['', Validators.required],
    khoaId: ['', Validators.required],
    khoaHoc: ['2023', Validators.required],
    hocKy: ['1', Validators.required],
    namHoc: ['2026-2027', Validators.required],
    ghiChu: [''],
  });

  // Raw data from BE
  protected allRegistrations: DangKyItem[] = [];

  // Modal states: Thêm đăng ký môn cá nhân
  protected showAddRegModal = false;
  protected readonly addRegForm = this.fb.group({
    sinhVienId: ['', Validators.required],
    monHocId: ['', Validators.required],
    hocKy: ['1', Validators.required],
    namHoc: ['2026-2027', Validators.required],
  });

  // Modal states: Ghi danh cả lớp hành chính
  protected showBatchRegModal = false;
  protected readonly batchRegForm = this.fb.group({
    lopId: ['', Validators.required],
    monHocId: ['', Validators.required],
    hocKy: ['1', Validators.required],
    namHoc: ['2026-2027', Validators.required],
  });

  private static dateRangeValidator(startKey: string, endKey: string): ValidatorFn {
    return (control: AbstractControl): ValidationErrors | null => {
      const startCtrl = control.get(startKey);
      const endCtrl = control.get(endKey);
      if (!startCtrl || !endCtrl) return null;

      const start = startCtrl.value;
      const end = endCtrl.value;

      if (start && end && start >= end) {
        endCtrl.setErrors({ ...endCtrl.errors, dateBefore: true });
        return { dateRangeInvalid: true };
      } else {
        if (endCtrl.hasError('dateBefore')) {
          const errors = { ...endCtrl.errors };
          delete errors['dateBefore'];
          endCtrl.setErrors(Object.keys(errors).length > 0 ? errors : null);
        }
        return null;
      }
    };
  }

  // Đợt đăng ký hiện tại
  protected currentDot: {
    id: number;
    hocKy: string;
    namHoc: string;
    tenDot: string;
    dangMo: boolean;
    ngayBatDau?: string | null;
    ngayKetThuc?: string | null;
  } | null = null;

  protected showConfigDotModal = false;
  protected dotModalError = '';
  protected batchModalError = '';
  protected addRegModalError = '';
  protected addMoModalError = '';

  protected readonly configDotForm = this.fb.group(
    {
      hocKy: ['1', Validators.required],
      namHoc: ['2026-2027', Validators.required],
      tenDot: ['', Validators.required],
      dangMo: [true],
      ngayBatDau: ['', Validators.required],
      ngayKetThuc: ['', Validators.required],
    },
    { validators: [AdminDiemDangKyComponent.dateRangeValidator('ngayBatDau', 'ngayKetThuc')] }
  );

  // Loading & alerts
  protected loading = false;
  protected saving = false;
  protected errorMessage = '';
  protected successMessage = '';

  ngOnInit(): void {
    this.loadMasterData();
    this.loadCurrentDot();
    this.refreshAllData();
    this.loadPendingClasses();
    this.loadMonHocMoList();
  }

  private formatDateForInput(dateVal: any, defaultOffsetDays = 0): string {
    if (!dateVal) {
      const d = new Date(Date.now() + defaultOffsetDays * 86400000);
      return d.toISOString().slice(0, 10);
    }
    if (Array.isArray(dateVal)) {
      const y = String(dateVal[0]).padStart(4, '0');
      const m = String(dateVal[1]).padStart(2, '0');
      const d = String(dateVal[2]).padStart(2, '0');
      return `${y}-${m}-${d}`;
    }
    if (typeof dateVal === 'string') {
      return dateVal.slice(0, 10);
    }
    if (dateVal instanceof Date) {
      return dateVal.toISOString().slice(0, 10);
    }
    return new Date().toISOString().slice(0, 10);
  }

  private sanitizeDot<T extends { hocKy: string; namHoc: string; tenDot: string; ngayBatDau?: any; ngayKetThuc?: any } | null>(dot: T): T {
    if (!dot) return dot;
    if (!dot.tenDot || dot.tenDot.includes('?')) {
      dot.tenDot = `Đợt đăng ký tín chỉ Học kỳ ${dot.hocKy} (${dot.namHoc})`;
    }
    dot.ngayBatDau = this.formatDateForInput(dot.ngayBatDau, 0);
    dot.ngayKetThuc = this.formatDateForInput(dot.ngayKetThuc, 30);
    return dot;
  }

  protected loadCurrentDot(): void {
    this.http.get<typeof this.currentDot>('http://localhost:8080/api/dot-dang-ky/hien-tai').subscribe({
      next: (res) => {
        this.currentDot = this.sanitizeDot(res);
        if (res) {
          this.filterRegHocKy = res.hocKy;
          this.filterRegNamHoc = res.namHoc;
        }
        this.cd.detectChanges();
      },
      error: () => {},
    });
  }

  protected toggleDotOpen(): void {
    if (!this.currentDot) return;
    const newStatus = !this.currentDot.dangMo;
    this.http.patch<typeof this.currentDot>(`http://localhost:8080/api/dot-dang-ky/hien-tai/toggle?open=${newStatus}`, {}).subscribe({
      next: (updated) => {
        this.currentDot = this.sanitizeDot(updated);
        const msg = newStatus ? 'Đã MỞ CỔNG đăng ký môn học cho sinh viên!' : 'Đã KHÓA CỔNG đăng ký môn học!';
        this.toastService.success(msg, 'Trạng thái Cổng Đăng ký');
        this.cd.detectChanges();
      },
      error: (err) => {
        const msg = this.toastService.extractError(err, 'Không thể cập nhật trạng thái cổng đăng ký.');
        this.toastService.error(msg, 'Lỗi thao tác');
        this.cd.detectChanges();
      },
    });
  }

  protected isControlInvalid(form: FormGroup, controlName: string): boolean {
    const ctrl = form.get(controlName);
    return !!ctrl && ctrl.invalid && (ctrl.touched || ctrl.dirty);
  }

  protected getDotTimeStatus(): 'ACTIVE' | 'NOT_YET' | 'EXPIRED' | 'UNKNOWN' {
    if (!this.currentDot || !this.currentDot.ngayBatDau || !this.currentDot.ngayKetThuc) {
      return 'UNKNOWN';
    }
    const today = new Date().toISOString().slice(0, 10);
    if (today < this.currentDot.ngayBatDau) {
      return 'NOT_YET';
    }
    if (today > this.currentDot.ngayKetThuc) {
      return 'EXPIRED';
    }
    return 'ACTIVE';
  }

  protected openConfigDotModal(): void {
    this.dotModalError = '';
    const batDau = this.formatDateForInput(this.currentDot?.ngayBatDau, 0);
    const ketThuc = this.formatDateForInput(this.currentDot?.ngayKetThuc, 30);

    this.configDotForm.patchValue({
      hocKy: this.currentDot?.hocKy || '1',
      namHoc: this.currentDot?.namHoc || '2026-2027',
      tenDot: (this.currentDot?.tenDot && !this.currentDot.tenDot.includes('?'))
        ? this.currentDot.tenDot
        : `Đợt đăng ký tín chỉ Học kỳ ${this.currentDot?.hocKy || '1'} (${this.currentDot?.namHoc || '2026-2027'})`,
      dangMo: this.currentDot ? this.currentDot.dangMo : true,
      ngayBatDau: batDau,
      ngayKetThuc: ketThuc,
    });
    this.showConfigDotModal = true;
  }

  protected closeConfigDotModal(): void {
    this.showConfigDotModal = false;
    this.dotModalError = '';
  }

  protected saveConfigDot(): void {
    this.dotModalError = '';
    if (this.configDotForm.invalid) {
      this.configDotForm.markAllAsTouched();
      const val = this.configDotForm.getRawValue();
      if (val.ngayBatDau && val.ngayKetThuc && val.ngayBatDau >= val.ngayKetThuc) {
        this.dotModalError = 'Thời gian trước phải nhỏ hơn thời gian sau (Ngày bắt đầu phải nhỏ hơn ngày kết thúc).';
        this.toastService.error(this.dotModalError, 'Lỗi khoảng thời gian');
      } else {
        this.dotModalError = 'Vui lòng kiểm tra và điền đầy đủ các thông tin bắt buộc (các ô viền đỏ).';
        this.toastService.warning(this.dotModalError, 'Thiếu thông tin');
      }
      return;
    }

    const val = this.configDotForm.getRawValue();
    this.saving = true;

    this.http.put<typeof this.currentDot>('http://localhost:8080/api/dot-dang-ky/hien-tai', val).subscribe({
      next: (updated) => {
        this.currentDot = this.sanitizeDot(updated);
        this.saving = false;
        this.showConfigDotModal = false;
        if (updated) {
          this.filterRegHocKy = updated.hocKy;
          this.filterRegNamHoc = updated.namHoc;
        }
        this.toastService.success('Đã cập nhật Đợt đăng ký tín chỉ thành công! Cổng đăng ký sẽ tự động mở/đóng theo mốc thời gian đã đặt.', 'Cấu hình hoàn tất');
        this.cd.detectChanges();
      },
      error: (err) => {
        this.saving = false;
        const msg = this.toastService.extractError(err, 'Không thể lưu đợt đăng ký. Vui lòng kiểm tra lại thời gian.');
        this.dotModalError = msg;
        this.toastService.error(msg, 'Lỗi cấu hình đợt đăng ký');
        this.cd.detectChanges();
      },
    });
  }

  protected loadMasterData(): void {
    forkJoin({
      monHocs: this.http.get<MonHocOption[]>('http://localhost:8080/api/mon-hoc').pipe(catchError(() => of([]))),
      sinhViens: this.http.get<SinhVienOption[]>('http://localhost:8080/api/sinh-vien').pipe(catchError(() => of([]))),
      khoas: this.http.get<KhoaOption[]>('http://localhost:8080/api/khoa').pipe(catchError(() => of([]))),
      lops: this.http.get<LopOption[]>('http://localhost:8080/api/lop').pipe(catchError(() => of([]))),
      giangViens: this.http.get<GiangVienOption[]>('http://localhost:8080/api/giang-vien').pipe(catchError(() => of([]))),
    }).subscribe({
      next: ({ monHocs, sinhViens, khoas, lops, giangViens }) => {
        this.monHocs = monHocs;
        this.sinhViens = sinhViens;
        this.khoas = khoas;
        this.lops = lops;
        this.giangViens = giangViens;
        if (khoas.length > 0 && !this.moKhoaId) {
          this.moKhoaId = String(khoas[0].id);
        }
        this.cd.detectChanges();
      },
    });
  }

  protected refreshAllData(): void {
    this.loading = true;
    this.errorMessage = '';
    this.loadPendingClasses();

    this.http.get<DangKyItem[]>('http://localhost:8080/api/dang-ky-mon-hoc').pipe(catchError(() => of([]))).subscribe({
      next: (regs) => {
        this.allRegistrations = regs;
        this.loading = false;
        this.cd.detectChanges();
      },
      error: () => {
        this.errorMessage = 'Không thể tải dữ liệu đào tạo.';
        this.loading = false;
        this.cd.detectChanges();
      },
    });
  }

  // --- Tab Navigation ---
  protected selectTab(tab: 'duyet' | 'dangky' | 'monhocmo'): void {
    this.activeTab = tab;
    if (tab === 'duyet') {
      this.loadPendingClasses();
    } else if (tab === 'dangky') {
      this.refreshAllData();
    } else if (tab === 'monhocmo') {
      this.loadMonHocMoList();
    }
    this.cd.detectChanges();
  }

  // ==================== PHÊ DUYỆT BẢNG ĐIỂM ====================
  protected loadPendingClasses(): void {
    this.loadingPending = true;
    this.http.get<any[]>('http://localhost:8080/api/admin/duyet-diem/pending').subscribe({
      next: (res) => {
        this.pendingClasses = res;
        this.loadingPending = false;
        if (this.selectedPendingClass) {
          const found = res.find((c) => c.monHocMoId === this.selectedPendingClass.monHocMoId);
          if (found) {
            this.viewPendingClass(found);
          } else {
            this.selectedPendingClass = null;
            this.pendingClassStudents = [];
          }
        }
        this.cd.detectChanges();
      },
      error: () => {
        this.loadingPending = false;
        this.cd.detectChanges();
      },
    });
  }

  protected viewPendingClass(cls: any): void {
    this.selectedPendingClass = cls;
    this.pendingPage = 1;
    this.loadingPendingDetails = true;
    this.cd.detectChanges();
    this.http.get<any>(`http://localhost:8080/api/admin/duyet-diem/chi-tiet/${cls.monHocMoId}`).subscribe({
      next: (res) => {
        const list = Array.isArray(res) ? res : (res?.students || []);
        this.pendingClassStudents = list.map((s: any) => ({
          ...s,
          mssv: s.mssv || s.sinhVienMssv,
          hoTen: s.hoTen || s.sinhVienHoTen,
          lopTen: s.lopTen || s.tenLop || s.lopMa || '-',
          lopMa: s.lopMa || s.tenLop || '-',
        }));
        this.loadingPendingDetails = false;
        this.cd.detectChanges();
      },
      error: () => {
        this.loadingPendingDetails = false;
        this.errorMessage = 'Không thể tải chi tiết bảng điểm chờ duyệt.';
        this.cd.detectChanges();
      },
    });
  }

  protected get paginatedPendingClassStudents(): any[] {
    const start = (this.pendingPage - 1) * this.pendingPageSize;
    return this.pendingClassStudents.slice(start, start + this.pendingPageSize);
  }

  protected async approvePendingClass(cls: any): Promise<void> {
    const courseTitle = cls.monHocTen || cls.tenMonHoc;
    const classTitle = cls.maLop ? `${cls.tenLop} (${cls.maLop})` : (cls.tenLop || `Toàn khóa ${cls.khoaHoc || ''}`);

    const confirmed = await this.confirmDialog.confirm({
      title: 'Phê duyệt công bố điểm',
      message: `Bạn có chắc muốn DUYỆT CÔNG BỐ bảng điểm môn "${courseTitle}" (${classTitle})?\nĐiểm sẽ được công bố chính thức cho sinh viên tra cứu ngay lập tức.`,
      confirmText: 'Duyệt công bố',
      cancelText: 'Hủy bỏ',
      type: 'primary',
    });
    if (!confirmed) {
      return;
    }
    this.loadingPendingDetails = true;
    this.http.put(`http://localhost:8080/api/admin/duyet-diem/${cls.monHocMoId}/approve`, {}).subscribe({
      next: () => {
        this.toastService.success(`Đã duyệt công bố bảng điểm môn ${courseTitle} thành công!`, 'Phê duyệt hoàn tất');
        this.selectedPendingClass = null;
        this.pendingClassStudents = [];
        this.loadPendingClasses();
        this.refreshAllData();
        this.cd.detectChanges();
      },
      error: (err) => {
        this.loadingPendingDetails = false;
        const msg = this.toastService.extractError(err, 'Không thể duyệt bảng điểm.');
        this.toastService.error(msg, 'Lỗi phê duyệt');
        this.cd.detectChanges();
      },
    });
  }

  protected async rejectPendingClass(cls: any): Promise<void> {
    const courseTitle = cls.monHocTen || cls.tenMonHoc;
    const reason = await this.confirmDialog.prompt({
      title: 'Từ chối phê duyệt bảng điểm',
      message: `Nhập lý do từ chối bảng điểm môn "${courseTitle}" để gửi thông báo phản hồi cho Giảng viên:`,
      defaultValue: 'Cần rà soát và kiểm tra lại điểm thành phần',
      placeholder: 'Nhập lý do cụ thể...',
      confirmText: 'Gửi lý do từ chối',
      cancelText: 'Hủy bỏ',
      type: 'warning',
      inputType: 'textarea',
      required: true,
    });
    if (!reason || !reason.trim()) {
      return;
    }
    this.loadingPendingDetails = true;
    this.http.put(`http://localhost:8080/api/admin/duyet-diem/${cls.monHocMoId}/reject`, {
      ghiChu: reason.trim(),
      reason: reason.trim(),
    }).subscribe({
      next: () => {
        this.toastService.info(`Đã từ chối bảng điểm môn ${courseTitle}. Giảng viên sẽ nhận được lý do để chỉnh sửa lại.`, 'Từ chối duyệt');
        this.selectedPendingClass = null;
        this.pendingClassStudents = [];
        this.loadPendingClasses();
        this.refreshAllData();
        this.cd.detectChanges();
      },
      error: (err) => {
        this.loadingPendingDetails = false;
        const msg = this.toastService.extractError(err, 'Không thể từ chối bảng điểm.');
        this.toastService.error(msg, 'Lỗi từ chối duyệt');
        this.cd.detectChanges();
      },
    });
  }

  // ==================== QUẢN LÝ ĐĂNG KÝ MÔN HỌC ====================
  protected get filteredRegistrations(): DangKyItem[] {
    const q = this.searchRegStudent.trim().toLowerCase();
    return this.allRegistrations.filter((r) => {
      const matchHk = !this.filterRegHocKy || r.hocKy === this.filterRegHocKy;
      const matchNh = !this.filterRegNamHoc || r.namHoc === this.filterRegNamHoc;
      const matchMon = !this.filterRegMonHocId || String(r.monHocId) === this.filterRegMonHocId;
      const matchSearch =
        !q ||
        r.sinhVienMssv.toLowerCase().includes(q) ||
        r.sinhVienHoTen.toLowerCase().includes(q) ||
        r.monHocTen.toLowerCase().includes(q) ||
        r.monHocMa.toLowerCase().includes(q);
      return matchHk && matchNh && matchMon && matchSearch;
    });
  }

  protected get paginatedRegistrations(): DangKyItem[] {
    const start = (this.regPage - 1) * this.regPageSize;
    return this.filteredRegistrations.slice(start, start + this.regPageSize);
  }

  protected openAddRegistrationModal(): void {
    this.addRegModalError = '';
    this.showAddRegModal = true;
    this.addRegForm.patchValue({
      sinhVienId: '',
      monHocId: this.monHocs[0]?.id ? String(this.monHocs[0].id) : '',
      hocKy: this.filterRegHocKy,
      namHoc: this.filterRegNamHoc,
    });
  }

  protected closeAddRegistrationModal(): void {
    this.showAddRegModal = false;
    this.addRegModalError = '';
  }

  protected saveRegistration(): void {
    this.addRegModalError = '';
    if (this.addRegForm.invalid) {
      this.addRegForm.markAllAsTouched();
      this.addRegModalError = 'Vui lòng chọn đầy đủ Sinh viên và Môn học.';
      return;
    }

    const val = this.addRegForm.getRawValue();
    const payload = {
      sinhVienId: Number(val.sinhVienId),
      monHocId: Number(val.monHocId),
      hocKy: val.hocKy,
      namHoc: val.namHoc,
      trangThai: 'DANG_KY',
    };

    this.saving = true;

    this.http.post<DangKyItem>('http://localhost:8080/api/dang-ky-mon-hoc', payload).subscribe({
      next: (savedReg) => {
        this.saving = false;
        this.closeAddRegistrationModal();
        this.toastService.success(`Đã ghi danh thành công sinh viên vào môn ${savedReg.monHocTen}!`, 'Ghi danh thành công');
        this.allRegistrations = [savedReg, ...this.allRegistrations];
        this.cd.detectChanges();
      },
      error: (err) => {
        this.saving = false;
        const msg = this.toastService.extractError(err, 'Sinh viên này đã đăng ký môn học trong học kỳ này rồi.');
        this.addRegModalError = msg;
        this.toastService.error(msg, 'Lỗi ghi danh sinh viên');
        this.cd.detectChanges();
      },
    });
  }

  // Ghi danh toàn bộ sinh viên lớp hành chính
  protected openBatchRegModal(): void {
    this.batchModalError = '';
    this.batchRegForm.patchValue({
      lopId: this.lops[0]?.id ? String(this.lops[0].id) : '',
      monHocId: this.monHocs[0]?.id ? String(this.monHocs[0].id) : '',
      hocKy: this.filterRegHocKy,
      namHoc: this.filterRegNamHoc,
    });
    this.showBatchRegModal = true;
  }

  protected closeBatchRegModal(): void {
    this.showBatchRegModal = false;
    this.batchModalError = '';
  }

  protected saveBatchRegistration(): void {
    this.batchModalError = '';
    if (this.batchRegForm.invalid) {
      this.batchRegForm.markAllAsTouched();
      this.batchModalError = 'Vui lòng chọn đầy đủ Lớp hành chính và Môn học.';
      this.toastService.warning(this.batchModalError, 'Thiếu thông tin');
      return;
    }
    const val = this.batchRegForm.getRawValue();
    const lop = this.lops.find((l) => String(l.id) === val.lopId);
    const mon = this.monHocs.find((m) => String(m.id) === val.monHocId);

    this.saving = true;

    this.http
      .post<DangKyItem[]>(
        `http://localhost:8080/api/dang-ky-mon-hoc/lop/${val.lopId}?monHocId=${val.monHocId}&hocKy=${val.hocKy}&namHoc=${val.namHoc}`,
        {}
      )
      .subscribe({
        next: (allRegs) => {
          this.allRegistrations = allRegs;
          this.saving = false;
          this.showBatchRegModal = false;
          this.toastService.success(`Đã ghi danh toàn bộ sinh viên Lớp "${lop?.tenLop}" vào môn học "${mon?.tenMonHoc}" thành công!`, 'Ghi danh theo lớp');
          this.cd.detectChanges();
        },
        error: (err) => {
          this.saving = false;
          const msg = this.toastService.extractError(err, 'Có lỗi xảy ra khi ghi danh lớp hành chính.');
          this.batchModalError = msg;
          this.toastService.error(msg, 'Lỗi ghi danh theo lớp');
          this.cd.detectChanges();
        },
      });
  }

  protected async cancelRegistration(item: DangKyItem): Promise<void> {
    const confirmed = await this.confirmDialog.confirm({
      title: 'Hủy đăng ký môn học',
      message: `Bạn có chắc muốn hủy đăng ký môn ${item.monHocTen} của sinh viên ${item.sinhVienHoTen} (${item.sinhVienMssv})?`,
      confirmText: 'Hủy đăng ký',
      cancelText: 'Giữ lại',
      type: 'danger',
    });
    if (!confirmed) {
      return;
    }

    this.http.delete(`http://localhost:8080/api/dang-ky-mon-hoc/${item.id}`).subscribe({
      next: () => {
        this.allRegistrations = this.allRegistrations.filter((r) => r.id !== item.id);
        this.toastService.success(`Đã hủy môn ${item.monHocMa} cho sinh viên ${item.sinhVienMssv}.`, 'Hủy đăng ký');
        this.cd.detectChanges();
      },
      error: (err) => {
        const msg = this.toastService.extractError(err, 'Không thể hủy đăng ký môn học này.');
        this.toastService.error(msg, 'Lỗi hủy đăng ký');
        this.cd.detectChanges();
      },
    });
  }

  // ==================== MỞ MÔN THEO KHÓA & PHÂN CÔNG THEO LỚP ====================
  protected loadMonHocMoList(): void {
    this.moPage = 1;
    if (this.moLopId) {
      const params: any = {
        hocKy: this.moHocKy,
        namHoc: this.moNamHoc,
      };
      this.http.get<MonHocMo[]>(`http://localhost:8080/api/mon-hoc-mo/lop/${this.moLopId}`, { params }).subscribe({
        next: (res) => {
          this.monHocMoList = res;
          this.cd.detectChanges();
        },
        error: () => {
          this.errorMessage = 'Không thể tải danh sách môn học của lớp này.';
          this.cd.detectChanges();
        },
      });
      return;
    }

    let params: any = {
      hocKy: this.moHocKy,
      namHoc: this.moNamHoc,
    };
    if (this.moKhoaId) params.khoaId = this.moKhoaId;
    if (this.moKhoaHoc) params.khoaHoc = this.moKhoaHoc;

    this.http.get<MonHocMo[]>('http://localhost:8080/api/mon-hoc-mo', { params }).subscribe({
      next: (res) => {
        this.monHocMoList = res;
        this.cd.detectChanges();
      },
      error: () => {
        this.errorMessage = 'Không thể tải danh sách môn học mở.';
        this.cd.detectChanges();
      },
    });
  }

  protected get paginatedMonHocMoList(): MonHocMo[] {
    const start = (this.moPage - 1) * this.moPageSize;
    return this.monHocMoList.slice(start, start + this.moPageSize);
  }

  protected get filteredMonHocsForMo(): MonHocOption[] {
    const selectedKhoaId = this.addMoForm.get('khoaId')?.value || this.moKhoaId;
    if (!selectedKhoaId) return this.monHocs;
    const kId = Number(selectedKhoaId);
    return this.monHocs.filter((m) => !m.khoaId || m.khoaId === kId);
  }

  protected get filteredLopsForMo(): LopOption[] {
    const selectedKhoaId = this.moKhoaId;
    const selectedKhoaHoc = (this.moKhoaHoc || '').replace(/\D+/g, '');
    return this.lops.filter((l) => {
      const matchKhoa = !selectedKhoaId || !l.khoaId || l.khoaId === Number(selectedKhoaId);
      const matchKhoaHoc =
        !selectedKhoaHoc ||
        l.maLop.toUpperCase().includes(selectedKhoaHoc) ||
        (l.nienKhoa && l.nienKhoa.includes(selectedKhoaHoc));
      return matchKhoa && matchKhoaHoc;
    });
  }

  protected get selectedLopObj(): LopOption | undefined {
    if (!this.moLopId) return undefined;
    const lid = Number(this.moLopId);
    return this.lops.find((l) => l.id === lid);
  }

  protected get assignedGvCount(): number {
    return this.monHocMoList.filter((m) => !!m.giangVienId).length;
  }

  protected get unassignedGvCount(): number {
    return this.monHocMoList.filter((m) => !m.giangVienId).length;
  }

  protected openAddMoModal(): void {
    this.addMoForm.patchValue({
      khoaId: this.moKhoaId || (this.khoas.length > 0 ? String(this.khoas[0].id) : ''),
      khoaHoc: this.moKhoaHoc || '2023',
      hocKy: this.moHocKy,
      namHoc: this.moNamHoc,
      monHocId: '',
      ghiChu: '',
    });
    this.addMoModalError = '';
    this.showAddMoModal = true;
  }

  protected closeAddMoModal(): void {
    this.showAddMoModal = false;
    this.addMoModalError = '';
  }

  protected submitAddMo(): void {
    this.addMoModalError = '';
    if (this.addMoForm.invalid) {
      this.addMoForm.markAllAsTouched();
      this.addMoModalError = 'Vui lòng chọn đầy đủ Khoa và Môn học cần mở.';
      this.toastService.warning(this.addMoModalError, 'Thiếu thông tin');
      return;
    }
    const val = this.addMoForm.getRawValue();
    const rawKhoa = val.khoaHoc?.trim() || '';
    const cleanKhoa = rawKhoa.replace(/\D+/g, '');
    const payload = {
      monHocId: Number(val.monHocId),
      khoaId: Number(val.khoaId),
      khoaHoc: cleanKhoa || rawKhoa,
      hocKy: val.hocKy,
      namHoc: val.namHoc,
      lopId: null,
      giangVienId: null,
      ghiChu: val.ghiChu?.trim() || null,
    };

    this.saving = true;

    this.http.post<MonHocMo>('http://localhost:8080/api/mon-hoc-mo', payload).subscribe({
      next: (res) => {
        this.saving = false;
        this.closeAddMoModal();
        this.toastService.success(`Đã mở môn "${res.tenMonHoc}" cho toàn bộ các lớp thuộc Khóa ${res.khoaHoc} (${res.tenKhoa || ''})!`, 'Mở môn thành công');
        this.loadMonHocMoList();
        this.cd.detectChanges();
      },
      error: (err) => {
        this.saving = false;
        const msg = this.toastService.extractError(err, 'Không thể mở môn học cho khóa này.');
        this.addMoModalError = msg;
        this.toastService.error(msg, 'Lỗi mở môn học');
        this.cd.detectChanges();
      },
    });
  }

  protected quickAssignGiangVien(moId: number, event: Event): void {
    const target = event.target as HTMLSelectElement;
    const gvId = target.value ? Number(target.value) : null;
    const url = gvId
      ? `http://localhost:8080/api/mon-hoc-mo/${moId}/gan-giang-vien?giangVienId=${gvId}`
      : `http://localhost:8080/api/mon-hoc-mo/${moId}/gan-giang-vien`;

    this.http.put<MonHocMo>(url, {}).subscribe({
      next: (updated) => {
        const msg = updated.tenGiangVien
          ? `Đã phân công giảng viên ${updated.tenGiangVien} phụ trách môn ${updated.tenMonHoc}!`
          : `Đã hủy phân công giảng viên cho môn ${updated.tenMonHoc}!`;
        this.toastService.success(msg, 'Phân công Giảng viên');
        this.monHocMoList = this.monHocMoList.map((m) => (m.id === updated.id ? updated : m));
        this.cd.detectChanges();
      },
      error: (err) => {
        const msg = this.toastService.extractError(err, 'Không thể phân công giảng viên.');
        this.toastService.error(msg, 'Lỗi phân công');
        this.cd.detectChanges();
      },
    });
  }

  protected async removeMonHocMo(item: MonHocMo): Promise<void> {
    const isClassSpecific = !!item.tenLop;
    const msg = isClassSpecific
      ? `Bạn có chắc muốn hủy môn "${item.tenMonHoc}" (${item.monHocMa}) của lớp ${item.tenLop}?`
      : `Bạn có chắc muốn xóa môn "${item.tenMonHoc}" (${item.monHocMa}) khỏi danh mục mở chung của khóa ${item.khoaHoc}? (Tất cả các lớp trong khóa này cũng sẽ được hủy môn)`;

    const confirmed = await this.confirmDialog.confirm({
      title: 'Xóa môn học mở',
      message: msg,
      confirmText: 'Xóa môn',
      cancelText: 'Hủy',
      type: 'danger',
    });
    if (!confirmed) {
      return;
    }
    this.http.delete(`http://localhost:8080/api/mon-hoc-mo/${item.id}`).subscribe({
      next: () => {
        this.monHocMoList = this.monHocMoList.filter((m) => m.id !== item.id);
        this.toastService.success(`Đã xóa môn "${item.tenMonHoc}".`, 'Xóa môn mở');
        this.cd.detectChanges();
      },
      error: (err) => {
        const errMsg = this.toastService.extractError(err, 'Không thể xóa môn học khỏi kỳ mở.');
        this.toastService.error(errMsg, 'Lỗi xóa môn');
        this.cd.detectChanges();
      },
    });
  }

  protected formatCohort(khoa: string | undefined): string {
    if (!khoa) return '---';
    const num = khoa.replace(/\D+/g, '');
    return num || khoa;
  }
}

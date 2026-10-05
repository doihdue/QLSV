import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { ChangeDetectorRef, Component, HostListener, inject, OnInit } from '@angular/core';
import { AbstractControl, FormBuilder, FormsModule, ReactiveFormsModule, ValidationErrors, ValidatorFn, Validators } from '@angular/forms';
import { PaginationComponent } from '../../../components/pagination/pagination.component';
import { ToastService } from '../../../services/toast.service';
import { ConfirmDialogService } from '../../../services/confirm-dialog.service';

export type SinhVienItem = {
  id: number;
  mssv: string;
  hoTen: string;
  ngaySinh?: string;
  gioiTinh?: string;
  email: string;
  soDienThoai?: string;
  diaChi?: string;
  ngayNhapHoc?: string;
  lopId: number;
  lopTen?: string;
  khoaId?: number;
  khoaTen?: string;
  khoaHoc?: string;
  active: boolean;
};

export type LopOption = {
  id: number;
  maLop: string;
  tenLop: string;
  khoaTen?: string;
};

@Component({
  selector: 'app-admin-sinh-vien',
  imports: [CommonModule, FormsModule, ReactiveFormsModule, PaginationComponent],
  templateUrl: './admin-sinh-vien.component.html',
  styleUrl: './admin-sinh-vien.component.scss',
})
export class AdminSinhVienComponent implements OnInit {
  private readonly http = inject(HttpClient);
  private readonly fb = inject(FormBuilder);
  private readonly cd = inject(ChangeDetectorRef);
  private readonly toastService = inject(ToastService);
  private readonly confirmDialog = inject(ConfirmDialogService);

  protected sinhViens: SinhVienItem[] = [];
  protected lops: LopOption[] = [];

  protected searchText = '';
  protected filterLopId = '';
  protected filterGender = '';

  protected currentPage = 1;
  protected pageSize = 10;

  protected showModal = false;
  protected editingId: number | null = null;
  protected loading = false;
  protected saving = false;
  protected errorMessage = '';
  protected successMessage = '';

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

  protected isControlInvalid(controlName: string): boolean {
    const ctrl = this.form.get(controlName);
    if (!ctrl) return false;
    const isRequired = ctrl.hasValidator(Validators.required);
    const isEmpty = !ctrl.value || (typeof ctrl.value === 'string' && !ctrl.value.trim());
    return (ctrl.invalid || (isRequired && isEmpty)) && (ctrl.touched || ctrl.dirty);
  }

  protected readonly form = this.fb.group(
    {
      mssv: [''],
      hoTen: ['', [Validators.required, Validators.maxLength(150)]],
      ngaySinh: [''],
      gioiTinh: ['Nam'],
      email: [''],
      soDienThoai: ['', [Validators.maxLength(20)]],
      diaChi: ['', [Validators.maxLength(255)]],
      ngayNhapHoc: ['2023-09-05'],
      lopId: ['', Validators.required],
      active: [true],
    },
    { validators: [AdminSinhVienComponent.dateRangeValidator('ngaySinh', 'ngayNhapHoc')] }
  );

  ngOnInit(): void {
    this.loadLops();
    this.loadSinhViens();
  }

  protected loadLops(): void {
    this.http.get<LopOption[]>('http://localhost:8080/api/lop').subscribe({
      next: (res) => {
        this.lops = res;
        this.cd.detectChanges();
      },
    });
  }

  protected loadSinhViens(): void {
    this.loading = true;
    this.errorMessage = '';
    this.http.get<SinhVienItem[]>('http://localhost:8080/api/sinh-vien').subscribe({
      next: (res) => {
        this.sinhViens = res;
        this.loading = false;
        this.cd.detectChanges();
      },
      error: () => {
        this.errorMessage = 'Không thể tải danh sách sinh viên.';
        this.loading = false;
        this.cd.detectChanges();
      },
    });
  }

  protected get filteredSinhViens(): SinhVienItem[] {
    const q = this.searchText.trim().toLowerCase();
    return this.sinhViens.filter((item) => {
      const matchSearch =
        !q ||
        item.mssv.toLowerCase().includes(q) ||
        item.hoTen.toLowerCase().includes(q) ||
        item.email.toLowerCase().includes(q) ||
        (item.soDienThoai && item.soDienThoai.includes(q));
      const matchLop = !this.filterLopId || String(item.lopId) === this.filterLopId;
      const matchGender = !this.filterGender || item.gioiTinh === this.filterGender;
      return matchSearch && matchLop && matchGender;
    });
  }

  protected get paginatedSinhViens(): SinhVienItem[] {
    const start = (this.currentPage - 1) * this.pageSize;
    return this.filteredSinhViens.slice(start, start + this.pageSize);
  }

  protected submit(): void {
    const val = this.form.getRawValue();
    const hoTen = val.hoTen?.trim();
    const lopId = val.lopId;

    if (!lopId || !hoTen) {
      this.form.markAllAsTouched();
      if (!lopId && !hoTen) {
        this.toastService.warning('Vui lòng chọn Lớp hành chính và nhập Họ tên sinh viên (các trường viền đỏ).', 'Thiếu thông tin bắt buộc');
      } else if (!lopId) {
        this.toastService.warning('Vui lòng chọn Lớp hành chính cho sinh viên.', 'Thiếu thông tin');
      } else {
        this.toastService.warning('Vui lòng nhập Họ và tên sinh viên.', 'Thiếu thông tin');
      }
      this.cd.detectChanges();
      return;
    }

    if (val.ngaySinh && val.ngayNhapHoc && val.ngaySinh >= val.ngayNhapHoc) {
      this.form.markAllAsTouched();
      this.toastService.error('Thời gian trước phải nhỏ hơn thời gian sau (Ngày sinh phải nhỏ hơn ngày nhập học).', 'Lỗi khoảng thời gian');
      this.cd.detectChanges();
      return;
    }

    if (this.form.invalid) {
      this.form.markAllAsTouched();
      this.toastService.warning('Vui lòng kiểm tra lại các trường thông tin không hợp lệ (viền đỏ).', 'Dữ liệu chưa đúng');
      this.cd.detectChanges();
      return;
    }

    const payload = {
      mssv: this.editingId === null ? undefined : (val.mssv?.trim() || undefined),
      hoTen: hoTen,
      ngaySinh: val.ngaySinh || null,
      gioiTinh: val.gioiTinh || 'Nam',
      email: this.editingId === null ? undefined : (val.email?.trim() || undefined),
      soDienThoai: val.soDienThoai?.trim() || null,
      diaChi: val.diaChi?.trim() || null,
      ngayNhapHoc: val.ngayNhapHoc || null,
      lopId: Number(val.lopId),
      active: val.active ?? true,
    };

    this.saving = true;

    const req = this.editingId === null
      ? this.http.post<SinhVienItem>('http://localhost:8080/api/sinh-vien', payload)
      : this.http.put<SinhVienItem>(`http://localhost:8080/api/sinh-vien/${this.editingId}`, payload);

    req.subscribe({
      next: () => {
        this.saving = false;
        const msg = this.editingId === null
          ? 'Thêm mới sinh viên thành công! Tài khoản đăng nhập đã được tạo.'
          : 'Cập nhật thông tin sinh viên thành công!';
        this.toastService.success(msg, 'Quản lý Sinh viên');
        this.loadSinhViens();
        this.closeModal();
      },
      error: (err) => {
        this.saving = false;
        const errorMsg = this.toastService.extractError(err, 'Không thể lưu sinh viên. Kiểm tra MSSV và email có bị trùng không.');
        this.toastService.error(errorMsg, 'Lỗi lưu sinh viên');
        this.cd.detectChanges();
      },
    });
  }

  @HostListener('window:keydown.escape')
  protected onEscape(): void {
    if (this.showModal && !this.saving) {
      this.closeModal();
    }
  }

  protected openCreateModal(): void {
    this.resetForm();
    this.editingId = null;
    this.showModal = true;
    this.cd.detectChanges();
  }

  protected openEditModal(item: SinhVienItem): void {
    this.editingId = item.id;
    this.form.setValue({
      mssv: item.mssv,
      hoTen: item.hoTen,
      ngaySinh: item.ngaySinh || '',
      gioiTinh: item.gioiTinh || 'Nam',
      email: item.email,
      soDienThoai: item.soDienThoai || '',
      diaChi: item.diaChi || '',
      ngayNhapHoc: item.ngayNhapHoc || '',
      lopId: String(item.lopId),
      active: item.active,
    });
    this.showModal = true;
    this.cd.detectChanges();
  }

  protected closeModal(): void {
    this.showModal = false;
    this.resetForm();
  }

  protected edit(item: SinhVienItem): void {
    this.openEditModal(item);
  }

  protected async remove(item: SinhVienItem): Promise<void> {
    const confirmed = await this.confirmDialog.confirm({
      title: 'Xác nhận xóa sinh viên',
      message: `Bạn có chắc muốn xóa sinh viên ${item.hoTen} (MSSV: ${item.mssv})?\nToàn bộ dữ liệu điểm và đăng ký môn liên quan cũng sẽ bị xóa.`,
      confirmText: 'Xóa sinh viên',
      cancelText: 'Hủy bỏ',
      type: 'danger',
    });
    if (!confirmed) {
      return;
    }

    this.http.delete(`http://localhost:8080/api/sinh-vien/${item.id}`).subscribe({
      next: () => {
        this.sinhViens = this.sinhViens.filter((x) => x.id !== item.id);
        this.toastService.success(`Đã xóa sinh viên ${item.mssv} thành công.`, 'Xóa sinh viên');
        this.cd.detectChanges();
      },
      error: (err) => {
        const msg = this.toastService.extractError(err, 'Không thể xóa sinh viên này.');
        this.toastService.error(msg, 'Lỗi xóa sinh viên');
        this.cd.detectChanges();
      },
    });
  }

  protected resetForm(): void {
    this.editingId = null;
    this.form.reset({
      mssv: '',
      hoTen: '',
      ngaySinh: '',
      gioiTinh: 'Nam',
      email: '',
      soDienThoai: '',
      diaChi: '',
      ngayNhapHoc: '2023-09-05',
      lopId: '',
      active: true,
    });
    this.cd.detectChanges();
  }

  protected getCohort(sv: SinhVienItem): string {
    if (sv.khoaHoc) {
      return `Khóa ${sv.khoaHoc}`;
    }
    if (sv.ngayNhapHoc) {
      const year = new Date(sv.ngayNhapHoc).getFullYear();
      if (!isNaN(year)) return `Khóa ${year}`;
    }
    const match = sv.mssv?.match(/^(\d{4})/);
    return match ? `Khóa ${match[1]}` : '';
  }

  protected async resetPassword(sv: SinhVienItem): Promise<void> {
    const confirmed = await this.confirmDialog.confirm({
      title: 'Cấp lại mật khẩu sinh viên',
      message: `Bạn có chắc muốn đặt lại mật khẩu đăng nhập cho sinh viên "${sv.hoTen}" (MSSV: ${sv.mssv})?`,
      confirmText: 'Cấp lại mật khẩu',
      cancelText: 'Hủy bỏ',
      type: 'primary',
    });
    if (!confirmed) {
      return;
    }
    this.http.post(`http://localhost:8080/api/sinh-vien/${sv.id}/reset-password`, {}, { responseType: 'text' }).subscribe({
      next: (pw) => {
        this.confirmDialog.alert({
          title: 'Cấp lại mật khẩu thành công',
          message: `Tài khoản (MSSV): ${sv.mssv}\nMật khẩu đăng nhập mới: ${pw}\n\n(Quy tắc: Ngày sinh dạng ddMMyyyy, hoặc 'student123' nếu không có ngày sinh).`,
          confirmText: 'Đã sao chép & ghi nhớ',
          type: 'success',
        });
        this.toastService.success(`Đã cấp lại mật khẩu cho sinh viên ${sv.mssv}`, 'Đặt lại mật khẩu');
      },
      error: () => {
        this.toastService.error('Không thể đặt lại mật khẩu cho sinh viên này. Vui lòng kiểm tra lại!', 'Lỗi');
      },
    });
  }
}

import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { ChangeDetectorRef, Component, inject, OnInit } from '@angular/core';
import { FormBuilder, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { PaginationComponent } from '../../../components/pagination/pagination.component';
import { ToastService } from '../../../services/toast.service';
import { ConfirmDialogService } from '../../../services/confirm-dialog.service';

export type LopItem = {
  id: number;
  maLop: string;
  tenLop: string;
  nienKhoa: string;
  siSoToiDa: number;
  khoaId: number;
  khoaTen?: string;
  active: boolean;
};

export type KhoaOption = {
  id: number;
  maKhoa: string;
  tenKhoa: string;
};

@Component({
  selector: 'app-admin-lop',
  imports: [CommonModule, FormsModule, ReactiveFormsModule, PaginationComponent],
  templateUrl: './admin-lop.component.html',
  styleUrl: './admin-lop.component.scss',
})
export class AdminLopComponent implements OnInit {
  private readonly http = inject(HttpClient);
  private readonly fb = inject(FormBuilder);
  private readonly cd = inject(ChangeDetectorRef);
  private readonly toastService = inject(ToastService);
  private readonly confirmDialog = inject(ConfirmDialogService);

  protected lops: LopItem[] = [];
  protected khoas: KhoaOption[] = [];

  protected searchText = '';
  protected filterKhoaId = '';

  protected currentPage = 1;
  protected pageSize = 15;

  protected editingId: number | null = null;
  protected loading = false;
  protected saving = false;
  protected errorMessage = '';
  protected successMessage = '';

  protected readonly form = this.fb.group({
    maLop: [''],
    tenLop: ['', [Validators.required, Validators.maxLength(150)]],
    nienKhoa: ['2023-2027', [Validators.required, Validators.maxLength(20)]],
    siSoToiDa: [45, [Validators.required, Validators.min(1)]],
    khoaId: ['', Validators.required],
    active: [true],
  });

  ngOnInit(): void {
    this.loadOptions();
    this.loadLops();
  }

  protected loadOptions(): void {
    this.http.get<KhoaOption[]>('http://localhost:8080/api/khoa').subscribe({
      next: (res) => {
        this.khoas = res;
        this.cd.detectChanges();
      },
    });
  }

  protected loadLops(): void {
    this.loading = true;
    this.errorMessage = '';
    this.http.get<LopItem[]>('http://localhost:8080/api/lop').subscribe({
      next: (res) => {
        this.lops = res;
        this.loading = false;
        this.cd.detectChanges();
      },
      error: () => {
        this.errorMessage = 'Không thể tải danh sách lớp học.';
        this.loading = false;
        this.cd.detectChanges();
      },
    });
  }

  protected get filteredLops(): LopItem[] {
    const q = this.searchText.trim().toLowerCase();
    return this.lops.filter((item) => {
      const matchSearch =
        !q ||
        item.maLop.toLowerCase().includes(q) ||
        item.tenLop.toLowerCase().includes(q);
      const matchKhoa = !this.filterKhoaId || String(item.khoaId) === this.filterKhoaId;
      return matchSearch && matchKhoa;
    });
  }

  protected get paginatedLops(): LopItem[] {
    const start = (this.currentPage - 1) * this.pageSize;
    return this.filteredLops.slice(start, start + this.pageSize);
  }

  protected isControlInvalid(controlName: string): boolean {
    const ctrl = this.form.get(controlName);
    if (!ctrl) return false;
    const isRequired = ctrl.hasValidator(Validators.required);
    const isEmpty = !ctrl.value || (typeof ctrl.value === 'string' && !ctrl.value.trim());
    return (ctrl.invalid || (isRequired && isEmpty)) && (ctrl.touched || ctrl.dirty);
  }

  protected submit(): void {
    const val = this.form.getRawValue();
    const tenLop = val.tenLop?.trim();
    const khoaId = val.khoaId;

    if (!tenLop || !khoaId) {
      this.form.markAllAsTouched();
      this.toastService.warning('Vui lòng chọn Khoa trực thuộc và nhập Tên lớp (các trường viền đỏ).', 'Thiếu thông tin bắt buộc');
      this.cd.detectChanges();
      return;
    }

    if (this.form.invalid) {
      this.form.markAllAsTouched();
      this.toastService.warning('Vui lòng kiểm tra lại thông tin không hợp lệ.', 'Dữ liệu chưa đúng');
      this.cd.detectChanges();
      return;
    }

    const payload = {
      maLop: val.maLop?.trim(),
      tenLop: tenLop,
      nienKhoa: val.nienKhoa?.trim(),
      siSoToiDa: Number(val.siSoToiDa),
      khoaId: Number(khoaId),
      active: val.active ?? true,
    };

    this.saving = true;

    const req = this.editingId === null
      ? this.http.post<LopItem>('http://localhost:8080/api/lop', payload)
      : this.http.put<LopItem>(`http://localhost:8080/api/lop/${this.editingId}`, payload);

    req.subscribe({
      next: () => {
        this.saving = false;
        const msg = this.editingId === null ? 'Thêm mới lớp học thành công!' : 'Cập nhật lớp học thành công!';
        this.toastService.success(msg, 'Quản lý Lớp');
        this.loadLops();
        this.resetForm();
      },
      error: (err) => {
        this.saving = false;
        const msg = this.toastService.extractError(err, 'Không thể lưu lớp học.');
        this.toastService.error(msg, 'Lỗi lưu lớp');
        this.cd.detectChanges();
      },
    });
  }

  protected edit(item: LopItem): void {
    this.editingId = item.id;
    this.form.setValue({
      maLop: item.maLop,
      tenLop: item.tenLop,
      nienKhoa: item.nienKhoa,
      siSoToiDa: item.siSoToiDa,
      khoaId: String(item.khoaId),
      active: item.active,
    });
    this.cd.detectChanges();
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }

  protected async remove(item: LopItem): Promise<void> {
    const confirmed = await this.confirmDialog.confirm({
      title: 'Xác nhận xóa lớp',
      message: `Bạn có chắc muốn xóa lớp "${item.tenLop}" (${item.maLop})?\n(Lưu ý: Không thể xóa nếu lớp đã có sinh viên).`,
      confirmText: 'Xóa lớp',
      cancelText: 'Hủy bỏ',
      type: 'danger',
    });
    if (!confirmed) {
      return;
    }

    this.http.delete(`http://localhost:8080/api/lop/${item.id}`).subscribe({
      next: () => {
        this.lops = this.lops.filter((x) => x.id !== item.id);
        this.toastService.success(`Đã xóa lớp ${item.maLop} thành công.`, 'Xóa lớp');
        this.cd.detectChanges();
      },
      error: (err) => {
        const msg = this.toastService.extractError(err, 'Không thể xóa lớp học này (có thể đã có sinh viên thuộc lớp).');
        this.toastService.error(msg, 'Lỗi xóa lớp');
        this.cd.detectChanges();
      },
    });
  }

  protected resetForm(): void {
    this.editingId = null;
    this.form.reset({
      maLop: '',
      tenLop: '',
      nienKhoa: '2023-2027',
      siSoToiDa: 45,
      khoaId: '',
      active: true,
    });
    this.cd.detectChanges();
  }
}

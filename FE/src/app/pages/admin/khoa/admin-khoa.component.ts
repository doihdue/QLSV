import { CommonModule } from '@angular/common';
import { Component, HostListener, inject } from '@angular/core';
import { ChangeDetectorRef } from '@angular/core';
import { FormBuilder, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { Khoa, KhoaPayload, KhoaService } from '../../../services/khoa.service';
import { PaginationComponent } from '../../../components/pagination/pagination.component';
import { ToastService } from '../../../services/toast.service';
import { ConfirmDialogService } from '../../../services/confirm-dialog.service';

@Component({
  selector: 'app-admin-khoa',
  imports: [CommonModule, FormsModule, ReactiveFormsModule, PaginationComponent],
  templateUrl: './admin-khoa.component.html',
  styleUrl: './admin-khoa.component.scss',
})
export class AdminKhoaComponent {
  private readonly fb = inject(FormBuilder);
  private readonly khoaService = inject(KhoaService);
  private readonly changeDetector = inject(ChangeDetectorRef);
  private readonly toastService = inject(ToastService);
  private readonly confirmDialog = inject(ConfirmDialogService);

  protected readonly form = this.fb.nonNullable.group({
    maKhoa: ['', [Validators.required, Validators.maxLength(20)]],
    tenKhoa: ['', [Validators.required, Validators.maxLength(150)]],
    moTa: ['', [Validators.maxLength(500)]],
    active: [true],
  });

  protected khoas: Khoa[] = [];
  protected searchText = '';
  protected currentPage = 1;
  protected pageSize = 10;
  protected showModal = false;
  protected editingId: number | null = null;
  protected loading = false;
  protected saving = false;
  protected errorMessage = '';
  protected successMessage = '';

  constructor() {
    this.loadKhoas();
  }

  protected loadKhoas(): void {
    this.loading = true;
    this.errorMessage = '';
    this.khoaService.findAll().subscribe({
      next: (khoas) => {
        this.khoas = khoas;
        this.loading = false;
        this.changeDetector.detectChanges();
      },
      error: () => {
        this.errorMessage = 'Không thể tải danh sách khoa. Hãy kiểm tra BE đang chạy.';
        this.loading = false;
        this.changeDetector.detectChanges();
      },
    });
  }

  protected get filteredKhoas(): Khoa[] {
    const q = this.searchText.trim().toLowerCase();
    return this.khoas.filter((k) => {
      return (
        !q ||
        k.maKhoa.toLowerCase().includes(q) ||
        k.tenKhoa.toLowerCase().includes(q) ||
        (k.moTa && k.moTa.toLowerCase().includes(q))
      );
    });
  }

  protected get paginatedKhoas(): Khoa[] {
    const start = (this.currentPage - 1) * this.pageSize;
    return this.filteredKhoas.slice(start, start + this.pageSize);
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
    const maKhoa = val.maKhoa?.trim();
    const tenKhoa = val.tenKhoa?.trim();

    if (!maKhoa || !tenKhoa) {
      this.form.markAllAsTouched();
      this.toastService.warning('Vui lòng nhập đầy đủ Mã khoa và Tên khoa (các trường viền đỏ).', 'Thiếu thông tin bắt buộc');
      this.changeDetector.detectChanges();
      return;
    }

    if (this.form.invalid) {
      this.form.markAllAsTouched();
      this.toastService.warning('Vui lòng kiểm tra lại các trường thông tin không hợp lệ.', 'Dữ liệu chưa đúng');
      this.changeDetector.detectChanges();
      return;
    }

    const payload: KhoaPayload = {
      ...val,
      maKhoa: maKhoa,
      tenKhoa: tenKhoa,
      moTa: val.moTa?.trim() || '',
    };
    const request = this.editingId === null
      ? this.khoaService.create(payload)
      : this.khoaService.update(this.editingId, payload);

    this.saving = true;
    request.subscribe({
      next: (savedKhoa) => {
        this.saving = false;
        const msg = this.editingId === null ? 'Thêm mới Khoa thành công!' : 'Cập nhật Khoa thành công!';
        this.toastService.success(msg, 'Quản lý Khoa');
        this.khoas = this.editingId === null
          ? [...this.khoas, savedKhoa]
          : this.khoas.map((khoa) => (khoa.id === savedKhoa.id ? savedKhoa : khoa));
        this.closeModal();
        this.changeDetector.detectChanges();
      },
      error: (err) => {
        this.saving = false;
        const msg = this.toastService.extractError(err, 'Không thể lưu khoa. Kiểm tra mã khoa đã tồn tại.');
        this.toastService.error(msg, 'Lỗi lưu khoa');
        this.changeDetector.detectChanges();
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
    this.changeDetector.detectChanges();
  }

  protected openEditModal(khoa: Khoa): void {
    this.editingId = khoa.id;
    this.form.setValue({
      maKhoa: khoa.maKhoa,
      tenKhoa: khoa.tenKhoa,
      moTa: khoa.moTa ?? '',
      active: khoa.active,
    });
    this.showModal = true;
    this.changeDetector.detectChanges();
  }

  protected closeModal(): void {
    this.showModal = false;
    this.resetForm();
  }

  protected edit(khoa: Khoa): void {
    this.openEditModal(khoa);
  }

  protected async remove(khoa: Khoa): Promise<void> {
    const confirmed = await this.confirmDialog.confirm({
      title: 'Xác nhận xóa khoa',
      message: `Bạn có chắc muốn xóa khoa "${khoa.tenKhoa}" (${khoa.maKhoa})?\n(Lưu ý: Không thể xóa nếu còn lớp hoặc môn học trực thuộc).`,
      confirmText: 'Xóa khoa',
      cancelText: 'Hủy bỏ',
      type: 'danger',
    });
    if (!confirmed) {
      return;
    }

    this.khoaService.delete(khoa.id).subscribe({
      next: () => {
        this.khoas = this.khoas.filter((item) => item.id !== khoa.id);
        this.toastService.success(`Đã xóa khoa ${khoa.maKhoa} thành công.`, 'Xóa khoa');
        this.changeDetector.detectChanges();
      },
      error: (err) => {
        const msg = this.toastService.extractError(err, 'Không thể xóa khoa này (có thể có lớp hoặc môn học đang trực thuộc khoa).');
        this.toastService.error(msg, 'Lỗi xóa khoa');
        this.changeDetector.detectChanges();
      },
    });
  }

  protected resetForm(): void {
    this.editingId = null;
    this.form.reset({ maKhoa: '', tenKhoa: '', moTa: '', active: true });
    this.changeDetector.detectChanges();
  }
}

export { AdminKhoaComponent as KhoaComponent };

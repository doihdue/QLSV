import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { ChangeDetectorRef, Component, HostListener, inject, OnInit } from '@angular/core';
import { FormBuilder, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { PaginationComponent } from '../../../components/pagination/pagination.component';
import { ToastService } from '../../../services/toast.service';
import { ConfirmDialogService } from '../../../services/confirm-dialog.service';

export type MonHocItem = {
  id: number;
  maMonHoc: string;
  tenMonHoc: string;
  soTinChi: number;
  soTietLyThuyet: number;
  soTietThucHanh: number;
  moTa?: string;
  monHocTienQuyet?: string;
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
  selector: 'app-admin-mon-hoc',
  imports: [CommonModule, FormsModule, ReactiveFormsModule, PaginationComponent],
  templateUrl: './admin-mon-hoc.component.html',
  styleUrl: './admin-mon-hoc.component.scss',
})
export class AdminMonHocComponent implements OnInit {
  private readonly http = inject(HttpClient);
  private readonly fb = inject(FormBuilder);
  private readonly cd = inject(ChangeDetectorRef);
  private readonly toastService = inject(ToastService);
  private readonly confirmDialog = inject(ConfirmDialogService);

  protected monHocs: MonHocItem[] = [];
  protected khoas: KhoaOption[] = [];

  protected searchText = '';
  protected filterKhoaId = '';
  protected filterCredits = '';

  protected currentPage = 1;
  protected pageSize = 15;

  protected showModal = false;
  protected editingId: number | null = null;
  protected loading = false;
  protected saving = false;
  protected errorMessage = '';
  protected successMessage = '';

  protected readonly form = this.fb.group({
    maMonHoc: ['', [Validators.required, Validators.maxLength(20)]],
    tenMonHoc: ['', [Validators.required, Validators.maxLength(150)]],
    soTinChi: [3, [Validators.required, Validators.min(1)]],
    soTietLyThuyet: [30, [Validators.required, Validators.min(0)]],
    soTietThucHanh: [15, [Validators.required, Validators.min(0)]],
    khoaId: ['', Validators.required],
    monHocTienQuyet: [''],
    moTa: [''],
    active: [true],
  });

  ngOnInit(): void {
    this.loadKhoas();
    this.loadMonHocs();
  }

  protected loadKhoas(): void {
    this.http.get<KhoaOption[]>('http://localhost:8080/api/khoa').subscribe({
      next: (res) => {
        this.khoas = res;
        this.cd.detectChanges();
      },
    });
  }

  protected loadMonHocs(): void {
    this.loading = true;
    this.errorMessage = '';
    this.http.get<MonHocItem[]>('http://localhost:8080/api/mon-hoc').subscribe({
      next: (res) => {
        this.monHocs = res;
        this.loading = false;
        this.cd.detectChanges();
      },
      error: () => {
        this.errorMessage = 'Không thể tải danh mục môn học.';
        this.loading = false;
        this.cd.detectChanges();
      },
    });
  }

  protected get filteredMonHocs(): MonHocItem[] {
    const q = this.searchText.trim().toLowerCase();
    return this.monHocs.filter((item) => {
      const matchSearch =
        !q ||
        item.maMonHoc.toLowerCase().includes(q) ||
        item.tenMonHoc.toLowerCase().includes(q) ||
        (item.monHocTienQuyet && item.monHocTienQuyet.toLowerCase().includes(q));
      const matchKhoa = !this.filterKhoaId || String(item.khoaId) === this.filterKhoaId;
      const matchCredits = !this.filterCredits || String(item.soTinChi) === this.filterCredits;
      return matchSearch && matchKhoa && matchCredits;
    });
  }

  protected get paginatedMonHocs(): MonHocItem[] {
    const start = (this.currentPage - 1) * this.pageSize;
    return this.filteredMonHocs.slice(start, start + this.pageSize);
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
    const maMon = val.maMonHoc?.trim();
    const tenMon = val.tenMonHoc?.trim();
    const khoaId = val.khoaId;

    if (!maMon || !tenMon || !khoaId) {
      this.form.markAllAsTouched();
      this.toastService.warning('Vui lòng điền đầy đủ các thông tin bắt buộc của môn học (các trường viền đỏ).', 'Thiếu thông tin bắt buộc');
      this.cd.detectChanges();
      return;
    }

    if (this.form.invalid) {
      this.form.markAllAsTouched();
      this.toastService.warning('Vui lòng kiểm tra lại các trường thông tin không hợp lệ.', 'Dữ liệu chưa đúng');
      this.cd.detectChanges();
      return;
    }

    const payload = {
      maMonHoc: maMon,
      tenMonHoc: tenMon,
      soTinChi: Number(val.soTinChi),
      soTietLyThuyet: Number(val.soTietLyThuyet),
      soTietThucHanh: Number(val.soTietThucHanh),
      khoaId: Number(val.khoaId),
      monHocTienQuyet: val.monHocTienQuyet?.trim() || null,
      moTa: val.moTa?.trim() || null,
      active: val.active ?? true,
    };

    this.saving = true;

    const req = this.editingId === null
      ? this.http.post<MonHocItem>('http://localhost:8080/api/mon-hoc', payload)
      : this.http.put<MonHocItem>(`http://localhost:8080/api/mon-hoc/${this.editingId}`, payload);

    req.subscribe({
      next: () => {
        this.saving = false;
        const msg = this.editingId === null
          ? 'Thêm mới môn học thành công!'
          : 'Cập nhật môn học thành công!';
        this.toastService.success(msg, 'Quản lý Môn học');
        this.loadMonHocs();
        this.closeModal();
      },
      error: (err) => {
        this.saving = false;
        const msg = this.toastService.extractError(err, 'Không thể lưu môn học. Kiểm tra mã môn học.');
        this.toastService.error(msg, 'Lỗi lưu môn học');
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

  protected openEditModal(item: MonHocItem): void {
    this.editingId = item.id;
    this.form.setValue({
      maMonHoc: item.maMonHoc,
      tenMonHoc: item.tenMonHoc,
      soTinChi: item.soTinChi,
      soTietLyThuyet: item.soTietLyThuyet,
      soTietThucHanh: item.soTietThucHanh,
      khoaId: String(item.khoaId),
      monHocTienQuyet: item.monHocTienQuyet || '',
      moTa: item.moTa || '',
      active: item.active,
    });
    this.showModal = true;
    this.cd.detectChanges();
  }

  protected closeModal(): void {
    this.showModal = false;
    this.resetForm();
  }

  protected edit(item: MonHocItem): void {
    this.openEditModal(item);
  }

  protected async remove(item: MonHocItem): Promise<void> {
    const confirmed = await this.confirmDialog.confirm({
      title: 'Xác nhận xóa môn học',
      message: `Bạn có chắc muốn xóa môn học "${item.tenMonHoc}" (${item.maMonHoc})?`,
      confirmText: 'Xóa môn học',
      cancelText: 'Hủy bỏ',
      type: 'danger',
    });
    if (!confirmed) {
      return;
    }

    this.http.delete(`http://localhost:8080/api/mon-hoc/${item.id}`).subscribe({
      next: () => {
        this.monHocs = this.monHocs.filter((x) => x.id !== item.id);
        this.toastService.success(`Đã xóa môn học ${item.maMonHoc} thành công.`, 'Xóa môn học');
        this.cd.detectChanges();
      },
      error: (err) => {
        const msg = this.toastService.extractError(err, 'Không thể xóa môn học này.');
        this.toastService.error(msg, 'Lỗi xóa môn học');
        this.cd.detectChanges();
      },
    });
  }

  protected resetForm(): void {
    this.editingId = null;
    this.form.reset({
      maMonHoc: '',
      tenMonHoc: '',
      soTinChi: 3,
      soTietLyThuyet: 30,
      soTietThucHanh: 15,
      khoaId: '',
      monHocTienQuyet: '',
      moTa: '',
      active: true,
    });
    this.cd.detectChanges();
  }
}

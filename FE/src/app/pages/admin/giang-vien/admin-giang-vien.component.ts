import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { ChangeDetectorRef, Component, HostListener, inject, OnInit } from '@angular/core';
import { FormBuilder, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { PaginationComponent } from '../../../components/pagination/pagination.component';
import { ToastService } from '../../../services/toast.service';
import { ConfirmDialogService } from '../../../services/confirm-dialog.service';

export type GiangVienItem = {
  id: number;
  maGiangVien: string;
  hoTen: string;
  email: string;
  soDienThoai?: string;
  hocVi?: string;
  chuyenMon?: string;
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
  selector: 'app-admin-giang-vien',
  imports: [CommonModule, FormsModule, ReactiveFormsModule, PaginationComponent],
  templateUrl: './admin-giang-vien.component.html',
  styleUrl: './admin-giang-vien.component.scss',
})
export class AdminGiangVienComponent implements OnInit {
  private readonly http = inject(HttpClient);
  private readonly fb = inject(FormBuilder);
  private readonly cd = inject(ChangeDetectorRef);
  private readonly toastService = inject(ToastService);
  private readonly confirmDialog = inject(ConfirmDialogService);

  protected giangViens: GiangVienItem[] = [];
  protected khoas: KhoaOption[] = [];

  protected searchText = '';
  protected filterKhoaId = '';
  protected filterHocVi = '';

  protected currentPage = 1;
  protected pageSize = 15;

  protected readonly hocViOptions = ['Cử nhân', 'Kỹ sư', 'Thạc sĩ', 'Tiến sĩ', 'Phó Giáo sư', 'Giáo sư'];

  protected showModal = false;
  protected editingId: number | null = null;
  protected loading = false;
  protected saving = false;
  protected errorMessage = '';
  protected successMessage = '';

  protected readonly form = this.fb.group({
    maGiangVien: [''],
    hoTen: ['', [Validators.required, Validators.maxLength(150)]],
    email: ['', [Validators.pattern(/^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/), Validators.maxLength(150)]],
    soDienThoai: ['', [Validators.pattern(/^(0|\+84)[0-9]{9}$/), Validators.maxLength(20)]],
    hocVi: ['Thạc sĩ', [Validators.required]],
    chuyenMon: ['', [Validators.maxLength(150)]],
    khoaId: ['', Validators.required],
    active: [true],
  });

  ngOnInit(): void {
    this.loadKhoas();
    this.loadGiangViens();
  }

  protected loadKhoas(): void {
    this.http.get<KhoaOption[]>('http://localhost:8080/api/khoa').subscribe({
      next: (res) => {
        this.khoas = res;
        this.cd.detectChanges();
      },
    });
  }

  protected loadGiangViens(): void {
    this.loading = true;
    this.errorMessage = '';
    this.http.get<GiangVienItem[]>('http://localhost:8080/api/giang-vien').subscribe({
      next: (res) => {
        this.giangViens = res;
        this.loading = false;
        this.cd.detectChanges();
      },
      error: () => {
        this.errorMessage = 'Không thể tải danh sách giảng viên.';
        this.loading = false;
        this.cd.detectChanges();
      },
    });
  }

  protected get filteredGiangViens(): GiangVienItem[] {
    const q = this.searchText.trim().toLowerCase();
    return this.giangViens.filter((item) => {
      const matchSearch =
        !q ||
        item.maGiangVien.toLowerCase().includes(q) ||
        item.hoTen.toLowerCase().includes(q) ||
        item.email.toLowerCase().includes(q) ||
        (item.chuyenMon && item.chuyenMon.toLowerCase().includes(q));
      const matchKhoa = !this.filterKhoaId || String(item.khoaId) === this.filterKhoaId;
      const matchHocVi = !this.filterHocVi || item.hocVi === this.filterHocVi;
      return matchSearch && matchKhoa && matchHocVi;
    });
  }

  protected get paginatedGiangViens(): GiangVienItem[] {
    const start = (this.currentPage - 1) * this.pageSize;
    return this.filteredGiangViens.slice(start, start + this.pageSize);
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
    const hoTen = val.hoTen?.trim();
    const khoaId = val.khoaId;

    if (!khoaId || !hoTen) {
      this.form.markAllAsTouched();
      if (!khoaId && !hoTen) {
        this.toastService.warning('Vui lòng chọn Khoa trực thuộc và nhập Họ tên giảng viên (các trường viền đỏ).', 'Thiếu thông tin bắt buộc');
      } else if (!khoaId) {
        this.toastService.warning('Vui lòng chọn Khoa trực thuộc cho giảng viên.', 'Thiếu thông tin');
      } else {
        this.toastService.warning('Vui lòng nhập Họ và tên giảng viên.', 'Thiếu thông tin');
      }
      this.cd.detectChanges();
      return;
    }

    if (this.form.invalid) {
      this.form.markAllAsTouched();
      if (this.form.get('email')?.invalid) {
        this.toastService.warning('Email không đúng định dạng (VD: giangvien@stu.edu.vn).', 'Email không hợp lệ');
      } else if (this.form.get('soDienThoai')?.invalid) {
        this.toastService.warning('Số điện thoại không đúng định dạng (gồm 10 chữ số, VD: 0901234567 hoặc +84901234567).', 'Số điện thoại không hợp lệ');
      } else {
        this.toastService.warning('Vui lòng kiểm tra lại thông tin không hợp lệ (các trường viền đỏ).', 'Dữ liệu chưa đúng');
      }
      this.cd.detectChanges();
      return;
    }

    const payload = {
      maGiangVien: this.editingId === null ? undefined : (val.maGiangVien?.trim() || undefined),
      hoTen: hoTen,
      email: val.email?.trim() || undefined,
      soDienThoai: val.soDienThoai?.trim() || null,
      hocVi: val.hocVi?.trim(),
      chuyenMon: val.chuyenMon?.trim() || null,
      khoaId: Number(val.khoaId),
      active: val.active ?? true,
    };

    this.saving = true;

    const req = this.editingId === null
      ? this.http.post<GiangVienItem>('http://localhost:8080/api/giang-vien', payload)
      : this.http.put<GiangVienItem>(`http://localhost:8080/api/giang-vien/${this.editingId}`, payload);

    req.subscribe({
      next: () => {
        this.saving = false;
        const msg = this.editingId === null
          ? 'Thêm mới giảng viên thành công!'
          : 'Cập nhật giảng viên thành công!';
        this.toastService.success(msg, 'Quản lý Giảng viên');
        this.loadGiangViens();
        this.closeModal();
      },
      error: (err) => {
        this.saving = false;
        const errorMsg = this.toastService.extractError(err, 'Không thể lưu giảng viên. Kiểm tra lại thông tin nhập.');
        this.toastService.error(errorMsg, 'Lỗi lưu giảng viên');
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

  protected openEditModal(item: GiangVienItem): void {
    this.editingId = item.id;
    this.form.setValue({
      maGiangVien: item.maGiangVien,
      hoTen: item.hoTen,
      email: item.email,
      soDienThoai: item.soDienThoai || '',
      hocVi: item.hocVi || 'Thạc sĩ',
      chuyenMon: item.chuyenMon || '',
      khoaId: String(item.khoaId),
      active: item.active,
    });
    this.showModal = true;
    this.cd.detectChanges();
  }

  protected closeModal(): void {
    this.showModal = false;
    this.resetForm();
  }

  protected edit(item: GiangVienItem): void {
    this.openEditModal(item);
  }

  protected async remove(item: GiangVienItem): Promise<void> {
    const confirmed = await this.confirmDialog.confirm({
      title: 'Xác nhận xóa giảng viên',
      message: `Bạn có chắc muốn xóa giảng viên "${item.hoTen}" (${item.maGiangVien})?\n(Lưu ý: Không thể xóa nếu giảng viên đang có lớp giảng dạy hoặc cố vấn).`,
      confirmText: 'Xóa giảng viên',
      cancelText: 'Hủy bỏ',
      type: 'danger',
    });
    if (!confirmed) {
      return;
    }

    this.http.delete(`http://localhost:8080/api/giang-vien/${item.id}`).subscribe({
      next: () => {
        this.giangViens = this.giangViens.filter((x) => x.id !== item.id);
        this.toastService.success(`Đã xóa giảng viên ${item.maGiangVien} thành công.`, 'Xóa giảng viên');
        this.cd.detectChanges();
      },
      error: (err) => {
        const msg = this.toastService.extractError(err, 'Không thể xóa giảng viên này (có thể đang là cố vấn học tập cho một lớp).');
        this.toastService.error(msg, 'Lỗi xóa giảng viên');
        this.cd.detectChanges();
      },
    });
  }

  protected resetForm(): void {
    this.editingId = null;
    this.form.reset({
      maGiangVien: '',
      hoTen: '',
      email: '',
      soDienThoai: '',
      hocVi: 'Thạc sĩ',
      chuyenMon: '',
      khoaId: '',
      active: true,
    });
    this.cd.detectChanges();
  }
}

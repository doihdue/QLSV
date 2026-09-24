import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { ChangeDetectorRef, Component, inject, OnInit } from '@angular/core';
import { FormBuilder, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';

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
  imports: [CommonModule, FormsModule, ReactiveFormsModule],
  templateUrl: './admin-sinh-vien.component.html',
  styleUrl: './admin-sinh-vien.component.scss',
})
export class AdminSinhVienComponent implements OnInit {
  private readonly http = inject(HttpClient);
  private readonly fb = inject(FormBuilder);
  private readonly cd = inject(ChangeDetectorRef);

  protected sinhViens: SinhVienItem[] = [];
  protected lops: LopOption[] = [];

  protected searchText = '';
  protected filterLopId = '';
  protected filterGender = '';

  protected editingId: number | null = null;
  protected loading = false;
  protected saving = false;
  protected errorMessage = '';
  protected successMessage = '';

  protected readonly form = this.fb.group({
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
  });

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

  protected submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const val = this.form.getRawValue();
    const payload = {
      mssv: this.editingId === null ? undefined : (val.mssv?.trim() || undefined),
      hoTen: val.hoTen?.trim(),
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
    this.errorMessage = '';
    this.successMessage = '';

    const req = this.editingId === null
      ? this.http.post<SinhVienItem>('http://localhost:8080/api/sinh-vien', payload)
      : this.http.put<SinhVienItem>(`http://localhost:8080/api/sinh-vien/${this.editingId}`, payload);

    req.subscribe({
      next: () => {
        this.saving = false;
        this.successMessage = this.editingId === null
          ? 'Thêm mới sinh viên thành công! Tài khoản đăng nhập đã được tạo (mật khẩu mặc định: ngày sinh ddMMyyyy).'
          : 'Cập nhật thông tin sinh viên thành công!';
        this.loadSinhViens();
        this.resetForm();
      },
      error: (err) => {
        this.saving = false;
        this.errorMessage = err?.error?.message || 'Không thể lưu sinh viên. Kiểm tra MSSV và email có bị trùng không.';
        this.cd.detectChanges();
      },
    });
  }

  protected edit(item: SinhVienItem): void {
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
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }

  protected remove(item: SinhVienItem): void {
    if (!confirm(`Bạn có chắc muốn xóa sinh viên ${item.hoTen} (MSSV: ${item.mssv})? Toàn bộ đăng ký môn và điểm liên quan cũng sẽ bị xóa.`)) {
      return;
    }

    this.http.delete(`http://localhost:8080/api/sinh-vien/${item.id}`).subscribe({
      next: () => {
        this.sinhViens = this.sinhViens.filter((x) => x.id !== item.id);
        this.successMessage = `Đã xóa sinh viên ${item.mssv} thành công.`;
        this.cd.detectChanges();
      },
      error: () => {
        this.errorMessage = 'Không thể xóa sinh viên này.';
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

  protected resetPassword(sv: SinhVienItem): void {
    if (!confirm(`Bạn có chắc muốn cấp lại mật khẩu đăng nhập cho sinh viên ${sv.hoTen} (${sv.mssv})?`)) {
      return;
    }
    this.http.post(`http://localhost:8080/api/sinh-vien/${sv.id}/reset-password`, {}, { responseType: 'text' }).subscribe({
      next: (pw) => {
        alert(`Đã cấp lại mật khẩu thành công!\n\nTài khoản (MSSV): ${sv.mssv}\nMật khẩu đăng nhập: ${pw}\n\n(Quy tắc: Ngày sinh dạng ddMMyyyy, hoặc 'student123' nếu không có ngày sinh)`);
      },
      error: () => {
        alert('Không thể đặt lại mật khẩu cho sinh viên này. Vui lòng kiểm tra lại!');
      },
    });
  }
}

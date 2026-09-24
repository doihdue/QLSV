import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { ChangeDetectorRef, Component, inject, OnInit } from '@angular/core';
import { FormBuilder, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';

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
  imports: [CommonModule, FormsModule, ReactiveFormsModule],
  templateUrl: './admin-giang-vien.component.html',
  styleUrl: './admin-giang-vien.component.scss',
})
export class AdminGiangVienComponent implements OnInit {
  private readonly http = inject(HttpClient);
  private readonly fb = inject(FormBuilder);
  private readonly cd = inject(ChangeDetectorRef);

  protected giangViens: GiangVienItem[] = [];
  protected khoas: KhoaOption[] = [];

  protected searchText = '';
  protected filterKhoaId = '';
  protected filterHocVi = '';

  protected readonly hocViOptions = ['Cử nhân', 'Kỹ sư', 'Thạc sĩ', 'Tiến sĩ', 'Phó Giáo sư', 'Giáo sư'];

  protected editingId: number | null = null;
  protected loading = false;
  protected saving = false;
  protected errorMessage = '';
  protected successMessage = '';

  protected readonly form = this.fb.group({
    maGiangVien: [''],
    hoTen: ['', [Validators.required, Validators.maxLength(150)]],
    email: [''],
    soDienThoai: ['', [Validators.maxLength(20)]],
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

  protected submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const val = this.form.getRawValue();
    const payload = {
      maGiangVien: this.editingId === null ? undefined : (val.maGiangVien?.trim() || undefined),
      hoTen: val.hoTen?.trim(),
      email: this.editingId === null ? undefined : (val.email?.trim() || undefined),
      soDienThoai: val.soDienThoai?.trim() || null,
      hocVi: val.hocVi?.trim(),
      chuyenMon: val.chuyenMon?.trim() || null,
      khoaId: Number(val.khoaId),
      active: val.active ?? true,
    };

    this.saving = true;
    this.errorMessage = '';
    this.successMessage = '';

    const req = this.editingId === null
      ? this.http.post<GiangVienItem>('http://localhost:8080/api/giang-vien', payload)
      : this.http.put<GiangVienItem>(`http://localhost:8080/api/giang-vien/${this.editingId}`, payload);

    req.subscribe({
      next: () => {
        this.saving = false;
        this.successMessage = this.editingId === null
          ? 'Thêm mới giảng viên thành công!'
          : 'Cập nhật giảng viên thành công!';
        this.loadGiangViens();
        this.resetForm();
      },
      error: (err) => {
        this.saving = false;
        this.errorMessage = err?.error?.message || 'Không thể lưu giảng viên. Kiểm tra mã GV và email.';
        this.cd.detectChanges();
      },
    });
  }

  protected edit(item: GiangVienItem): void {
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
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }

  protected remove(item: GiangVienItem): void {
    if (!confirm(`Bạn có chắc muốn xóa giảng viên ${item.hoTen} (${item.maGiangVien})?`)) {
      return;
    }

    this.http.delete(`http://localhost:8080/api/giang-vien/${item.id}`).subscribe({
      next: () => {
        this.giangViens = this.giangViens.filter((x) => x.id !== item.id);
        this.successMessage = `Đã xóa giảng viên ${item.maGiangVien} thành công.`;
        this.cd.detectChanges();
      },
      error: () => {
        this.errorMessage = 'Không thể xóa giảng viên này (có thể đang là cố vấn học tập cho một lớp).';
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
  }
}

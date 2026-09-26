import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { ChangeDetectorRef, Component, inject, OnInit } from '@angular/core';
import { FormBuilder, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { PaginationComponent } from '../../../components/pagination/pagination.component';

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

  protected monHocs: MonHocItem[] = [];
  protected khoas: KhoaOption[] = [];

  protected searchText = '';
  protected filterKhoaId = '';
  protected filterCredits = '';

  protected currentPage = 1;
  protected pageSize = 15;

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

  protected submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const val = this.form.getRawValue();
    const payload = {
      maMonHoc: val.maMonHoc?.trim(),
      tenMonHoc: val.tenMonHoc?.trim(),
      soTinChi: Number(val.soTinChi),
      soTietLyThuyet: Number(val.soTietLyThuyet),
      soTietThucHanh: Number(val.soTietThucHanh),
      khoaId: Number(val.khoaId),
      monHocTienQuyet: val.monHocTienQuyet?.trim() || null,
      moTa: val.moTa?.trim() || null,
      active: val.active ?? true,
    };

    this.saving = true;
    this.errorMessage = '';
    this.successMessage = '';

    const req = this.editingId === null
      ? this.http.post<MonHocItem>('http://localhost:8080/api/mon-hoc', payload)
      : this.http.put<MonHocItem>(`http://localhost:8080/api/mon-hoc/${this.editingId}`, payload);

    req.subscribe({
      next: () => {
        this.saving = false;
        this.successMessage = this.editingId === null
          ? 'Thêm mới môn học thành công!'
          : 'Cập nhật môn học thành công!';
        this.loadMonHocs();
        this.resetForm();
      },
      error: (err) => {
        this.saving = false;
        this.errorMessage = err?.error?.message || 'Không thể lưu môn học. Kiểm tra mã môn học đã tồn tại.';
        this.cd.detectChanges();
      },
    });
  }

  protected edit(item: MonHocItem): void {
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
    this.errorMessage = '';
    this.successMessage = '';
    this.cd.detectChanges();
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }

  protected remove(item: MonHocItem): void {
    if (!confirm(`Bạn có chắc muốn xóa môn học ${item.tenMonHoc} (${item.maMonHoc})?`)) {
      return;
    }

    this.http.delete(`http://localhost:8080/api/mon-hoc/${item.id}`).subscribe({
      next: () => {
        this.monHocs = this.monHocs.filter((x) => x.id !== item.id);
        this.successMessage = `Đã xóa môn học ${item.maMonHoc} thành công.`;
        this.cd.detectChanges();
      },
      error: () => {
        this.errorMessage = 'Không thể xóa môn học này (có thể đã có sinh viên đăng ký hoặc có liên kết điểm).';
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

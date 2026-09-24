import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { ChangeDetectorRef, Component, inject, OnInit } from '@angular/core';
import { FormBuilder, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { forkJoin, of } from 'rxjs';
import { catchError } from 'rxjs/operators';

import { MonHocMo } from '../../../services/student.service';

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
  imports: [CommonModule, FormsModule, ReactiveFormsModule],
  templateUrl: './admin-diem-dangky.component.html',
  styleUrl: './admin-diem-dangky.component.scss',
})
export class AdminDiemDangKyComponent implements OnInit {
  private readonly http = inject(HttpClient);
  private readonly fb = inject(FormBuilder);
  private readonly cd = inject(ChangeDetectorRef);

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

  // Tab: Quản lý đăng ký filters
  protected filterRegHocKy: string = '1';
  protected filterRegNamHoc: string = '2026-2027';
  protected filterRegMonHocId: string = '';
  protected searchRegStudent: string = '';

  // Tab: Mở môn theo Khóa & Lớp hành chính filters
  protected moNamHoc: string = '2026-2027';
  protected moHocKy: string = '1';
  protected moKhoaId: string = '';
  protected moKhoaHoc: string = '2023';
  protected moLopId: string = '';
  protected monHocMoList: MonHocMo[] = [];

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

  // Đợt đăng ký hiện tại
  protected currentDot: {
    id: number;
    hocKy: string;
    namHoc: string;
    tenDot: string;
    dangMo: boolean;
  } | null = null;

  protected showConfigDotModal = false;
  protected readonly configDotForm = this.fb.group({
    hocKy: ['1', Validators.required],
    namHoc: ['2026-2027', Validators.required],
    tenDot: [''],
    dangMo: [true],
  });

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
  }

  private sanitizeDot<T extends { hocKy: string; namHoc: string; tenDot: string } | null>(dot: T): T {
    if (!dot) return dot;
    if (!dot.tenDot || dot.tenDot.includes('?')) {
      dot.tenDot = `Đợt đăng ký tín chỉ Học kỳ ${dot.hocKy} (${dot.namHoc})`;
    }
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
    });
  }

  protected toggleDotOpen(): void {
    if (!this.currentDot) return;
    const newStatus = !this.currentDot.dangMo;
    this.http.patch<typeof this.currentDot>(`http://localhost:8080/api/dot-dang-ky/hien-tai/toggle?open=${newStatus}`, {}).subscribe({
      next: (updated) => {
        this.currentDot = this.sanitizeDot(updated);
        this.successMessage = newStatus ? 'Đã MỞ CỔNG đăng ký môn học cho sinh viên!' : 'Đã KHÓA CỔNG đăng ký môn học!';
        this.cd.detectChanges();
      },
      error: () => {
        this.errorMessage = 'Không thể cập nhật trạng thái cổng đăng ký.';
        this.cd.detectChanges();
      },
    });
  }

  protected openConfigDotModal(): void {
    if (this.currentDot) {
      this.configDotForm.patchValue({
        hocKy: this.currentDot.hocKy,
        namHoc: this.currentDot.namHoc,
        tenDot: this.currentDot.tenDot,
        dangMo: this.currentDot.dangMo,
      });
    }
    this.showConfigDotModal = true;
  }

  protected closeConfigDotModal(): void {
    this.showConfigDotModal = false;
  }

  protected saveConfigDot(): void {
    if (this.configDotForm.invalid) {
      this.configDotForm.markAllAsTouched();
      return;
    }

    const val = this.configDotForm.getRawValue();
    this.saving = true;
    this.errorMessage = '';
    this.successMessage = '';

    this.http.put<typeof this.currentDot>('http://localhost:8080/api/dot-dang-ky/hien-tai', val).subscribe({
      next: (updated) => {
        this.currentDot = this.sanitizeDot(updated);
        this.saving = false;
        this.showConfigDotModal = false;
        if (updated) {
          this.filterRegHocKy = updated.hocKy;
          this.filterRegNamHoc = updated.namHoc;
        }
        this.successMessage = 'Đã cập nhật Đợt đăng ký tín chỉ thành công!';
        this.cd.detectChanges();
      },
      error: () => {
        this.saving = false;
        this.errorMessage = 'Không thể lưu đợt đăng ký.';
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
    this.loadingPendingDetails = true;
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

  protected approvePendingClass(cls: any): void {
    const courseTitle = cls.monHocTen || cls.tenMonHoc;
    const classTitle = cls.maLop ? `${cls.tenLop} (${cls.maLop})` : (cls.tenLop || `Toàn khóa ${cls.khoaHoc || ''}`);

    if (!confirm(`Xác nhận DUYỆT CÔNG BỐ bảng điểm môn "${courseTitle}" (${classTitle})?\nĐiểm sẽ được công bố chính thức cho sinh viên tra cứu.`)) {
      return;
    }
    this.loadingPendingDetails = true;
    this.http.put(`http://localhost:8080/api/admin/duyet-diem/${cls.monHocMoId}/approve`, {}).subscribe({
      next: () => {
        this.successMessage = `Đã duyệt công bố bảng điểm môn ${courseTitle} thành công!`;
        this.selectedPendingClass = null;
        this.pendingClassStudents = [];
        this.loadPendingClasses();
        this.refreshAllData();
        this.cd.detectChanges();
      },
      error: (err) => {
        this.loadingPendingDetails = false;
        this.errorMessage = err?.error?.message || 'Không thể duyệt bảng điểm.';
        this.cd.detectChanges();
      },
    });
  }

  protected rejectPendingClass(cls: any): void {
    const courseTitle = cls.monHocTen || cls.tenMonHoc;
    const reason = prompt(`Nhập lý do từ chối bảng điểm môn "${courseTitle}" (để gửi phản hồi cho Giảng viên):`, 'Cần rà soát và kiểm tra lại điểm thành phần');
    if (reason === null) return;
    if (!reason.trim()) {
      alert('Vui lòng nhập lý do từ chối để giảng viên có thể chỉnh sửa.');
      return;
    }
    this.loadingPendingDetails = true;
    this.http.put(`http://localhost:8080/api/admin/duyet-diem/${cls.monHocMoId}/reject`, {
      ghiChu: reason.trim(),
      reason: reason.trim(),
    }).subscribe({
      next: () => {
        this.successMessage = `Đã từ chối bảng điểm môn ${courseTitle}. Giảng viên sẽ nhận được lý do để chỉnh sửa lại.`;
        this.selectedPendingClass = null;
        this.pendingClassStudents = [];
        this.loadPendingClasses();
        this.refreshAllData();
        this.cd.detectChanges();
      },
      error: (err) => {
        this.loadingPendingDetails = false;
        this.errorMessage = err?.error?.message || 'Không thể từ chối bảng điểm.';
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

  protected openAddRegistrationModal(): void {
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
  }

  protected saveRegistration(): void {
    if (this.addRegForm.invalid) {
      this.addRegForm.markAllAsTouched();
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
    this.errorMessage = '';
    this.successMessage = '';

    this.http.post<DangKyItem>('http://localhost:8080/api/dang-ky-mon-hoc', payload).subscribe({
      next: (savedReg) => {
        this.saving = false;
        this.closeAddRegistrationModal();
        this.successMessage = `Đã ghi danh thành công sinh viên vào môn ${savedReg.monHocTen}!`;
        this.allRegistrations = [savedReg, ...this.allRegistrations];
        this.cd.detectChanges();
      },
      error: (err) => {
        this.saving = false;
        this.errorMessage = err?.error?.message || 'Sinh viên này đã đăng ký môn học trong học kỳ này rồi.';
        this.cd.detectChanges();
      },
    });
  }

  // Ghi danh toàn bộ sinh viên lớp hành chính
  protected openBatchRegModal(): void {
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
  }

  protected saveBatchRegistration(): void {
    if (this.batchRegForm.invalid) {
      this.batchRegForm.markAllAsTouched();
      return;
    }
    const val = this.batchRegForm.getRawValue();
    const lop = this.lops.find((l) => String(l.id) === val.lopId);
    const mon = this.monHocs.find((m) => String(m.id) === val.monHocId);

    this.saving = true;
    this.errorMessage = '';
    this.successMessage = '';

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
          this.successMessage = `Đã ghi danh toàn bộ sinh viên Lớp "${lop?.tenLop}" vào môn học "${mon?.tenMonHoc}" thành công!`;
          this.cd.detectChanges();
        },
        error: () => {
          this.saving = false;
          this.errorMessage = 'Có lỗi xảy ra khi ghi danh lớp hành chính.';
          this.cd.detectChanges();
        },
      });
  }

  protected cancelRegistration(item: DangKyItem): void {
    if (!confirm(`Bạn có chắc muốn hủy đăng ký môn ${item.monHocTen} của sinh viên ${item.sinhVienHoTen} (${item.sinhVienMssv})?`)) {
      return;
    }

    this.http.delete(`http://localhost:8080/api/dang-ky-mon-hoc/${item.id}`).subscribe({
      next: () => {
        this.allRegistrations = this.allRegistrations.filter((r) => r.id !== item.id);
        this.successMessage = `Đã hủy môn ${item.monHocMa} cho sinh viên ${item.sinhVienMssv}.`;
        this.cd.detectChanges();
      },
      error: () => {
        this.errorMessage = 'Không thể hủy đăng ký môn học này.';
        this.cd.detectChanges();
      },
    });
  }

  // ==================== MỞ MÔN THEO KHÓA & PHÂN CÔNG THEO LỚP ====================
  protected loadMonHocMoList(): void {
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
    this.showAddMoModal = true;
  }

  protected closeAddMoModal(): void {
    this.showAddMoModal = false;
  }

  protected submitAddMo(): void {
    if (this.addMoForm.invalid) {
      this.addMoForm.markAllAsTouched();
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
    this.errorMessage = '';
    this.successMessage = '';

    this.http.post<MonHocMo>('http://localhost:8080/api/mon-hoc-mo', payload).subscribe({
      next: (res) => {
        this.saving = false;
        this.closeAddMoModal();
        this.successMessage = `Đã mở môn "${res.tenMonHoc}" cho toàn bộ các lớp thuộc Khóa ${res.khoaHoc} (${res.tenKhoa || ''})!`;
        this.loadMonHocMoList();
        this.cd.detectChanges();
      },
      error: (err) => {
        this.saving = false;
        this.errorMessage = err?.error?.message || 'Không thể mở môn học cho khóa này.';
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
        this.successMessage = updated.tenGiangVien
          ? `Đã phân công giảng viên ${updated.tenGiangVien} phụ trách môn ${updated.tenMonHoc}!`
          : `Đã hủy phân công giảng viên cho môn ${updated.tenMonHoc}!`;
        this.monHocMoList = this.monHocMoList.map((m) => (m.id === updated.id ? updated : m));
        this.cd.detectChanges();
      },
      error: () => {
        this.errorMessage = 'Không thể phân công giảng viên.';
        this.cd.detectChanges();
      },
    });
  }

  protected removeMonHocMo(item: MonHocMo): void {
    const isClassSpecific = !!item.tenLop;
    const msg = isClassSpecific
      ? `Bạn có chắc muốn hủy môn "${item.tenMonHoc}" (${item.monHocMa}) của lớp ${item.tenLop}?`
      : `Bạn có chắc muốn xóa môn "${item.tenMonHoc}" (${item.monHocMa}) khỏi danh mục mở chung của khóa ${item.khoaHoc}? (Tất cả các lớp trong khóa này cũng sẽ được hủy môn)`;

    if (!confirm(msg)) {
      return;
    }
    this.http.delete(`http://localhost:8080/api/mon-hoc-mo/${item.id}`).subscribe({
      next: () => {
        this.monHocMoList = this.monHocMoList.filter((m) => m.id !== item.id);
        this.successMessage = `Đã xóa môn "${item.tenMonHoc}".`;
        this.cd.detectChanges();
      },
      error: () => {
        this.errorMessage = 'Không thể xóa môn học khỏi kỳ mở.';
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

import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, DestroyRef, inject, OnInit } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormBuilder, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { forkJoin, of } from 'rxjs';
import { catchError } from 'rxjs/operators';
import {
  Course,
  DotDangKy,
  Grade,
  Registration,
  StudentProfile,
  StudentService,
} from '../../services/student.service';
import { PaginationComponent } from '../../components/pagination/pagination.component';

export type PortalMode = 'profile' | 'registration' | 'grades';

@Component({
  selector: 'app-student-portal',
  imports: [CommonModule, FormsModule, ReactiveFormsModule, RouterLink, PaginationComponent],
  templateUrl: './student-portal.component.html',
  styleUrl: './student-portal.component.scss',
})
export class StudentPortalComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly fb = inject(FormBuilder);
  private readonly studentService = inject(StudentService);
  private readonly destroyRef = inject(DestroyRef);
  private readonly cd = inject(ChangeDetectorRef);

  protected mode: PortalMode = 'profile';

  // Navigation tabs for student portal
  protected readonly navTabs = [
    { key: 'profile', label: 'Hồ sơ sinh viên', path: '/sinh-vien/ho-so-ca-nhan', icon: 'M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z' },
    { key: 'registration', label: 'Đăng ký môn học', path: '/sinh-vien/dang-ky-mon-hoc', icon: 'M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2' },
    { key: 'grades', label: 'Kết quả học tập', path: '/sinh-vien/bang-diem', icon: 'M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z' },
  ];

  // Semester options
  protected readonly semesters = [
    { id: '1', name: 'Học kỳ 1' },
    { id: '2', name: 'Học kỳ 2' },
    { id: '3', name: 'Học kỳ hè' },
  ];
  protected readonly academicYears = ['2026-2027', '2025-2026', '2024-2025'];

  // Registration states
  protected regHocKy = '1';
  protected regNamHoc = '2026-2027';
  protected courseSearch = '';
  protected selectedKhoaFilter = '';
  protected courses: Course[] = [];
  protected registrations: Registration[] = [];
  protected registeringId: number | null = null;
  protected pendingCancelRegistration: Registration | null = null;
  protected cancelling = false;
  protected coursePage = 1;
  protected coursePageSize = 10;

  // Grade states
  protected gradeHocKy = '';
  protected gradeNamHoc = '';
  protected grades: Grade[] = [];
  protected showGradingScale = false;
  protected gradePage = 1;
  protected gradePageSize = 15;

  // Profile forms
  protected profile: StudentProfile | null = null;
  protected readonly profileForm = this.fb.nonNullable.group({
    email: ['', [Validators.required, Validators.email]],
    soDienThoai: ['', [Validators.pattern(/^[0-9+() -]{8,20}$/)]],
    diaChi: [''],
  });

  protected readonly passwordForm = this.fb.nonNullable.group({
    oldPassword: ['', [Validators.required, Validators.minLength(6)]],
    newPassword: ['', [Validators.required, Validators.minLength(6)]],
    confirmPassword: ['', [Validators.required, Validators.minLength(6)]],
  });

  // Global UI feedback states
  protected loading = false;
  protected savingProfile = false;
  protected changingPassword = false;
  protected errorMessage = '';
  protected successMessage = '';

  // Đợt đăng ký hiện tại
  protected currentDot: DotDangKy | null = null;

  protected get studentCohort(): string {
    const mssv = this.profile?.mssv;
    if (!mssv) return '';
    const match = mssv.match(/^[A-Za-z]*(\d{2})/);
    return match ? match[1] : '';
  }

  /**
   * So sánh thứ tự giữa 2 kỳ học:
   * > 0: kỳ 1 ở tương lai so với kỳ 2 (VD: đang mở kỳ 1 mà chọn kỳ 2 hoặc kỳ hè cùng năm, hoặc năm sau)
   * < 0: kỳ 1 ở quá khứ so với kỳ 2 (VD: năm trước, hoặc kỳ 1 khi đang mở kỳ 2)
   * = 0: cùng một kỳ
   */
  protected compareSemester(nam1: string, hk1: string, nam2: string, hk2: string): number {
    const y1 = parseInt(nam1?.split('-')[0], 10) || 0;
    const y2 = parseInt(nam2?.split('-')[0], 10) || 0;
    if (y1 !== y2) {
      return y1 - y2;
    }
    const s1 = parseInt(hk1, 10) || 0;
    const s2 = parseInt(hk2, 10) || 0;
    return s1 - s2;
  }

  protected getSemesterStatus(namHoc: string, hocKy: string): 'OPEN' | 'NOT_OPEN_YET' | 'ENDED' | 'CLOSED' {
    if (!this.currentDot || !this.currentDot.dangMo) {
      return 'CLOSED';
    }
    const cmp = this.compareSemester(namHoc, hocKy, this.currentDot.namHoc, this.currentDot.hocKy);
    if (cmp === 0) {
      return 'OPEN';
    } else if (cmp > 0) {
      return 'NOT_OPEN_YET';
    } else {
      return 'ENDED';
    }
  }

  protected get currentSelectedSemesterStatus(): 'OPEN' | 'NOT_OPEN_YET' | 'ENDED' | 'CLOSED' {
    return this.getSemesterStatus(this.regNamHoc, this.regHocKy);
  }

  protected get isCurrentSemesterOpen(): boolean {
    return this.currentSelectedSemesterStatus === 'OPEN';
  }

  protected get isSemesterNotOpenYet(): boolean {
    return this.currentSelectedSemesterStatus === 'NOT_OPEN_YET';
  }

  protected get isPastSemester(): boolean {
    return this.currentSelectedSemesterStatus === 'ENDED';
  }

  protected get isSemesterPortalClosed(): boolean {
    return this.currentSelectedSemesterStatus === 'CLOSED';
  }

  protected getSemesterLabelWithStatus(semId: string): string {
    const sem = this.semesters.find((s) => s.id === semId);
    const baseName = sem ? sem.name : `Học kỳ ${semId}`;
    if (!this.currentDot) return baseName;

    const status = this.getSemesterStatus(this.regNamHoc, semId);
    if (status === 'OPEN') {
      return `${baseName} (Đang mở)`;
    } else if (status === 'NOT_OPEN_YET') {
      return `${baseName} (Chưa mở)`;
    } else if (status === 'ENDED') {
      return `${baseName} (Đã kết thúc)`;
    } else {
      return `${baseName} (Đã đóng)`;
    }
  }

  ngOnInit(): void {
    // Always load profile once for header identity badge
    this.loadProfile();
    this.loadDotDangKy();

    // Listen to route data reactively so tab clicks trigger proper re-fetch
    this.route.data.pipe(takeUntilDestroyed(this.destroyRef)).subscribe((data) => {
      this.mode = (data['mode'] as PortalMode) || 'profile';
      this.errorMessage = '';
      this.successMessage = '';

      if (this.mode === 'registration') {
        this.loadRegistrationData();
      } else if (this.mode === 'grades') {
        this.loadGrades();
      }
    });
  }

  protected loadDotDangKy(): void {
    this.studentService.getCurrentDotDangKy().subscribe({
      next: (dot) => {
        if (dot && (!dot.tenDot || dot.tenDot.includes('?'))) {
          dot.tenDot = `Đợt đăng ký tín chỉ Học kỳ ${dot.hocKy} (${dot.namHoc})`;
        }
        this.currentDot = dot;
        if (dot && dot.hocKy && dot.namHoc) {
          const changed = this.regHocKy !== dot.hocKy || this.regNamHoc !== dot.namHoc;
          this.regHocKy = dot.hocKy;
          this.regNamHoc = dot.namHoc;
          if (changed && this.mode === 'registration') {
            this.loadRegistrationData();
          }
        }
        this.cd.detectChanges();
      },
    });
  }

  // --- Profile methods ---
  protected loadProfile(): void {
    this.loading = true;
    this.studentService.getProfile().subscribe({
      next: (res) => {
        this.profile = res;
        this.profileForm.patchValue({
          email: res.email || '',
          soDienThoai: res.soDienThoai || '',
          diaChi: res.diaChi || '',
        });
        this.loading = false;
        this.cd.detectChanges();
      },
      error: () => {
        this.loading = false;
        this.errorMessage = 'Không thể tải thông tin hồ sơ sinh viên.';
        this.cd.detectChanges();
      },
    });
  }

  protected saveProfile(): void {
    if (this.profileForm.invalid) {
      this.profileForm.markAllAsTouched();
      return;
    }
    this.savingProfile = true;
    this.errorMessage = '';
    this.successMessage = '';

    this.studentService.updateProfile(this.profileForm.getRawValue()).subscribe({
      next: (updated) => {
        this.profile = updated;
        this.savingProfile = false;
        this.successMessage = 'Đã cập nhật thông tin liên hệ thành công.';
        this.cd.detectChanges();
      },
      error: (err) => {
        this.savingProfile = false;
        this.errorMessage = err?.error?.message || 'Không thể cập nhật hồ sơ. Vui lòng thử lại.';
        this.cd.detectChanges();
      },
    });
  }

  protected savePassword(): void {
    if (this.passwordForm.invalid) {
      this.passwordForm.markAllAsTouched();
      return;
    }
    const val = this.passwordForm.getRawValue();
    if (val.newPassword !== val.confirmPassword) {
      this.errorMessage = 'Mật khẩu mới và xác nhận mật khẩu không khớp.';
      return;
    }

    this.changingPassword = true;
    this.errorMessage = '';
    this.successMessage = '';

    this.studentService.changePassword({
      oldPassword: val.oldPassword,
      newPassword: val.newPassword,
    }).subscribe({
      next: () => {
        this.changingPassword = false;
        this.passwordForm.reset();
        this.successMessage = 'Đổi mật khẩu thành công. Hãy ghi nhớ mật khẩu mới của bạn.';
        this.cd.detectChanges();
      },
      error: (err) => {
        this.changingPassword = false;
        this.errorMessage = err?.error?.message || 'Mật khẩu cũ không đúng hoặc có lỗi xảy ra.';
        this.cd.detectChanges();
      },
    });
  }

  // --- Registration methods ---
  protected loadRegistrationData(): void {
    this.loading = true;
    this.coursePage = 1;
    this.errorMessage = '';

    forkJoin({
      moList: this.studentService.getCoursesForStudent(this.regHocKy, this.regNamHoc).pipe(catchError(() => of([]))),
      regs: this.studentService.getRegistrations(this.regHocKy, this.regNamHoc).pipe(catchError(() => of([]))),
    }).subscribe({
      next: ({ moList, regs }) => {
        this.registrations = regs;
        if (moList && moList.length > 0) {
          this.courses = moList.map((m) => ({
            id: m.monHocId,
            maMonHoc: m.monHocMa,
            tenMonHoc: m.tenMonHoc,
            soTinChi: m.soTinChi,
            soTietLyThuyet: m.soTietLyThuyet,
            soTietThucHanh: m.soTietThucHanh,
            moTa: m.ghiChu || '',
            monHocTienQuyet: m.monHocTienQuyet,
            khoaTen: m.tenKhoa,
            giangVienTen: m.tenGiangVien || 'Chưa phân công',
            tenLop: m.tenLop || (m.khoaHoc ? `Toàn khóa ${m.khoaHoc.replace(/\D+/g, '')}` : 'Chung'),
            maLop: m.maLop,
            monHocMoId: m.id,
          }));
        } else {
          this.courses = [];
        }
        this.loading = false;
        this.cd.detectChanges();
      },
      error: () => {
        this.loading = false;
        this.errorMessage = 'Không thể tải danh sách học phần và môn đã đăng ký.';
        this.cd.detectChanges();
      },
    });
  }

  protected onRegistrationSemesterChange(): void {
    this.loadRegistrationData();
  }

  protected isCourseRegistered(courseId: number): boolean {
    return this.registrations.some((r) => r.monHocId === courseId);
  }

  protected getCourseRegistration(courseId: number): Registration | undefined {
    return this.registrations.find((r) => r.monHocId === courseId);
  }

  protected registerCourse(course: Course): void {
    if (!this.isCurrentSemesterOpen) {
      if (this.isSemesterNotOpenYet) {
        this.errorMessage = `Học kỳ ${this.regHocKy} (${this.regNamHoc}) hiện chưa mở đợt đăng ký môn học.`;
      } else if (this.isPastSemester) {
        this.errorMessage = `Học kỳ ${this.regHocKy} (${this.regNamHoc}) đã kết thúc. Bạn không thể đăng ký thêm môn học.`;
      } else {
        this.errorMessage = 'Cổng đăng ký tín chỉ hiện đang đóng.';
      }
      return;
    }

    if (this.isCourseRegistered(course.id)) return;

    if (this.totalRegisteredCredits + course.soTinChi > 24) {
      this.errorMessage = `Vượt quá giới hạn tối đa 24 tín chỉ trong học kỳ (Hiện tại: ${this.totalRegisteredCredits} TC + ${course.soTinChi} TC).`;
      return;
    }

    this.registeringId = course.id;
    this.errorMessage = '';
    this.successMessage = '';

    this.studentService.register(course.id, this.regHocKy, this.regNamHoc).subscribe({
      next: (res) => {
        this.registrations = [res, ...this.registrations];
        this.registeringId = null;
        this.successMessage = `Đã đăng ký thành công môn học: ${course.tenMonHoc} (${course.soTinChi} TC).`;
        this.cd.detectChanges();
      },
      error: (err) => {
        this.registeringId = null;
        this.errorMessage = err?.error?.message || 'Không thể đăng ký môn học này. Vui lòng kiểm tra lại.';
        this.cd.detectChanges();
      },
    });
  }

  protected openCancelModal(reg: Registration): void {
    if (!this.isCurrentSemesterOpen) {
      if (this.isSemesterNotOpenYet) {
        this.errorMessage = 'Học kỳ này chưa mở đợt đăng ký môn học.';
      } else if (this.isPastSemester) {
        this.errorMessage = 'Không thể hủy môn học của học kỳ đã kết thúc.';
      } else {
        this.errorMessage = 'Cổng đăng ký tín chỉ hiện đang đóng.';
      }
      return;
    }
    this.pendingCancelRegistration = reg;
  }

  protected closeCancelModal(): void {
    this.pendingCancelRegistration = null;
  }

  protected confirmCancelRegistration(): void {
    if (!this.pendingCancelRegistration) return;
    if (!this.isCurrentSemesterOpen) {
      this.errorMessage = 'Không thể hủy môn học khi cổng đăng ký đã đóng hoặc học kỳ đã kết thúc.';
      this.pendingCancelRegistration = null;
      return;
    }
    const target = this.pendingCancelRegistration;
    this.cancelling = true;
    this.errorMessage = '';
    this.successMessage = '';

    this.studentService.cancelRegistration(target.id).subscribe({
      next: () => {
        this.registrations = this.registrations.filter((r) => r.id !== target.id);
        this.cancelling = false;
        this.pendingCancelRegistration = null;
        this.successMessage = `Đã hủy đăng ký môn ${target.monHocTen} thành công.`;
        this.cd.detectChanges();
      },
      error: (err) => {
        this.cancelling = false;
        this.pendingCancelRegistration = null;
        this.errorMessage = err?.error?.message || 'Không thể hủy môn học. Có thể môn đã được ghi điểm.';
        this.cd.detectChanges();
      },
    });
  }

  // --- Grade methods ---
  protected loadGrades(): void {
    this.loading = true;
    this.gradePage = 1;
    this.studentService.getGrades(this.gradeHocKy, this.gradeNamHoc).subscribe({
      next: (res) => {
        this.grades = res;
        this.loading = false;
        this.cd.detectChanges();
      },
      error: () => {
        this.loading = false;
        this.errorMessage = 'Không thể tải bảng điểm kết quả học tập.';
        this.cd.detectChanges();
      },
    });
  }

  protected onGradeFilterChange(): void {
    this.loadGrades();
  }

  // Conversion logic standard to Vietnam higher education credit system
  protected getGradeScale(score: number | null | undefined): {
    letter: string;
    gpa4: number;
    status: string;
    badgeClass: string;
  } {
    if (score === null || score === undefined) {
      return { letter: '-', gpa4: 0, status: 'Chưa có điểm', badgeClass: 'badge-pending' };
    }
    if (score >= 8.5) return { letter: 'A', gpa4: 4.0, status: 'Đạt', badgeClass: 'badge-a' };
    if (score >= 8.0) return { letter: 'B+', gpa4: 3.5, status: 'Đạt', badgeClass: 'badge-b-plus' };
    if (score >= 7.0) return { letter: 'B', gpa4: 3.0, status: 'Đạt', badgeClass: 'badge-b' };
    if (score >= 6.5) return { letter: 'C+', gpa4: 2.5, status: 'Đạt', badgeClass: 'badge-c-plus' };
    if (score >= 5.5) return { letter: 'C', gpa4: 2.0, status: 'Đạt', badgeClass: 'badge-c' };
    if (score >= 5.0) return { letter: 'D+', gpa4: 1.5, status: 'Đạt', badgeClass: 'badge-d-plus' };
    if (score >= 4.0) return { letter: 'D', gpa4: 1.0, status: 'Đạt', badgeClass: 'badge-d' };
    return { letter: 'F', gpa4: 0.0, status: 'Học lại', badgeClass: 'badge-f' };
  }

  // --- Computed properties ---
  protected get filteredCourses(): Course[] {
    const q = this.courseSearch.trim().toLowerCase();
    return this.courses.filter((c) => {
      const matchSearch =
        !q ||
        c.tenMonHoc.toLowerCase().includes(q) ||
        c.maMonHoc.toLowerCase().includes(q) ||
        (c.giangVienTen && c.giangVienTen.toLowerCase().includes(q)) ||
        (c.tenLop && c.tenLop.toLowerCase().includes(q));
      const matchKhoa =
        !this.selectedKhoaFilter || c.khoaTen === this.selectedKhoaFilter;
      return matchSearch && matchKhoa;
    });
  }

  protected get paginatedCourses(): Course[] {
    const start = (this.coursePage - 1) * this.coursePageSize;
    return this.filteredCourses.slice(start, start + this.coursePageSize);
  }

  protected get paginatedGrades(): Grade[] {
    const start = (this.gradePage - 1) * this.gradePageSize;
    return this.grades.slice(start, start + this.gradePageSize);
  }

  protected get availableDepartments(): string[] {
    const depts = new Set<string>();
    this.courses.forEach((c) => {
      if (c.khoaTen) depts.add(c.khoaTen);
    });
    return Array.from(depts);
  }

  protected get totalRegisteredCredits(): number {
    return this.registrations.reduce((acc, r) => acc + (r.soTinChi || 0), 0);
  }

  protected get registrationProgressPercent(): number {
    const max = 24;
    return Math.min(100, Math.round((this.totalRegisteredCredits / max) * 100));
  }

  // Grades computed metrics
  protected get totalAttemptedCredits(): number {
    return this.grades.reduce((acc, g) => acc + (g.soTinChi || 0), 0);
  }

  protected get totalPassedCredits(): number {
    return this.grades
      .filter((g) => g.dat || (g.diemTongKet !== null && g.diemTongKet >= 4.0))
      .reduce((acc, g) => acc + (g.soTinChi || 0), 0);
  }

  protected get gpa10(): string {
    const validGrades = this.grades.filter((g) => g.diemTongKet !== null && g.soTinChi > 0);
    if (validGrades.length === 0) return '-';
    const totalWeighted = validGrades.reduce(
      (sum, g) => sum + (g.diemTongKet ?? 0) * g.soTinChi,
      0
    );
    const totalCredits = validGrades.reduce((sum, g) => sum + g.soTinChi, 0);
    return totalCredits > 0 ? (totalWeighted / totalCredits).toFixed(2) : '-';
  }

  protected get gpa4(): string {
    const validGrades = this.grades.filter((g) => g.diemTongKet !== null && g.soTinChi > 0);
    if (validGrades.length === 0) return '-';
    const totalWeighted = validGrades.reduce((sum, g) => {
      const gpa4Val = this.getGradeScale(g.diemTongKet).gpa4;
      return sum + gpa4Val * g.soTinChi;
    }, 0);
    const totalCredits = validGrades.reduce((sum, g) => sum + g.soTinChi, 0);
    return totalCredits > 0 ? (totalWeighted / totalCredits).toFixed(2) : '-';
  }

  protected get academicStanding(): { label: string; badgeClass: string } {
    const gpa4Num = parseFloat(this.gpa4);
    if (isNaN(gpa4Num)) return { label: 'Chưa xếp loại', badgeClass: 'tag-muted' };
    if (gpa4Num >= 3.6) return { label: 'Xuất sắc', badgeClass: 'tag-purple' };
    if (gpa4Num >= 3.2) return { label: 'Giỏi', badgeClass: 'tag-emerald' };
    if (gpa4Num >= 2.5) return { label: 'Khá', badgeClass: 'tag-blue' };
    if (gpa4Num >= 2.0) return { label: 'Trung bình', badgeClass: 'tag-amber' };
    return { label: 'Yếu / Cảnh báo', badgeClass: 'tag-rose' };
  }

  protected get passedCount(): number {
    return this.grades.filter((g) => g.dat || (g.diemTongKet !== null && g.diemTongKet >= 4.0)).length;
  }

  protected get failedCount(): number {
    return this.grades.filter((g) => !g.dat && g.diemTongKet !== null && g.diemTongKet < 4.0).length;
  }

  protected get passRate(): string {
    const graded = this.grades.filter((g) => g.diemTongKet !== null);
    if (graded.length === 0) return '-';
    const rate = (this.passedCount / graded.length) * 100;
    return `${rate.toFixed(1)}%`;
  }

  protected get studentInitials(): string {
    if (!this.profile?.hoTen) return 'SV';
    const parts = this.profile.hoTen.trim().split(/\s+/);
    if (parts.length === 1) return parts[0].substring(0, 2).toUpperCase();
    return (parts[0][0] + parts[parts.length - 1][0]).toUpperCase();
  }

  protected printPage(): void {
    window.print();
  }
}
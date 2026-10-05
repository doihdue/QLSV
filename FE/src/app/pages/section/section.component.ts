import { ChangeDetectorRef, Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { RouterLink } from '@angular/router';
import { forkJoin, of } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { AuthService } from '../../services/auth.service';
import { MonHocMo } from '../../services/student.service';

export type DashboardCounts = {
  sinhVien: number;
  giangVien: number;
  monHoc: number;
  khoa: number;
  lop: number;
  dangKy: number;
};

@Component({
  selector: 'app-section',
  imports: [CommonModule, RouterLink],
  templateUrl: './section.component.html',
  styleUrl: './section.component.scss',
})
export class SectionComponent implements OnInit {
  private readonly http = inject(HttpClient);
  private readonly authService = inject(AuthService);
  private readonly cd = inject(ChangeDetectorRef);

  protected get isStudent(): boolean {
    return this.authService.isStudent;
  }

  protected get isLecturer(): boolean {
    return this.authService.isLecturer;
  }

  protected get isAdmin(): boolean {
    return this.authService.isAdmin;
  }

  protected get currentUserName(): string {
    const name = this.authService.currentUser?.fullName;
    if (!name || name.includes('?')) {
      if (this.isStudent) return 'Sinh viên';
      if (this.isLecturer) return 'Giảng viên';
      return 'Quản trị hệ thống';
    }
    return name;
  }

  // Admin stats
  protected counts: DashboardCounts = {
    sinhVien: 0,
    giangVien: 0,
    monHoc: 0,
    khoa: 0,
    lop: 0,
    dangKy: 0,
  };
  protected loading = false;

  // Lecturer stats
  protected lecturerClasses: MonHocMo[] = [];
  protected loadingLecturer = false;

  ngOnInit(): void {
    if (this.isAdmin) {
      this.loadAdminStats();
    } else if (this.isLecturer) {
      this.loadLecturerClasses();
    }
  }

  protected loadAdminStats(): void {
    this.loading = true;

    forkJoin({
      sinhVien: this.http.get<unknown[]>('http://localhost:8080/api/sinh-vien').pipe(catchError(() => of([]))),
      giangVien: this.http.get<unknown[]>('http://localhost:8080/api/giang-vien').pipe(catchError(() => of([]))),
      monHoc: this.http.get<unknown[]>('http://localhost:8080/api/mon-hoc').pipe(catchError(() => of([]))),
      khoa: this.http.get<unknown[]>('http://localhost:8080/api/khoa').pipe(catchError(() => of([]))),
      lop: this.http.get<unknown[]>('http://localhost:8080/api/lop').pipe(catchError(() => of([]))),
      dangKy: this.http.get<unknown[]>('http://localhost:8080/api/dang-ky-mon-hoc').pipe(catchError(() => of([]))),
    }).subscribe({
      next: (results) => {
        this.counts = {
          sinhVien: results.sinhVien.length,
          giangVien: results.giangVien.length,
          monHoc: results.monHoc.length,
          khoa: results.khoa.length,
          lop: results.lop.length,
          dangKy: results.dangKy.length,
        };
        this.loading = false;
        this.cd.detectChanges();
      },
      error: () => {
        this.loading = false;
        this.cd.detectChanges();
      },
    });
  }

  protected loadLecturerClasses(): void {
    this.loadingLecturer = true;
    this.http.get<MonHocMo[]>('http://localhost:8080/api/lecturer/mon-hoc-mo').pipe(
      catchError(() => of([]))
    ).subscribe({
      next: (res) => {
        this.lecturerClasses = (res || []).filter(
          (c) => !!c.tenLop && c.tenLop.trim() !== '' && c.tenLop !== 'Tất cả' && c.tenLop !== 'Tất cả lớp'
        );
        this.loadingLecturer = false;
        this.cd.detectChanges();
      },
      error: () => {
        this.loadingLecturer = false;
        this.cd.detectChanges();
      },
    });
  }
}

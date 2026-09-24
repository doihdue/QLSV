import { ChangeDetectorRef, Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { RouterLink } from '@angular/router';
import { forkJoin, of } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { AuthService } from '../../services/auth.service';

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

  protected get currentUserName(): string {
    const name = this.authService.currentUser?.fullName;
    if (!name || name.includes('?')) {
      return this.isStudent ? 'Sinh viên' : 'Quản trị hệ thống';
    }
    return name;
  }

  protected counts: DashboardCounts = {
    sinhVien: 0,
    giangVien: 0,
    monHoc: 0,
    khoa: 0,
    lop: 0,
    dangKy: 0,
  };

  protected loading = false;

  ngOnInit(): void {
    if (!this.isStudent) {
      this.loadAdminStats();
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
        this.cd.markForCheck();
      },
      error: () => {
        this.loading = false;
        this.cd.markForCheck();
      },
    });
  }
}


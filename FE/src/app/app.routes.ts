import { Routes } from '@angular/router';
import { authGuard } from './services/auth.guard';

export const routes: Routes = [
  {
    path: 'login',
    loadComponent: () => import('./pages/login/login.component').then((m) => m.LoginComponent),
  },
  {
    canActivate: [authGuard],
    path: '',
    loadComponent: () => import('./layout/shell.component').then((m) => m.ShellComponent),
    children: [
      { path: '', pathMatch: 'full', redirectTo: 'dashboard' },
      {
        path: 'dashboard',
        loadComponent: () => import('./pages/section/section.component').then((m) => m.SectionComponent),
      },
      // Phân hệ Quản trị Đào tạo (Admin)
      {
        path: 'quan-ly/khoa',
        loadComponent: () =>
          import('./pages/admin/khoa/admin-khoa.component').then((m) => m.AdminKhoaComponent),
      },
      {
        path: 'quan-ly/lop',
        loadComponent: () =>
          import('./pages/admin/lop/admin-lop.component').then((m) => m.AdminLopComponent),
      },
      {
        path: 'quan-ly/mon-hoc',
        loadComponent: () =>
          import('./pages/admin/mon-hoc/admin-mon-hoc.component').then((m) => m.AdminMonHocComponent),
      },
      {
        path: 'quan-ly/giang-vien',
        loadComponent: () =>
          import('./pages/admin/giang-vien/admin-giang-vien.component').then((m) => m.AdminGiangVienComponent),
      },
      {
        path: 'quan-ly/sinh-vien',
        loadComponent: () =>
          import('./pages/admin/sinh-vien/admin-sinh-vien.component').then((m) => m.AdminSinhVienComponent),
      },
      {
        path: 'dao-tao/quan-ly-diem',
        loadComponent: () =>
          import('./pages/admin/diem-dangky/admin-diem-dangky.component').then((m) => m.AdminDiemDangKyComponent),
      },
      // Cổng Thông tin Sinh viên (Student Portal)
      {
        path: 'sinh-vien/ho-so-ca-nhan',
        data: { mode: 'profile' },
        loadComponent: () =>
          import('./pages/student-portal/student-portal.component').then((m) => m.StudentPortalComponent),
      },
      {
        path: 'sinh-vien/bang-diem',
        data: { mode: 'grades' },
        loadComponent: () =>
          import('./pages/student-portal/student-portal.component').then((m) => m.StudentPortalComponent),
      },
      {
        path: 'sinh-vien/dang-ky-mon-hoc',
        data: { mode: 'registration' },
        loadComponent: () =>
          import('./pages/student-portal/student-portal.component').then((m) => m.StudentPortalComponent),
      },
      // Cổng Giảng viên (Lecturer Portal)
      {
        path: 'giang-vien/lop-mon-hoc',
        loadComponent: () =>
          import('./pages/lecturer/lecturer-diem.component').then((m) => m.LecturerDiemComponent),
      },
    ],
  },
  { path: '**', redirectTo: 'dashboard' },
];

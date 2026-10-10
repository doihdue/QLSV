import { Routes } from '@angular/router';
import { adminGuard, authGuard, lecturerGuard, studentGuard } from './services/auth.guard';

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
      // Phân hệ Quản trị Đào tạo (Admin Only)
      {
        path: 'quan-ly/khoa',
        canActivate: [adminGuard],
        loadComponent: () =>
          import('./pages/admin/khoa/admin-khoa.component').then((m) => m.AdminKhoaComponent),
      },
      {
        path: 'quan-ly/lop',
        canActivate: [adminGuard],
        loadComponent: () =>
          import('./pages/admin/lop/admin-lop.component').then((m) => m.AdminLopComponent),
      },
      {
        path: 'quan-ly/mon-hoc',
        canActivate: [adminGuard],
        loadComponent: () =>
          import('./pages/admin/mon-hoc/admin-mon-hoc.component').then((m) => m.AdminMonHocComponent),
      },
      {
        path: 'quan-ly/giang-vien',
        canActivate: [adminGuard],
        loadComponent: () =>
          import('./pages/admin/giang-vien/admin-giang-vien.component').then((m) => m.AdminGiangVienComponent),
      },
      {
        path: 'quan-ly/sinh-vien',
        canActivate: [adminGuard],
        loadComponent: () =>
          import('./pages/admin/sinh-vien/admin-sinh-vien.component').then((m) => m.AdminSinhVienComponent),
      },
      {
        path: 'dao-tao/quan-ly-diem',
        canActivate: [adminGuard],
        loadComponent: () =>
          import('./pages/admin/diem-dangky/admin-diem-dangky.component').then((m) => m.AdminDiemDangKyComponent),
      },
      {
        path: 'dao-tao/diem-ren-luyen',
        canActivate: [adminGuard],
        loadComponent: () =>
          import('./pages/admin/diem-ren-luyen/admin-diem-ren-luyen.component').then(
            (m) => m.AdminDiemRenLuyenComponent
          ),
      },
      {
        path: 'thong-ke/gpa',
        canActivate: [adminGuard],
        loadComponent: () =>
          import('./pages/admin/thong-ke-gpa/thong-ke-gpa.component').then((m) => m.ThongKeGpaComponent),
      },
      // Cổng Thông tin Sinh viên (Student Only)
      {
        path: 'sinh-vien/ho-so-ca-nhan',
        canActivate: [studentGuard],
        data: { mode: 'profile' },
        loadComponent: () =>
          import('./pages/student-portal/student-portal.component').then((m) => m.StudentPortalComponent),
      },
      {
        path: 'sinh-vien/bang-diem',
        canActivate: [studentGuard],
        data: { mode: 'grades' },
        loadComponent: () =>
          import('./pages/student-portal/student-portal.component').then((m) => m.StudentPortalComponent),
      },
      {
        path: 'sinh-vien/dang-ky-mon-hoc',
        canActivate: [studentGuard],
        data: { mode: 'registration' },
        loadComponent: () =>
          import('./pages/student-portal/student-portal.component').then((m) => m.StudentPortalComponent),
      },
      {
        path: 'sinh-vien/diem-ren-luyen',
        canActivate: [studentGuard],
        loadComponent: () =>
          import('./pages/student-portal/diem-ren-luyen/student-diem-ren-luyen.component').then(
            (m) => m.StudentDiemRenLuyenComponent
          ),
      },
      // Cổng Giảng viên (Lecturer Only)
      {
        path: 'giang-vien/lop-mon-hoc',
        canActivate: [lecturerGuard],
        loadComponent: () =>
          import('./pages/lecturer/lecturer-diem.component').then((m) => m.LecturerDiemComponent),
      },
      {
        path: 'giang-vien/diem-ren-luyen',
        canActivate: [lecturerGuard],
        loadComponent: () =>
          import('./pages/lecturer/diem-ren-luyen/lecturer-diem-ren-luyen.component').then(
            (m) => m.LecturerDiemRenLuyenComponent
          ),
      },
    ],
  },
  { path: '**', redirectTo: 'dashboard' },
];

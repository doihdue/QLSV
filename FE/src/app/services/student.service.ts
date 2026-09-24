import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';

export type Course = {
  id: number;
  maMonHoc: string;
  tenMonHoc: string;
  soTinChi: number;
  soTietLyThuyet?: number;
  soTietThucHanh?: number;
  moTa?: string;
  monHocTienQuyet?: string;
  khoaTen?: string;
};

export type Registration = {
  id: number;
  sinhVienId?: number;
  sinhVienMssv?: string;
  sinhVienHoTen?: string;
  monHocId: number;
  monHocMa: string;
  monHocTen: string;
  soTinChi: number;
  hocKy: string;
  namHoc: string;
  ngayDangKy?: string;
  trangThai: string;
};

export type Grade = {
  id: number;
  dangKyMonHocId: number;
  sinhVienId?: number;
  sinhVienMssv?: string;
  sinhVienHoTen?: string;
  monHocId?: number;
  monHocMa: string;
  monHocTen: string;
  soTinChi: number;
  hocKy: string;
  namHoc: string;
  diemChuyenCan: number | null;
  diemGiuaKy: number | null;
  diemCuoiKy: number | null;
  diemTongKet: number | null;
  xepLoai: string;
  dat: boolean;
};

export type StudentProfile = {
  id: number;
  mssv: string;
  hoTen: string;
  ngaySinh: string | null;
  gioiTinh: string | null;
  email: string;
  soDienThoai: string | null;
  diaChi: string | null;
  ngayNhapHoc: string | null;
  lopTen: string | null;
  khoaTen: string | null;
  active?: boolean;
};

export type StudentProfileUpdate = {
  email: string;
  soDienThoai: string;
  diaChi: string;
};

export type ChangePasswordPayload = {
  oldPassword: string;
  newPassword: string;
};

export type DotDangKy = {
  id: number;
  hocKy: string;
  namHoc: string;
  tenDot: string;
  dangMo: boolean;
  ngayBatDau?: string;
  ngayKetThuc?: string;
};

export type MonHocMo = {
  id: number;
  monHocId: number;
  monHocMa: string;
  tenMonHoc: string;
  soTinChi: number;
  soTietLyThuyet?: number;
  soTietThucHanh?: number;
  monHocTienQuyet?: string;
  khoaId: number;
  tenKhoa: string;
  khoaHoc: string;
  hocKy: string;
  namHoc: string;
  lopId?: number;
  tenLop?: string;
  maLop?: string;
  giangVienId?: number;
  maGiangVien?: string;
  tenGiangVien?: string;
  ghiChu?: string;
};

export type MonHocMoPayload = {
  monHocId: number;
  khoaId: number;
  khoaHoc: string;
  hocKy: string;
  namHoc: string;
  lopId?: number | null;
  giangVienId?: number | null;
  ghiChu?: string;
};

@Injectable({ providedIn: 'root' })
export class StudentService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = 'http://localhost:8080/api';

  getCurrentDotDangKy(): Observable<DotDangKy> {
    return this.http.get<DotDangKy>(`${this.apiUrl}/dot-dang-ky/hien-tai`);
  }

  updateDotDangKy(payload: Partial<DotDangKy>): Observable<DotDangKy> {
    return this.http.put<DotDangKy>(`${this.apiUrl}/dot-dang-ky/hien-tai`, payload);
  }

  toggleDotDangKy(open: boolean): Observable<DotDangKy> {
    return this.http.patch<DotDangKy>(`${this.apiUrl}/dot-dang-ky/hien-tai/toggle?open=${open}`, {});
  }

  getCourses(): Observable<Course[]> {
    return this.http.get<Course[]>(`${this.apiUrl}/mon-hoc/active`);
  }

  getMonHocMoList(hocKy = '', namHoc = '', khoaId?: number, khoaHoc?: string, lopId?: number): Observable<MonHocMo[]> {
    let params = new HttpParams();
    if (hocKy) params = params.set('hocKy', hocKy);
    if (namHoc) params = params.set('namHoc', namHoc);
    if (khoaId) params = params.set('khoaId', khoaId.toString());
    if (khoaHoc) params = params.set('khoaHoc', khoaHoc);
    if (lopId) params = params.set('lopId', lopId.toString());
    return this.http.get<MonHocMo[]>(`${this.apiUrl}/mon-hoc-mo`, { params });
  }

  createMonHocMo(payload: MonHocMoPayload): Observable<MonHocMo> {
    return this.http.post<MonHocMo>(`${this.apiUrl}/mon-hoc-mo`, payload);
  }

  deleteMonHocMo(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/mon-hoc-mo/${id}`);
  }

  getCoursesForStudent(hocKy = '', namHoc = ''): Observable<MonHocMo[]> {
    let params = new HttpParams();
    if (hocKy) params = params.set('hocKy', hocKy);
    if (namHoc) params = params.set('namHoc', namHoc);
    return this.http.get<MonHocMo[]>(`${this.apiUrl}/mon-hoc-mo/sinh-vien`, { params });
  }

  getRegistrations(hocKy = '', namHoc = ''): Observable<Registration[]> {
    let params = new HttpParams();
    if (hocKy) params = params.set('hocKy', hocKy);
    if (namHoc) params = params.set('namHoc', namHoc);
    return this.http.get<Registration[]>(`${this.apiUrl}/dang-ky-mon-hoc/me`, { params });
  }

  register(monHocId: number, hocKy: string, namHoc: string): Observable<Registration> {
    return this.http.post<Registration>(`${this.apiUrl}/dang-ky-mon-hoc/me`, { monHocId, hocKy, namHoc });
  }

  cancelRegistration(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/dang-ky-mon-hoc/me/${id}`);
  }

  getGrades(hocKy = '', namHoc = ''): Observable<Grade[]> {
    let params = new HttpParams();
    if (hocKy) params = params.set('hocKy', hocKy);
    if (namHoc) params = params.set('namHoc', namHoc);
    return this.http.get<Grade[]>(`${this.apiUrl}/quan-ly-diem/me`, { params });
  }

  getProfile(): Observable<StudentProfile> {
    return this.http.get<StudentProfile>(`${this.apiUrl}/sinh-vien/me`);
  }

  updateProfile(payload: StudentProfileUpdate): Observable<StudentProfile> {
    return this.http.patch<StudentProfile>(`${this.apiUrl}/sinh-vien/me/profile`, payload);
  }

  changePassword(payload: ChangePasswordPayload): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/auth/change-password`, payload);
  }
}
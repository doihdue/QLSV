import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';

export type DotRenLuyen = {
  id: number;
  tenDot: string;
  hocKy: string;
  namHoc: string;
  ngayBatDau: string;
  ngayKetThuc: string;
  dangMo: boolean;
  ghiChu?: string;
  trangThaiThoiGian?: string; // ACTIVE, NOT_YET, EXPIRED
};

export type DotRenLuyenPayload = {
  tenDot: string;
  hocKy: string;
  namHoc: string;
  ngayBatDau: string;
  ngayKetThuc: string;
  dangMo: boolean;
  ghiChu?: string;
};

export type BangDiemRenLuyen = {
  id: number;
  dotRenLuyenId: number;
  tenDot: string;
  hocKy: string;
  namHoc: string;
  sinhVienId: number;
  mssv: string;
  hoTen: string;
  lopId: number;
  tenLop: string;
  tenKhoa: string;

  diemSvMuc1: number | null;
  diemSvMuc2: number | null;
  diemSvMuc3: number | null;
  diemSvMuc4: number | null;
  diemSvMuc5: number | null;
  tongDiemSv: number | null;
  ghiChuSv?: string;
  thoiGianSvNop?: string;

  diemGvMuc1: number | null;
  diemGvMuc2: number | null;
  diemGvMuc3: number | null;
  diemGvMuc4: number | null;
  diemGvMuc5: number | null;
  tongDiemGv: number | null;
  nhanXetGv?: string;
  hoTenGiangVien?: string;
  thoiGianGvDanhGia?: string;

  diemTongKet: number | null;
  xepLoai?: string;
  nhanXetAdmin?: string;
  thoiGianAdminDuyet?: string;

  trangThai: string;
  trangThaiMoTa: string;
};

export type StudentDanhGiaRequest = {
  dotId: number;
  diemSvMuc1: number;
  diemSvMuc2: number;
  diemSvMuc3: number;
  diemSvMuc4: number;
  diemSvMuc5: number;
  ghiChuSv?: string;
  action: 'SAVE_DRAFT' | 'SUBMIT';
};

export type LecturerDanhGiaRequest = {
  bangDiemId: number;
  diemGvMuc1: number;
  diemGvMuc2: number;
  diemGvMuc3: number;
  diemGvMuc4: number;
  diemGvMuc5: number;
  nhanXetGv?: string;
  action: 'SAVE_DRAFT' | 'SUBMIT_ADMIN';
};

export type AdminPheDuyetRequest = {
  bangDiemId: number;
  action: 'APPROVE' | 'REJECT';
  nhanXetAdmin?: string;
  diemDieuChinh?: number;
};

export type ThongKeDrl = {
  tongSinhVien: number;
  chuaDanhGia: number;
  choGiangVienDuyet: number;
  choAdminDuyet: number;
  daDuyet: number;
  tuChoi: number;
  countXuatSac: number;
  countTot: number;
  countKha: number;
  countTrungBinh: number;
  countYeu: number;
  countKem: number;
  diemTrungBinhToanLop: number;
};

@Injectable({ providedIn: 'root' })
export class DrlService {
  private readonly http = inject(HttpClient);
  private readonly dotUrl = 'http://localhost:8080/api/dot-ren-luyen';
  private readonly drlUrl = 'http://localhost:8080/api/diem-ren-luyen';

  // === Đợt Rèn Luyện ===
  getAllDots(): Observable<DotRenLuyen[]> {
    return this.http.get<DotRenLuyen[]>(this.dotUrl);
  }

  getCurrentDot(): Observable<DotRenLuyen> {
    return this.http.get<DotRenLuyen>(`${this.dotUrl}/hien-tai`);
  }

  createDot(payload: DotRenLuyenPayload): Observable<DotRenLuyen> {
    return this.http.post<DotRenLuyen>(this.dotUrl, payload);
  }

  updateDot(id: number, payload: DotRenLuyenPayload): Observable<DotRenLuyen> {
    return this.http.put<DotRenLuyen>(`${this.dotUrl}/${id}`, payload);
  }

  toggleDot(id: number, open: boolean): Observable<DotRenLuyen> {
    return this.http.patch<DotRenLuyen>(`${this.dotUrl}/${id}/toggle?open=${open}`, {});
  }

  // === Sinh Viên ===
  getMyScore(dotId?: number): Observable<BangDiemRenLuyen> {
    let params = new HttpParams();
    if (dotId) params = params.set('dotId', dotId.toString());
    return this.http.get<BangDiemRenLuyen>(`${this.drlUrl}/me`, { params });
  }

  studentSubmit(req: StudentDanhGiaRequest): Observable<BangDiemRenLuyen> {
    return this.http.post<BangDiemRenLuyen>(`${this.drlUrl}/me/submit`, req);
  }

  // === Giảng Viên ===
  getScoresForClass(lopId: number, dotId?: number): Observable<BangDiemRenLuyen[]> {
    let params = new HttpParams();
    if (dotId) params = params.set('dotId', dotId.toString());
    return this.http.get<BangDiemRenLuyen[]>(`${this.drlUrl}/lop/${lopId}`, { params });
  }

  getScoreById(id: number): Observable<BangDiemRenLuyen> {
    return this.http.get<BangDiemRenLuyen>(`${this.drlUrl}/chi-tiet/${id}`);
  }

  lecturerEvaluate(req: LecturerDanhGiaRequest): Observable<BangDiemRenLuyen> {
    return this.http.post<BangDiemRenLuyen>(`${this.drlUrl}/giang-vien/evaluate`, req);
  }

  lecturerSubmitClass(lopId: number, dotId?: number): Observable<{ message: string; count: number }> {
    let params = new HttpParams().set('lopId', lopId.toString());
    if (dotId) params = params.set('dotId', dotId.toString());
    return this.http.post<{ message: string; count: number }>(`${this.drlUrl}/giang-vien/submit-lop`, null, { params });
  }

  // === Admin ===
  getScoresForAdmin(dotId?: number, lopId?: number, trangThai?: string): Observable<BangDiemRenLuyen[]> {
    let params = new HttpParams();
    if (dotId) params = params.set('dotId', dotId.toString());
    if (lopId) params = params.set('lopId', lopId.toString());
    if (trangThai && trangThai !== 'ALL') params = params.set('trangThai', trangThai);
    return this.http.get<BangDiemRenLuyen[]>(`${this.drlUrl}/admin`, { params });
  }

  getThongKe(dotId?: number, lopId?: number): Observable<ThongKeDrl> {
    let params = new HttpParams();
    if (dotId) params = params.set('dotId', dotId.toString());
    if (lopId) params = params.set('lopId', lopId.toString());
    return this.http.get<ThongKeDrl>(`${this.drlUrl}/admin/thong-ke`, { params });
  }

  adminApproveOrReject(req: AdminPheDuyetRequest): Observable<BangDiemRenLuyen> {
    return this.http.post<BangDiemRenLuyen>(`${this.drlUrl}/admin/phe-duyet`, req);
  }

  adminApproveWholeClass(lopId: number, dotId?: number): Observable<{ message: string; count: number }> {
    let params = new HttpParams().set('lopId', lopId.toString());
    if (dotId) params = params.set('dotId', dotId.toString());
    return this.http.post<{ message: string; count: number }>(`${this.drlUrl}/admin/duyet-lop`, null, { params });
  }
}

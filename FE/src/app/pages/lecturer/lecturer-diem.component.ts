import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { ChangeDetectorRef, Component, inject, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MonHocMo } from '../../services/student.service';

export type LecturerGradeItem = {
  dangKyMonHocId: number;
  sinhVienId: number;
  sinhVienMssv: string;
  sinhVienHoTen: string;
  tenLop: string;
  diemId?: number;
  diemChuyenCan: number | null;
  diemGiuaKy: number | null;
  diemCuoiKy: number | null;
  diemTongKet: number | null;
  xepLoai?: string;
  dat: boolean;
  trangThaiDuyet: string;
  ghiChuDuyet?: string;
};

@Component({
  selector: 'app-lecturer-diem',
  imports: [CommonModule, FormsModule],
  templateUrl: './lecturer-diem.component.html',
  styleUrl: './lecturer-diem.component.scss',
})
export class LecturerDiemComponent implements OnInit {
  private readonly http = inject(HttpClient);
  private readonly cd = inject(ChangeDetectorRef);

  protected classes: MonHocMo[] = [];
  protected selectedClass: MonHocMo | null = null;
  protected students: LecturerGradeItem[] = [];

  protected loadingClasses = false;
  protected loadingStudents = false;
  protected saving = false;
  protected successMessage = '';
  protected errorMessage = '';

  ngOnInit(): void {
    this.loadMyClasses();
  }

  protected loadMyClasses(): void {
    this.loadingClasses = true;
    this.errorMessage = '';
    this.http.get<MonHocMo[]>('http://localhost:8080/api/lecturer/mon-hoc-mo').subscribe({
      next: (res) => {
        this.classes = res;
        this.loadingClasses = false;
        if (this.classes.length > 0 && !this.selectedClass) {
          this.selectClass(this.classes[0]);
        }
        this.cd.detectChanges();
      },
      error: () => {
        this.errorMessage = 'Không thể tải danh sách lớp môn học phân công.';
        this.loadingClasses = false;
        this.cd.detectChanges();
      },
    });
  }

  protected selectClass(cls: MonHocMo): void {
    this.selectedClass = cls;
    this.successMessage = '';
    this.errorMessage = '';
    this.loadStudentsForClass(cls.id);
  }

  protected loadStudentsForClass(monHocMoId: number): void {
    this.loadingStudents = true;
    this.http.get<LecturerGradeItem[]>(`http://localhost:8080/api/lecturer/bang-diem/${monHocMoId}`).subscribe({
      next: (res) => {
        this.students = res;
        this.loadingStudents = false;
        this.cd.detectChanges();
      },
      error: () => {
        this.errorMessage = 'Không thể tải danh sách sinh viên của lớp môn học này.';
        this.loadingStudents = false;
        this.cd.detectChanges();
      },
    });
  }

  protected onScoreChange(item: LecturerGradeItem): void {
    const cc = item.diemChuyenCan !== null && item.diemChuyenCan !== undefined ? Number(item.diemChuyenCan) : null;
    const gk = item.diemGiuaKy !== null && item.diemGiuaKy !== undefined ? Number(item.diemGiuaKy) : null;
    const ck = item.diemCuoiKy !== null && item.diemCuoiKy !== undefined ? Number(item.diemCuoiKy) : null;

    if (cc !== null && gk !== null && ck !== null) {
      const total = Number((cc * 0.1 + gk * 0.3 + ck * 0.6).toFixed(2));
      item.diemTongKet = total;
      item.dat = total >= 5.0;
      if (total >= 8.5) item.xepLoai = 'Xuất sắc';
      else if (total >= 7.0) item.xepLoai = 'Khá';
      else if (total >= 5.0) item.xepLoai = 'Trung bình';
      else item.xepLoai = 'Không đạt';
    }
  }

  protected get overallStatus(): string {
    if (!this.students || this.students.length === 0) return 'CHUA_CO_SV';
    const firstStatus = this.students[0].trangThaiDuyet || 'BAN_NHAP';
    return firstStatus;
  }

  protected get isLocked(): boolean {
    return this.overallStatus === 'CHO_DUYET' || this.overallStatus === 'DA_DUYET';
  }

  protected get rejectReason(): string | null {
    const item = this.students.find((s) => s.trangThaiDuyet === 'TU_CHOI' && s.ghiChuDuyet);
    return item?.ghiChuDuyet || null;
  }

  protected save(submitForApproval: boolean): void {
    if (!this.selectedClass) return;

    if (submitForApproval) {
      const hasEmpty = this.students.some(
        (s) => s.diemChuyenCan === null || s.diemGiuaKy === null || s.diemCuoiKy === null
      );
      if (hasEmpty) {
        if (!confirm('Một số sinh viên chưa được nhập đủ điểm. Bạn có chắc chắn muốn gửi duyệt danh sách này không?')) {
          return;
        }
      } else {
        if (!confirm('Bạn có chắc chắn muốn gửi bảng điểm này cho Quản trị viên (Admin) duyệt không? Sau khi gửi, bạn sẽ không thể chỉnh sửa cho đến khi có phản hồi.')) {
          return;
        }
      }
    }

    const payload = {
      monHocMoId: this.selectedClass.id,
      submitForApproval,
      grades: this.students.map((s) => ({
        dangKyMonHocId: s.dangKyMonHocId,
        diemChuyenCan: s.diemChuyenCan,
        diemGiuaKy: s.diemGiuaKy,
        diemCuoiKy: s.diemCuoiKy,
      })),
    };

    this.saving = true;
    this.successMessage = '';
    this.errorMessage = '';

    this.http.post<LecturerGradeItem[]>('http://localhost:8080/api/lecturer/bang-diem', payload).subscribe({
      next: (res) => {
        this.saving = false;
        this.students = res;
        this.successMessage = submitForApproval
          ? 'Đã gửi bảng điểm lên Admin phê duyệt thành công!'
          : 'Đã lưu nháp bảng điểm thành công!';
        this.cd.detectChanges();
      },
      error: () => {
        this.saving = false;
        this.errorMessage = 'Không thể lưu bảng điểm. Vui lòng thử lại.';
        this.cd.detectChanges();
      },
    });
  }
}

import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { ChangeDetectorRef, Component, inject, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MonHocMo } from '../../services/student.service';
import { PaginationComponent } from '../../components/pagination/pagination.component';

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
  imports: [CommonModule, FormsModule, PaginationComponent],
  templateUrl: './lecturer-diem.component.html',
  styleUrl: './lecturer-diem.component.scss',
})
export class LecturerDiemComponent implements OnInit {
  private readonly http = inject(HttpClient);
  private readonly cd = inject(ChangeDetectorRef);

  protected classes: MonHocMo[] = [];
  protected selectedClass: MonHocMo | null = null;
  protected students: LecturerGradeItem[] = [];

  protected currentPage = 1;
  protected pageSize = 20;

  protected loadingClasses = false;
  protected loadingStudents = false;
  protected saving = false;
  protected submittedAttempt = false;
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
        // TUYỆT ĐỐI CHỈ LẤY CÁC LỚP HÀNH CHÍNH CỤ THỂ, LOẠI BỎ HOÀN TOÀN CÁC BẢN GHI "LỚP TẤT CẢ"
        this.classes = (res || []).filter(
          (c) => !!c.tenLop && c.tenLop.trim() !== '' && c.tenLop !== 'Tất cả' && c.tenLop !== 'Tất cả lớp'
        );
        this.loadingClasses = false;
        if (this.classes.length > 0) {
          if (!this.selectedClass || !this.classes.some((c) => c.id === this.selectedClass?.id)) {
            this.selectClass(this.classes[0]);
          }
        } else {
          this.selectedClass = null;
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
    this.submittedAttempt = false;
    this.successMessage = '';
    this.errorMessage = '';
    this.cd.detectChanges();
    this.loadStudentsForClass(cls.id);
  }

  protected loadStudentsForClass(monHocMoId: number): void {
    this.loadingStudents = true;
    this.currentPage = 1;
    this.submittedAttempt = false;
    this.http.get<LecturerGradeItem[]>(`http://localhost:8080/api/lecturer/bang-diem/${monHocMoId}`).subscribe({
      next: (res) => {
        const clsName = this.selectedClass?.tenLop;
        if (clsName) {
          // Chỉ lấy sinh viên thuộc đúng lớp hành chính này
          this.students = (res || []).filter((s) => !s.tenLop || s.tenLop === clsName);
        } else {
          this.students = res || [];
        }
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

  protected get paginatedStudents(): LecturerGradeItem[] {
    const start = (this.currentPage - 1) * this.pageSize;
    return this.students.slice(start, start + this.pageSize);
  }

  protected isFieldMissing(val: number | null | undefined): boolean {
    return val === null || val === undefined || isNaN(Number(val)) || Number(val) < 0 || Number(val) > 10;
  }

  protected isStudentIncomplete(item: LecturerGradeItem): boolean {
    return (
      this.isFieldMissing(item.diemChuyenCan) ||
      this.isFieldMissing(item.diemGiuaKy) ||
      this.isFieldMissing(item.diemCuoiKy)
    );
  }

  protected get completedCount(): number {
    return this.students.filter((s) => !this.isStudentIncomplete(s)).length;
  }

  protected get incompleteCount(): number {
    return this.students.length - this.completedCount;
  }

  protected get completionPercent(): number {
    if (!this.students || this.students.length === 0) return 0;
    return Math.round((this.completedCount / this.students.length) * 100);
  }

  protected jumpToFirstIncomplete(): void {
    const firstIdx = this.students.findIndex((s) => this.isStudentIncomplete(s));
    if (firstIdx !== -1) {
      this.currentPage = Math.floor(firstIdx / this.pageSize) + 1;
      this.submittedAttempt = true;
      this.cd.detectChanges();
    }
  }

  protected onScoreChange(item: LecturerGradeItem): void {
    const cc = item.diemChuyenCan !== null && item.diemChuyenCan !== undefined && !isNaN(Number(item.diemChuyenCan)) ? Number(item.diemChuyenCan) : null;
    const gk = item.diemGiuaKy !== null && item.diemGiuaKy !== undefined && !isNaN(Number(item.diemGiuaKy)) ? Number(item.diemGiuaKy) : null;
    const ck = item.diemCuoiKy !== null && item.diemCuoiKy !== undefined && !isNaN(Number(item.diemCuoiKy)) ? Number(item.diemCuoiKy) : null;

    if (cc !== null && gk !== null && ck !== null) {
      const total = Number((cc * 0.1 + gk * 0.3 + ck * 0.6).toFixed(2));
      item.diemTongKet = total;
      item.dat = total >= 5.0;
      if (total >= 8.5) item.xepLoai = 'Xuất sắc';
      else if (total >= 7.0) item.xepLoai = 'Khá';
      else if (total >= 5.0) item.xepLoai = 'Trung bình';
      else item.xepLoai = 'Không đạt';
    } else {
      item.diemTongKet = null;
      item.xepLoai = undefined;
      item.dat = false;
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

    this.successMessage = '';
    this.errorMessage = '';

    if (this.students.length === 0) {
      this.errorMessage = 'Lớp môn học này hiện không có sinh viên nào để lưu điểm.';
      return;
    }

    if (submitForApproval) {
      this.submittedAttempt = true;
      const incompleteList = this.students.filter((s) => this.isStudentIncomplete(s));

      const className = this.selectedClass.tenLop || this.selectedClass.maLop || 'lớp này';
      if (incompleteList.length > 0) {
        this.jumpToFirstIncomplete();
        this.errorMessage = `Không thể gửi duyệt: Lớp ${className} còn ${incompleteList.length} / ${this.students.length} sinh viên chưa được nhập đủ điểm (Chuyên cần, Giữa kỳ, Cuối kỳ). Giảng viên cần hoàn thành đủ điểm cho toàn bộ sinh viên của lớp ${className} trước khi gửi phê duyệt!`;
        return;
      }

      if (!confirm(`Xác nhận gửi bảng điểm riêng cho LỚP "${className}" (${this.selectedClass.monHocMa} - ${this.selectedClass.tenMonHoc}):\n\nToàn bộ ${this.students.length} sinh viên của lớp đã có đủ điểm hợp lệ. Bạn có chắc chắn muốn gửi bảng điểm lớp ${className} lên Admin phê duyệt không? (Chỉ gửi điểm của lớp này, các lớp khác bạn giảng dạy sẽ không bị ảnh hưởng).`)) {
        return;
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

    this.http.post<LecturerGradeItem[]>('http://localhost:8080/api/lecturer/bang-diem', payload).subscribe({
      next: (res) => {
        this.saving = false;
        this.students = res;
        this.submittedAttempt = false;
        const className = this.selectedClass?.tenLop || this.selectedClass?.maLop || '';
        this.successMessage = submitForApproval
          ? `Đã gửi bảng điểm lớp ${className} lên Admin phê duyệt thành công! (Chỉ áp dụng cho riêng lớp này, các lớp khác không bị ảnh hưởng).`
          : `Đã lưu nháp bảng điểm lớp ${className} thành công!`;
        this.cd.detectChanges();
      },
      error: (err) => {
        this.saving = false;
        this.errorMessage = err?.error?.message || 'Không thể lưu bảng điểm. Vui lòng kiểm tra lại dữ liệu và thử lại.';
        this.cd.detectChanges();
      },
    });
  }
}

import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { ChangeDetectorRef, Component, inject, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { PaginationComponent } from '../../../components/pagination/pagination.component';
import { ReportService } from '../../../services/report.service';
import { ToastService } from '../../../services/toast.service';

export type SinhVienGpaItem = {
  sinhVienId: number;
  mssv: string;
  hoTen: string;
  tenLop: string;
  tenKhoa: string;
  soMonHoc: number;
  tinChiTichLuy: number;
  diemTrungBinh: number;
  xepLoaiHocLuc: string;
};

export type LopOption = {
  id: number;
  maLop: string;
  tenLop: string;
};

@Component({
  selector: 'app-thong-ke-gpa',
  imports: [CommonModule, FormsModule, PaginationComponent],
  templateUrl: './thong-ke-gpa.component.html',
  styleUrl: './thong-ke-gpa.component.scss',
})
export class ThongKeGpaComponent implements OnInit {
  private readonly http = inject(HttpClient);
  private readonly cd = inject(ChangeDetectorRef);
  private readonly reportService = inject(ReportService);
  private readonly toast = inject(ToastService);

  protected data: SinhVienGpaItem[] = [];
  protected lops: LopOption[] = [];

  protected selectedLopId = '';
  protected selectedXepLoai = '';
  protected searchText = '';
  protected sortBy = 'gpa_desc';

  protected currentPage = 1;
  protected pageSize = 15;

  protected loading = false;
  protected exportingPdf = false;
  protected errorMessage = '';


  ngOnInit(): void {
    this.loadLops();
    this.loadStats();
  }

  protected loadLops(): void {
    this.http.get<LopOption[]>('http://localhost:8080/api/lop').subscribe({
      next: (res) => {
        this.lops = res;
        this.cd.detectChanges();
      },
    });
  }

  protected loadStats(): void {
    this.loading = true;
    this.errorMessage = '';

    const url = this.selectedLopId
      ? `http://localhost:8080/api/thong-ke/sinh-vien-gpa?lopId=${this.selectedLopId}`
      : 'http://localhost:8080/api/thong-ke/sinh-vien-gpa';

    this.http.get<SinhVienGpaItem[]>(url).subscribe({
      next: (res) => {
        this.data = res;
        this.loading = false;
        this.cd.detectChanges();
      },
      error: () => {
        this.errorMessage = 'Không thể tải thống kê xếp hạng GPA. Vui lòng kiểm tra máy chủ backend.';
        this.loading = false;
        this.cd.detectChanges();
      },
    });
  }

  protected onLopChange(): void {
    this.currentPage = 1;
    this.loadStats();
  }


  // KPIs
  protected get totalStudents(): number {
    return this.data.length;
  }

  protected get gradedStudentsCount(): number {
    return this.data.filter((s) => s.soMonHoc > 0).length;
  }

  protected get averageGpa(): number {
    const graded = this.data.filter((s) => s.soMonHoc > 0);
    if (graded.length === 0) return 0;
    const sum = graded.reduce((acc, cur) => acc + (cur.diemTrungBinh || 0), 0);
    return Math.round((sum / graded.length) * 100) / 100;
  }

  protected get topStudent(): SinhVienGpaItem | null {
    const graded = this.data.filter((s) => s.soMonHoc > 0);
    if (graded.length === 0) return null;
    return graded[0]; // Already ordered by diemTrungBinh DESC from query
  }

  protected get passRate(): number {
    const graded = this.data.filter((s) => s.soMonHoc > 0);
    if (graded.length === 0) return 0;
    const passed = graded.filter((s) => (s.diemTrungBinh || 0) >= 5.0).length;
    return Math.round((passed / graded.length) * 100);
  }

  // Distribution
  protected get countXuatSac(): number {
    return this.data.filter((s) => s.xepLoaiHocLuc === 'Xuất sắc').length;
  }

  protected get countKhaGioi(): number {
    return this.data.filter((s) => s.xepLoaiHocLuc === 'Khá / Giỏi').length;
  }

  protected get countTrungBinh(): number {
    return this.data.filter((s) => s.xepLoaiHocLuc === 'Trung bình').length;
  }

  protected get countYeu(): number {
    return this.data.filter((s) => s.xepLoaiHocLuc === 'Yếu / Cảnh báo').length;
  }

  protected get countChuaDiem(): number {
    return this.data.filter((s) => s.xepLoaiHocLuc === 'Chưa có điểm').length;
  }

  protected getPercent(count: number): number {
    if (this.totalStudents === 0) return 0;
    return Math.round((count / this.totalStudents) * 100);
  }

  protected filterByRank(rank: string): void {
    this.currentPage = 1;
    if (this.selectedXepLoai === rank) {
      this.selectedXepLoai = '';
    } else {
      this.selectedXepLoai = rank;
    }
  }

  protected get filteredData(): SinhVienGpaItem[] {
    const q = this.searchText.trim().toLowerCase();
    const filtered = this.data.filter((item) => {
      const matchSearch =
        !q ||
        item.mssv.toLowerCase().includes(q) ||
        item.hoTen.toLowerCase().includes(q) ||
        item.tenLop.toLowerCase().includes(q) ||
        item.tenKhoa.toLowerCase().includes(q);
      const matchRank = !this.selectedXepLoai || item.xepLoaiHocLuc === this.selectedXepLoai;
      return matchSearch && matchRank;
    });

    switch (this.sortBy) {
      case 'gpa_desc':
        return filtered.sort((a, b) => (b.diemTrungBinh ?? 0) - (a.diemTrungBinh ?? 0));
      case 'gpa_asc':
        return filtered.sort((a, b) => (a.diemTrungBinh ?? 0) - (b.diemTrungBinh ?? 0));
      case 'credits_desc':
        return filtered.sort((a, b) => (b.tinChiTichLuy ?? 0) - (a.tinChiTichLuy ?? 0));
      case 'name_asc':
        return filtered.sort((a, b) => a.hoTen.localeCompare(b.hoTen));
      case 'mssv_asc':
        return filtered.sort((a, b) => a.mssv.localeCompare(b.mssv));
      default:
        return filtered;
    }
  }

  protected get paginatedData(): SinhVienGpaItem[] {
    const start = (this.currentPage - 1) * this.pageSize;
    return this.filteredData.slice(start, start + this.pageSize);
  }

  protected getRankClass(rank: string): string {
    switch (rank) {
      case 'Xuất sắc':
        return 'badge-xuat-sac';
      case 'Khá / Giỏi':
        return 'badge-kha-gioi';
      case 'Trung bình':
        return 'badge-trung-binh';
      case 'Yếu / Cảnh báo':
        return 'badge-yeu';
      default:
        return 'badge-chua-diem';
    }
  }

  protected exportCsv(): void {
    if (this.data.length === 0) return;
    const header = ['Hạng', 'MSSV', 'Họ và tên', 'Lớp', 'Khoa', 'Số môn học', 'Tín chỉ tích lũy', 'GPA', 'Xếp loại'];
    const rows = this.filteredData.map((item, index) => [
      index + 1,
      `"${item.mssv}"`,
      `"${item.hoTen}"`,
      `"${item.tenLop}"`,
      `"${item.tenKhoa}"`,
      item.soMonHoc,
      item.tinChiTichLuy,
      item.diemTrungBinh,
      `"${item.xepLoaiHocLuc}"`,
    ]);

    const csvContent = '\uFEFF' + [header.join(','), ...rows.map((r) => r.join(','))].join('\n');
    const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' });
    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.setAttribute('download', `thong_ke_gpa_${new Date().toISOString().slice(0, 10)}.csv`);
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
  }

  protected printPage(): void {
    window.print();
  }

  private pollTimeout: any = null;

  // === Asynchronous JasperReports + RabbitMQ Export ===
  protected exportPdfReport(): void {
    if (this.exportingPdf) return;

    const lopIdNum = this.selectedLopId ? Number(this.selectedLopId) : undefined;
    this.exportingPdf = true;
    this.cd.markForCheck();
    this.cd.detectChanges();

    this.reportService.requestExportGpa(lopIdNum).subscribe({
      next: (res) => {
        this.pollExportJob(res.jobId, 0);
      },
      error: (err) => {
        this.exportingPdf = false;
        this.toast.error('Lỗi', err.error?.message || 'Không thể gửi yêu cầu xuất báo cáo.');
        this.cd.markForCheck();
        this.cd.detectChanges();
      },
    });
  }

  private pollExportJob(jobId: string, attempt: number): void {
    if (this.pollTimeout) {
      clearTimeout(this.pollTimeout);
      this.pollTimeout = null;
    }

    if (attempt > 30) {
      this.exportingPdf = false;
      this.toast.warning('Thông báo', 'Quá trình xuất báo cáo đang tiếp tục xử lý ngầm.');
      this.cd.markForCheck();
      this.cd.detectChanges();
      return;
    }

    this.pollTimeout = setTimeout(() => {
      this.reportService.getStatus(jobId).subscribe({
        next: (job) => {
          if (job.status === 'COMPLETED') {
            this.toast.success('Thành công', 'Báo cáo PDF đã tạo hoàn tất! Đang tải file về máy...');
            this.downloadReportFile(job.jobId, job.fileName || 'BaoCao_ThongKe_GPA.pdf');
          } else if (job.status === 'FAILED') {
            this.exportingPdf = false;
            this.toast.error('Lỗi', job.errorMessage || 'Lỗi khi tạo báo cáo JasperReports.');
            this.cd.markForCheck();
            this.cd.detectChanges();
          } else {
            this.pollExportJob(jobId, attempt + 1);
          }
        },
        error: () => {
          this.exportingPdf = false;
          this.cd.markForCheck();
          this.cd.detectChanges();
        },
      });
    }, 1200);
  }

  protected downloadReportFile(jobId: string, fileName: string): void {
    this.reportService.downloadBlob(jobId).subscribe({
      next: (blob) => {
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = fileName;
        document.body.appendChild(a);
        a.click();
        document.body.removeChild(a);
        setTimeout(() => window.URL.revokeObjectURL(url), 1000);

        this.exportingPdf = false;
        this.cd.markForCheck();
        this.cd.detectChanges();
      },
      error: () => {
        this.exportingPdf = false;
        this.toast.error('Lỗi', 'Không thể tải file báo cáo.');
        this.cd.markForCheck();
        this.cd.detectChanges();
      },
    });
  }
}

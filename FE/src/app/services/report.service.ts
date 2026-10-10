import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';

export type ReportJobResponse = {
  jobId: string;
  reportType: string;
  title: string;
  status: 'PENDING' | 'PROCESSING' | 'COMPLETED' | 'FAILED';
  fileName?: string;
  fileSize?: number;
  requestedBy: string;
  createdAt: string;
  completedAt?: string;
  errorMessage?: string;
};

@Injectable({ providedIn: 'root' })
export class ReportService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = 'http://localhost:8080/api/reports';

  /**
   * Gửi yêu cầu xuất báo cáo ĐRL bất đồng bộ qua RabbitMQ
   */
  requestExportDrl(dotId: number, lopId: number, format = 'PDF'): Observable<ReportJobResponse> {
    return this.http.post<ReportJobResponse>(`${this.apiUrl}/export-drl`, {
      dotId,
      lopId,
      format,
    });
  }

  /**
   * Gửi yêu cầu xuất báo cáo Thống kê GPA bất đồng bộ qua RabbitMQ
   */
  requestExportGpa(lopId?: number): Observable<ReportJobResponse> {
    const params = lopId ? `?lopId=${lopId}` : '';
    return this.http.post<ReportJobResponse>(`${this.apiUrl}/export-gpa${params}`, {});
  }

  /**
   * Kiểm tra tiến độ xử lý của tác vụ xuất báo cáo
   */
  getStatus(jobId: string): Observable<ReportJobResponse> {
    return this.http.get<ReportJobResponse>(`${this.apiUrl}/status/${jobId}`);
  }

  /**
   * Lấy URL trực tiếp để tải file PDF
   */
  getDownloadUrl(jobId: string): string {
    return `${this.apiUrl}/download/${jobId}`;
  }

  /**
   * Tải file blob báo cáo trực tiếp về máy
   */
  downloadBlob(jobId: string): Observable<Blob> {
    return this.http.get(`${this.apiUrl}/download/${jobId}`, {
      responseType: 'blob',
    });
  }

  /**
   * Lấy danh sách lịch sử các báo cáo đã xuất
   */
  getMyHistory(): Observable<ReportJobResponse[]> {
    return this.http.get<ReportJobResponse[]>(`${this.apiUrl}/my-history`);
  }
}

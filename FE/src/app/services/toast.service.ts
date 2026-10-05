import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

export type ToastType = 'success' | 'error' | 'warning' | 'info';

export interface ToastMessage {
  id: number;
  type: ToastType;
  title: string;
  message: string;
  duration?: number;
}

@Injectable({
  providedIn: 'root',
})
export class ToastService {
  private count = 0;
  private readonly toasts$ = new BehaviorSubject<ToastMessage[]>([]);
  public readonly toasts = this.toasts$.asObservable();

  public show(message: string, type: ToastType = 'info', title?: string, duration = 4500): void {
    const id = ++this.count;
    let defaultTitle = 'Thông báo';
    if (type === 'error') defaultTitle = 'Lỗi dữ liệu';
    else if (type === 'success') defaultTitle = 'Thao tác thành công';
    else if (type === 'warning') defaultTitle = 'Cảnh báo';

    const toast: ToastMessage = {
      id,
      type,
      title: title || defaultTitle,
      message,
      duration,
    };

    const current = this.toasts$.getValue();
    this.toasts$.next([...current, toast]);

    if (duration > 0) {
      setTimeout(() => {
        this.remove(id);
      }, duration);
    }
  }

  public success(message: string, title = 'Thành công'): void {
    this.show(message, 'success', title);
  }

  public error(message: string, title = 'Lỗi'): void {
    this.show(message, 'error', title, 6000);
  }

  public warning(message: string, title = 'Cảnh báo'): void {
    this.show(message, 'warning', title);
  }

  public info(message: string, title = 'Thông báo'): void {
    this.show(message, 'info', title);
  }

  public remove(id: number): void {
    const updated = this.toasts$.getValue().filter((t) => t.id !== id);
    this.toasts$.next(updated);
  }

  public extractError(err: any, fallback = 'Đã có lỗi xảy ra. Vui lòng thử lại.'): string {
    if (!err) return fallback;
    if (err.error) {
      if (typeof err.error === 'string') {
        return err.error;
      }
      if (Array.isArray(err.error.details) && err.error.details.length > 0) {
        if (err.error.message && err.error.message !== 'Validation failed') {
          return err.error.message;
        }
        return err.error.details.join('; ');
      }
      if (err.error.message && err.error.message !== 'Validation failed') {
        return err.error.message;
      }
    }
    return err.message || fallback;
  }
}

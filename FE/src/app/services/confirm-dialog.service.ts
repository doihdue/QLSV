import { Injectable, signal } from '@angular/core';

export type DialogType = 'danger' | 'warning' | 'primary' | 'info' | 'success';

export interface ConfirmOptions {
  title?: string;
  message: string;
  confirmText?: string;
  cancelText?: string;
  type?: DialogType;
}

export interface PromptOptions {
  title: string;
  message?: string;
  defaultValue?: string;
  placeholder?: string;
  confirmText?: string;
  cancelText?: string;
  type?: DialogType;
  inputType?: 'text' | 'textarea';
  required?: boolean;
}

export interface AlertOptions {
  title?: string;
  message: string;
  confirmText?: string;
  type?: DialogType;
}

export interface DialogConfig {
  mode: 'confirm' | 'prompt' | 'alert';
  title: string;
  message: string;
  confirmText: string;
  cancelText?: string;
  type: DialogType;
  inputValue: string;
  placeholder?: string;
  inputType?: 'text' | 'textarea';
  required?: boolean;
  resolve: (value: any) => void;
}

@Injectable({
  providedIn: 'root',
})
export class ConfirmDialogService {
  public readonly currentDialog = signal<DialogConfig | null>(null);

  public confirm(options: ConfirmOptions | string): Promise<boolean> {
    const opts = typeof options === 'string' ? { message: options } : options;
    return new Promise<boolean>((resolve) => {
      this.currentDialog.set({
        mode: 'confirm',
        title: opts.title || (opts.type === 'danger' ? 'Xác nhận xóa' : 'Xác nhận thực hiện'),
        message: opts.message,
        confirmText: opts.confirmText || (opts.type === 'danger' ? 'Xóa bỏ' : 'Đồng ý'),
        cancelText: opts.cancelText || 'Hủy bỏ',
        type: opts.type || (opts.title?.toLowerCase().includes('xóa') ? 'danger' : 'primary'),
        inputValue: '',
        resolve,
      });
    });
  }

  public prompt(options: PromptOptions): Promise<string | null> {
    return new Promise<string | null>((resolve) => {
      this.currentDialog.set({
        mode: 'prompt',
        title: options.title,
        message: options.message || '',
        confirmText: options.confirmText || 'Xác nhận',
        cancelText: options.cancelText || 'Hủy bỏ',
        type: options.type || 'primary',
        inputValue: options.defaultValue || '',
        placeholder: options.placeholder || '',
        inputType: options.inputType || 'textarea',
        required: options.required !== false,
        resolve,
      });
    });
  }

  public alert(options: AlertOptions | string): Promise<void> {
    const opts = typeof options === 'string' ? { message: options } : options;
    return new Promise<void>((resolve) => {
      this.currentDialog.set({
        mode: 'alert',
        title: opts.title || 'Thông báo',
        message: opts.message,
        confirmText: opts.confirmText || 'Đã hiểu',
        type: opts.type || 'info',
        inputValue: '',
        resolve,
      });
    });
  }

  public handleConfirm(value?: any): void {
    const current = this.currentDialog();
    if (current) {
      this.currentDialog.set(null);
      current.resolve(value !== undefined ? value : true);
    }
  }

  public handleCancel(): void {
    const current = this.currentDialog();
    if (current) {
      this.currentDialog.set(null);
      if (current.mode === 'prompt') {
        current.resolve(null);
      } else if (current.mode === 'confirm') {
        current.resolve(false);
      } else {
        current.resolve(undefined);
      }
    }
  }
}

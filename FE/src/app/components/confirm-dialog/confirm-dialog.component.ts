import { Component, ElementRef, ViewChild, HostListener, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ConfirmDialogService, DialogConfig } from '../../services/confirm-dialog.service';

@Component({
  selector: 'app-confirm-dialog',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    @if (dialogService.currentDialog(); as dialog) {
      <div
        class="dialog-backdrop"
        (click)="onBackdropClick($event)"
        tabindex="-1"
      >
        <div
          class="dialog-card"
          [ngClass]="'dialog-' + dialog.type"
          (click)="$event.stopPropagation()"
          role="dialog"
          aria-modal="true"
        >
          <!-- Header with Icon and Title -->
          <div class="dialog-header">
            <div class="icon-badge" [ngClass]="dialog.mode === 'prompt' ? 'badge-prompt' : 'badge-' + dialog.type">
              <!-- Danger Icon (Trash / Alert) -->
              <svg *ngIf="dialog.type === 'danger' && dialog.mode !== 'prompt'" viewBox="0 0 24 24" width="22" height="22" fill="none" stroke="currentColor" stroke-width="2">
                <path stroke-linecap="round" stroke-linejoin="round" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
              </svg>

              <!-- Warning Icon -->
              <svg *ngIf="dialog.type === 'warning' && dialog.mode !== 'prompt'" viewBox="0 0 24 24" width="22" height="22" fill="none" stroke="currentColor" stroke-width="2">
                <path stroke-linecap="round" stroke-linejoin="round" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
              </svg>

              <!-- Success Icon -->
              <svg *ngIf="dialog.type === 'success' && dialog.mode !== 'prompt'" viewBox="0 0 24 24" width="22" height="22" fill="none" stroke="currentColor" stroke-width="2">
                <path stroke-linecap="round" stroke-linejoin="round" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
              </svg>

              <!-- Prompt Icon (Pencil / Input) -->
              <svg *ngIf="dialog.mode === 'prompt'" viewBox="0 0 24 24" width="22" height="22" fill="none" stroke="currentColor" stroke-width="2">
                <path stroke-linecap="round" stroke-linejoin="round" d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z" />
              </svg>

              <!-- Info / Default Confirm Icon -->
              <svg *ngIf="(dialog.type === 'primary' || dialog.type === 'info') && dialog.mode !== 'prompt'" viewBox="0 0 24 24" width="22" height="22" fill="none" stroke="currentColor" stroke-width="2">
                <path stroke-linecap="round" stroke-linejoin="round" d="M8.228 9c.549-1.165 2.03-2 3.772-2 2.21 0 4 1.343 4 3 0 1.4-1.278 2.575-3.006 2.907-.542.104-.994.54-.994 1.093m0 3h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
              </svg>
            </div>

            <div class="header-text">
              <h3 class="dialog-title">{{ dialog.title }}</h3>
            </div>

            <button type="button" class="btn-close" (click)="onCancel()" title="Đóng">
              <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2">
                <path stroke-linecap="round" stroke-linejoin="round" d="M6 18L18 6M6 6l12 12" />
              </svg>
            </button>
          </div>

          <!-- Body Message -->
          <div class="dialog-body">
            <p class="dialog-message" *ngIf="dialog.message">{{ dialog.message }}</p>

            <!-- Prompt Input Area -->
            <div *ngIf="dialog.mode === 'prompt'" class="prompt-input-wrapper">
              <textarea
                *ngIf="dialog.inputType === 'textarea'"
                #promptInput
                class="form-control-dialog"
                [(ngModel)]="dialog.inputValue"
                [placeholder]="dialog.placeholder || 'Nhập nội dung...'"
                rows="3"
                (keydown)="onTextareaKeyDown($event)"
              ></textarea>

              <input
                *ngIf="dialog.inputType !== 'textarea'"
                #promptInput
                type="text"
                class="form-control-dialog"
                [(ngModel)]="dialog.inputValue"
                [placeholder]="dialog.placeholder || 'Nhập nội dung...'"
                (keydown.enter)="onConfirm()"
              />

              <div class="error-hint" *ngIf="showValidation && dialog.required && !dialog.inputValue.trim()">
                ⚠️ Vui lòng nhập nội dung trước khi xác nhận.
              </div>
              <div class="shortcut-hint" *ngIf="dialog.inputType === 'textarea'">
                Mẹo: Nhấn <strong>Ctrl + Enter</strong> để gửi nhanh.
              </div>
            </div>
          </div>

          <!-- Footer Actions -->
          <div class="dialog-footer">
            <button
              *ngIf="dialog.mode !== 'alert'"
              type="button"
              class="btn btn-cancel"
              (click)="onCancel()"
            >
              {{ dialog.cancelText || 'Hủy bỏ' }}
            </button>

            <button
              type="button"
              class="btn btn-confirm"
              [ngClass]="'btn-' + (dialog.mode === 'prompt' ? 'primary' : dialog.type)"
              (click)="onConfirm()"
            >
              {{ dialog.confirmText || 'Xác nhận' }}
            </button>
          </div>
        </div>
      </div>
    }
  `,
  styles: [
    `
      :host {
        display: contents;
      }

      .dialog-backdrop {
        position: fixed;
        inset: 0;
        z-index: 99999;
        background: rgba(15, 23, 42, 0.6);
        backdrop-filter: blur(4px);
        -webkit-backdrop-filter: blur(4px);
        display: flex;
        align-items: center;
        justify-content: center;
        padding: 16px;
        animation: fadeIn 0.15s ease-out forwards;
      }

      @keyframes fadeIn {
        from {
          opacity: 0;
        }
        to {
          opacity: 1;
        }
      }

      .dialog-card {
        background: #ffffff;
        border-radius: 16px;
        box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1), 0 10px 10px -5px rgba(0, 0, 0, 0.04), 0 0 0 1px rgba(0, 0, 0, 0.05);
        width: 100%;
        max-width: 500px;
        overflow: hidden;
        display: flex;
        flex-direction: column;
        animation: scaleUp 0.18s cubic-bezier(0.16, 1, 0.3, 1) forwards;
      }

      @keyframes scaleUp {
        from {
          transform: scale(0.94);
          opacity: 0;
        }
        to {
          transform: scale(1);
          opacity: 1;
        }
      }

      .dialog-header {
        display: flex;
        align-items: center;
        gap: 14px;
        padding: 20px 22px 14px 22px;
      }

      .icon-badge {
        width: 44px;
        height: 44px;
        border-radius: 12px;
        display: flex;
        align-items: center;
        justify-content: center;
        flex-shrink: 0;
      }

      .badge-danger {
        background: #fee2e2;
        color: #dc2626;
      }

      .badge-warning {
        background: #fef3c7;
        color: #d97706;
      }

      .badge-success {
        background: #dcfce7;
        color: #16a34a;
      }

      .badge-primary,
      .badge-info {
        background: #e0f2fe;
        color: #0284c7;
      }

      .badge-prompt {
        background: #ede9fe;
        color: #6366f1;
      }

      .header-text {
        flex: 1;
        min-width: 0;
      }

      .dialog-title {
        margin: 0;
        font-size: 1.15rem;
        font-weight: 700;
        color: #0f172a;
        line-height: 1.35;
      }

      .btn-close {
        background: transparent;
        border: none;
        color: #94a3b8;
        cursor: pointer;
        padding: 6px;
        border-radius: 8px;
        display: flex;
        align-items: center;
        justify-content: center;
        transition: all 0.15s ease;
      }

      .btn-close:hover {
        background: #f1f5f9;
        color: #334155;
      }

      .dialog-body {
        padding: 0 22px 18px 22px;
      }

      .dialog-message {
        margin: 0;
        color: #475569;
        font-size: 0.95rem;
        line-height: 1.55;
        white-space: pre-line;
        word-break: break-word;
      }

      .prompt-input-wrapper {
        margin-top: 14px;
      }

      .form-control-dialog {
        width: 100%;
        box-sizing: border-box;
        border: 1.5px solid #cbd5e1;
        border-radius: 10px;
        padding: 10px 14px;
        font-size: 0.95rem;
        font-family: inherit;
        color: #1e293b;
        background: #f8fafc;
        transition: all 0.2s ease;
        resize: vertical;
      }

      .form-control-dialog:focus {
        outline: none;
        border-color: #2563eb;
        background: #ffffff;
        box-shadow: 0 0 0 3px rgba(37, 99, 235, 0.15);
      }

      .error-hint {
        color: #ef4444;
        font-size: 0.85rem;
        font-weight: 500;
        margin-top: 6px;
      }

      .shortcut-hint {
        color: #94a3b8;
        font-size: 0.8rem;
        margin-top: 6px;
      }

      .dialog-footer {
        display: flex;
        align-items: center;
        justify-content: flex-end;
        gap: 10px;
        padding: 14px 22px 18px 22px;
        background: #f8fafc;
        border-top: 1px solid #f1f5f9;
      }

      .btn {
        padding: 9px 18px;
        border-radius: 9px;
        font-size: 0.92rem;
        font-weight: 600;
        cursor: pointer;
        display: inline-flex;
        align-items: center;
        justify-content: center;
        transition: all 0.15s ease;
        border: none;
      }

      .btn-cancel {
        background: #ffffff;
        color: #475569;
        border: 1px solid #cbd5e1;
      }

      .btn-cancel:hover {
        background: #f1f5f9;
        color: #0f172a;
      }

      .btn-confirm {
        color: #ffffff;
      }

      .btn-danger {
        background: #ef4444;
        box-shadow: 0 4px 12px rgba(239, 68, 68, 0.25);
      }
      .btn-danger:hover {
        background: #dc2626;
      }

      .btn-warning {
        background: #f59e0b;
        box-shadow: 0 4px 12px rgba(245, 158, 11, 0.25);
      }
      .btn-warning:hover {
        background: #d97706;
      }

      .btn-success {
        background: #10b981;
        box-shadow: 0 4px 12px rgba(16, 185, 129, 0.25);
      }
      .btn-success:hover {
        background: #059669;
      }

      .btn-primary,
      .btn-info {
        background: #2563eb;
        box-shadow: 0 4px 12px rgba(37, 99, 235, 0.25);
      }
      .btn-primary:hover,
      .btn-info:hover {
        background: #1d4ed8;
      }
    `,
  ],
})
export class ConfirmDialogComponent {
  protected readonly dialogService = inject(ConfirmDialogService);
  public showValidation = false;

  @ViewChild('promptInput') set promptInput(el: ElementRef | undefined) {
    if (el) {
      setTimeout(() => {
        el.nativeElement?.focus();
        if (el.nativeElement?.select) {
          el.nativeElement.select();
        }
      }, 50);
    }
  }

  @HostListener('document:keydown.escape')
  public onEscape(): void {
    if (this.dialogService.currentDialog()) {
      this.onCancel();
    }
  }

  public onBackdropClick(event: MouseEvent): void {
    if ((event.target as HTMLElement).classList.contains('dialog-backdrop')) {
      this.onCancel();
    }
  }

  public onTextareaKeyDown(event: KeyboardEvent): void {
    if (event.key === 'Enter' && (event.ctrlKey || event.metaKey)) {
      event.preventDefault();
      this.onConfirm();
    }
  }

  public onConfirm(): void {
    const dialog = this.dialogService.currentDialog();
    if (!dialog) return;

    if (dialog.mode === 'prompt') {
      if (dialog.required && (!dialog.inputValue || !dialog.inputValue.trim())) {
        this.showValidation = true;
        return;
      }
      this.dialogService.handleConfirm(dialog.inputValue.trim());
      return;
    }

    this.dialogService.handleConfirm(true);
  }

  public onCancel(): void {
    this.showValidation = false;
    this.dialogService.handleCancel();
  }
}

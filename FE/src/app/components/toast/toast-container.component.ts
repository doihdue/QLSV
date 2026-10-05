import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ToastService, ToastMessage } from '../../services/toast.service';

@Component({
  selector: 'app-toast-container',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="toast-container" aria-live="polite">
      @for (toast of (toastService.toasts | async); track toast.id) {
        <div class="toast-card" [ngClass]="'toast-' + toast.type">
          <div class="toast-indicator"></div>
          <div class="toast-icon">
            @if (toast.type === 'success') {
              <span class="icon-sym">✓</span>
            } @else if (toast.type === 'error') {
              <span class="icon-sym">✕</span>
            } @else if (toast.type === 'warning') {
              <span class="icon-sym">⚠</span>
            } @else {
              <span class="icon-sym">ℹ</span>
            }
          </div>
          <div class="toast-body">
            <h4 class="toast-title">{{ toast.title }}</h4>
            <p class="toast-message">{{ toast.message }}</p>
          </div>
          <button type="button" class="toast-close" (click)="toastService.remove(toast.id)" title="Đóng thông báo">
            ✕
          </button>
        </div>
      }
    </div>
  `,
  styles: [`
    .toast-container {
      position: fixed;
      top: 24px;
      right: 24px;
      z-index: 999999;
      display: flex;
      flex-direction: column;
      gap: 12px;
      pointer-events: none;
      max-width: 420px;
      width: calc(100vw - 32px);
    }

    .toast-card {
      pointer-events: auto;
      position: relative;
      display: flex;
      align-items: flex-start;
      gap: 12px;
      padding: 14px 16px;
      background: #ffffff;
      border-radius: 12px;
      box-shadow: 0 14px 38px rgba(15, 23, 42, 0.16), 0 4px 12px rgba(15, 23, 42, 0.08);
      border: 1px solid #e2e8f0;
      animation: slideInRight 0.28s cubic-bezier(0.16, 1, 0.3, 1) forwards;
      overflow: hidden;
      transition: all 0.2s ease;

      &:hover {
        transform: translateY(-2px);
        box-shadow: 0 18px 42px rgba(15, 23, 42, 0.2);
      }
    }

    @keyframes slideInRight {
      from {
        transform: translateX(115%);
        opacity: 0;
      }
      to {
        transform: translateX(0);
        opacity: 1;
      }
    }

    .toast-indicator {
      position: absolute;
      top: 0;
      left: 0;
      bottom: 0;
      width: 5px;
    }

    .toast-icon {
      flex-shrink: 0;
      width: 32px;
      height: 32px;
      border-radius: 8px;
      display: flex;
      align-items: center;
      justify-content: center;
      font-weight: 800;
      font-size: 1rem;
      margin-top: 1px;
    }

    .toast-body {
      flex: 1;
      min-width: 0;
    }

    .toast-title {
      margin: 0 0 3px;
      font-size: 0.92rem;
      font-weight: 700;
      line-height: 1.25;
      color: #0f172a;
    }

    .toast-message {
      margin: 0;
      font-size: 0.84rem;
      font-weight: 500;
      line-height: 1.45;
      color: #334155;
      word-break: break-word;
    }

    .toast-close {
      flex-shrink: 0;
      background: transparent;
      border: none;
      color: #94a3b8;
      font-size: 0.88rem;
      font-weight: 700;
      cursor: pointer;
      padding: 4px;
      line-height: 1;
      border-radius: 6px;
      transition: all 0.15s;

      &:hover {
        color: #0f172a;
        background: #f1f5f9;
      }
    }

    /* ERROR TOAST (Hình chữ nhật báo lỗi bên phải trên) */
    .toast-error {
      border-color: #fecaca;
      background: #ffffff;

      .toast-indicator {
        background: #dc2626;
      }

      .toast-icon {
        background: #fee2e2;
        color: #dc2626;
      }

      .toast-title {
        color: #991b1b;
      }
    }

    /* SUCCESS TOAST */
    .toast-success {
      border-color: #bbf7d0;
      background: #ffffff;

      .toast-indicator {
        background: #16a34a;
      }

      .toast-icon {
        background: #dcfce7;
        color: #16a34a;
      }

      .toast-title {
        color: #14532d;
      }
    }

    /* WARNING TOAST */
    .toast-warning {
      border-color: #fde68a;
      background: #ffffff;

      .toast-indicator {
        background: #d97706;
      }

      .toast-icon {
        background: #fef3c7;
        color: #d97706;
      }

      .toast-title {
        color: #92400e;
      }
    }

    /* INFO TOAST */
    .toast-info {
      border-color: #bfdbfe;
      background: #ffffff;

      .toast-indicator {
        background: #2563eb;
      }

      .toast-icon {
        background: #dbeafe;
        color: #2563eb;
      }

      .toast-title {
        color: #1e3a8a;
      }
    }
  `],
})
export class ToastContainerComponent {
  protected readonly toastService = inject(ToastService);
}

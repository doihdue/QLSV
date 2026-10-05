import { Component, inject, OnInit } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { ToastService } from '../../services/toast.service';

@Component({
  selector: 'app-login',
  imports: [ReactiveFormsModule],
  templateUrl: './login.component.html',
  styleUrl: './login.component.scss',
})
export class LoginComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly router = inject(Router);
  private readonly authService = inject(AuthService);
  private readonly toastService = inject(ToastService);

  protected readonly form = this.fb.nonNullable.group({
    username: ['', [Validators.required]],
    password: ['', [Validators.required, Validators.minLength(6)]],
    remember: [true],
  });

  ngOnInit(): void {
    this.form.valueChanges.subscribe(() => {
      if (this.form.controls.username.hasError('invalidCredentials')) {
        const uErrors = { ...this.form.controls.username.errors };
        delete uErrors['invalidCredentials'];
        this.form.controls.username.setErrors(Object.keys(uErrors).length ? uErrors : null);
      }
      if (this.form.controls.password.hasError('invalidCredentials')) {
        const pErrors = { ...this.form.controls.password.errors };
        delete pErrors['invalidCredentials'];
        this.form.controls.password.setErrors(Object.keys(pErrors).length ? pErrors : null);
      }
    });
  }

  protected submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      this.toastService.warning('Vui lòng nhập đầy đủ tên đăng nhập và mật khẩu (các ô viền đỏ).', 'Thiếu thông tin đăng nhập');
      return;
    }

    this.authService.login({
      username: this.form.controls.username.value.trim(),
      password: this.form.controls.password.value,
    }).subscribe({
      next: (result) => {
        this.authService.saveSession(result);
        this.toastService.success(`Xin chào ${result.fullName || result.username}! Đăng nhập thành công.`, 'Đăng nhập thành công');
        if (result.role === 'STUDENT') {
          void this.router.navigate(['/sinh-vien/ho-so-ca-nhan']);
        } else if (result.role === 'LECTURER') {
          void this.router.navigate(['/giang-vien/lop-mon-hoc']);
        } else {
          void this.router.navigate(['/dashboard']);
        }
      },
      error: (err) => {
        this.form.controls.username.setErrors({ invalidCredentials: true });
        this.form.controls.password.setErrors({ invalidCredentials: true });
        this.form.controls.username.markAsTouched();
        this.form.controls.password.markAsTouched();
        const msg = this.toastService.extractError(err, 'Tên đăng nhập hoặc mật khẩu không chính xác.');
        this.toastService.error(msg, 'Đăng nhập thất bại');
      },
    });
  }
}

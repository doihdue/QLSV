import { CanActivateFn, Router } from '@angular/router';
import { inject } from '@angular/core';
import { AuthService } from './auth.service';

export const authGuard: CanActivateFn = () => {
  const router = inject(Router);
  return localStorage.getItem('accessToken')
    ? true
    : router.createUrlTree(['/login']);
};

export const adminGuard: CanActivateFn = () => {
  const authService = inject(AuthService);
  const router = inject(Router);
  if (authService.isAdmin) {
    return true;
  }
  return router.createUrlTree(['/dashboard']);
};

export const lecturerGuard: CanActivateFn = () => {
  const authService = inject(AuthService);
  const router = inject(Router);
  if (authService.isLecturer) {
    return true;
  }
  return router.createUrlTree(['/dashboard']);
};

export const studentGuard: CanActivateFn = () => {
  const authService = inject(AuthService);
  const router = inject(Router);
  if (authService.isStudent) {
    return true;
  }
  return router.createUrlTree(['/dashboard']);
};

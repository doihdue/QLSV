import { CommonModule } from '@angular/common';
import { Component, inject, OnDestroy, OnInit } from '@angular/core';
import { NavigationEnd, Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { filter, Subscription } from 'rxjs';
import { AuthService } from '../services/auth.service';
import { NotificationItem, NotificationService } from '../services/notification.service';

type NavItem = {
  label: string;
  path: string;
  icon?: string;
};

type NavGroup = {
  title: string;
  items: NavItem[];
};

@Component({
  selector: 'app-shell',
  imports: [CommonModule, RouterLink, RouterLinkActive, RouterOutlet],
  templateUrl: './shell.component.html',
  styleUrl: './shell.component.scss',
})
export class ShellComponent implements OnInit, OnDestroy {
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);
  private readonly notificationService = inject(NotificationService);

  protected notifications: NotificationItem[] = [];
  protected unreadCount = 0;
  protected showNotifications = false;
  protected mobileSidebarOpen = false;

  private pollTimer: any = null;
  private routerSub: Subscription | null = null;

  ngOnInit(): void {
    this.refreshUnreadCount();
    this.pollTimer = setInterval(() => {
      this.refreshUnreadCount();
    }, 15000);

    // Tự động đóng menu trên mobile khi chuyển trang
    this.routerSub = this.router.events
      .pipe(filter((event) => event instanceof NavigationEnd))
      .subscribe(() => {
        this.mobileSidebarOpen = false;
      });
  }

  ngOnDestroy(): void {
    if (this.pollTimer) {
      clearInterval(this.pollTimer);
    }
    if (this.routerSub) {
      this.routerSub.unsubscribe();
    }
  }

  protected toggleSidebar(): void {
    this.mobileSidebarOpen = !this.mobileSidebarOpen;
  }

  protected closeSidebar(): void {
    this.mobileSidebarOpen = false;
  }

  protected refreshUnreadCount(): void {
    this.notificationService.getUnreadCount().subscribe({
      next: (res) => {
        this.unreadCount = res.unreadCount;
      },
      error: () => {},
    });
  }

  protected toggleNotifications(): void {
    this.showNotifications = !this.showNotifications;
    if (this.showNotifications) {
      this.loadNotifications();
    }
  }

  protected loadNotifications(): void {
    this.notificationService.getMyNotifications().subscribe({
      next: (list) => {
        this.notifications = list;
        this.unreadCount = list.filter((n) => !n.daDoc).length;
      },
      error: () => {},
    });
  }

  protected markAllRead(): void {
    this.notificationService.markAllAsRead().subscribe({
      next: () => {
        this.notifications = this.notifications.map((n) => ({ ...n, daDoc: true }));
        this.unreadCount = 0;
      },
      error: () => {},
    });
  }

  protected handleNotificationClick(item: NotificationItem): void {
    if (!item.daDoc) {
      this.notificationService.markAsRead(item.id).subscribe({
        next: () => {
          item.daDoc = true;
          this.unreadCount = Math.max(0, this.unreadCount - 1);
        },
      });
    }
    this.showNotifications = false;
    if (item.lienKet) {
      void this.router.navigate([item.lienKet]);
    }
  }

  private readonly adminNavGroups: NavGroup[] = [
    {
      title: 'Tổng quan',
      items: [{ label: 'Bảng điều khiển', path: '/dashboard', icon: '📊' }],
    },
    {
      title: 'Quản lý Đào tạo',
      items: [
        { label: 'Quản lý Khoa', path: '/quan-ly/khoa', icon: '🏛️' },
        { label: 'Quản lý Lớp hành chính', path: '/quan-ly/lop', icon: '🏫' },
        { label: 'Quản lý Môn học', path: '/quan-ly/mon-hoc', icon: '📚' },
        { label: 'Quản lý Giảng viên', path: '/quan-ly/giang-vien', icon: '👨‍🏫' },
        { label: 'Quản lý Sinh viên', path: '/quan-ly/sinh-vien', icon: '👨‍🎓' },
      ],
    },
    {
      title: 'Học vụ & Đào tạo',
      items: [{ label: 'Phê duyệt điểm & Đăng ký', path: '/dao-tao/quan-ly-diem', icon: '⚖️' }],
    },
  ];

  private readonly studentNavGroups: NavGroup[] = [
    {
      title: 'Tổng quan',
      items: [{ label: 'Trang chủ', path: '/dashboard', icon: '🏠' }],
    },
    {
      title: 'Cổng Sinh viên',
      items: [
        { label: 'Đăng ký môn học', path: '/sinh-vien/dang-ky-mon-hoc', icon: '✍️' },
        { label: 'Bảng điểm học tập', path: '/sinh-vien/bang-diem', icon: '📑' },
        { label: 'Hồ sơ cá nhân', path: '/sinh-vien/ho-so-ca-nhan', icon: '👤' },
      ],
    },
  ];

  private readonly lecturerNavGroups: NavGroup[] = [
    {
      title: 'Tổng quan',
      items: [{ label: 'Trang chủ', path: '/dashboard', icon: '🏠' }],
    },
    {
      title: 'Cổng Giảng viên',
      items: [
        { label: 'Lớp học phần & Nhập điểm', path: '/giang-vien/lop-mon-hoc', icon: '📋' },
      ],
    },
  ];

  protected get navGroups(): NavGroup[] {
    if (this.authService.isStudent) return this.studentNavGroups;
    if (this.authService.isLecturer) return this.lecturerNavGroups;
    return this.adminNavGroups;
  }

  protected get currentUserName(): string {
    const fullName = this.authService.currentUser?.fullName;
    if (!fullName || fullName.includes('?')) {
      if (this.authService.isStudent) return 'Sinh viên';
      if (this.authService.isLecturer) return 'Giảng viên';
      return 'Quản trị hệ thống';
    }
    return fullName;
  }

  protected get currentUserRole(): string {
    const role = this.authService.currentUser?.role;
    if (role === 'STUDENT') return 'Sinh viên';
    if (role === 'LECTURER') return 'Giảng viên';
    return 'Quản trị viên';
  }

  protected get userInitials(): string {
    const name = this.currentUserName;
    if (!name || name === 'Quản trị hệ thống') return this.authService.isStudent ? 'SV' : 'QT';
    const parts = name.trim().split(/\s+/);
    if (parts.length === 1) return parts[0].substring(0, 2).toUpperCase();
    return (parts[0][0] + parts[parts.length - 1][0]).toUpperCase();
  }

  protected logout(): void {
    this.authService.logout();
    void this.router.navigate(['/login']);
  }
}

import { Injectable, inject, signal, computed } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { jwtDecode } from 'jwt-decode';
import { environment } from '../../../environments/environment';
import { UserSession, UserRole } from './sessionUser.model';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private http = inject(HttpClient);
  private router = inject(Router);
  private apiUrl = `${environment.apiUrl}/auth`;
  private readonly TOKEN_KEY = 'wms_token';

  private _currentUser = signal<UserSession | null>(null);

  public currentUser = computed(() => this._currentUser());
  public isAuthenticated = computed(() => !!this._currentUser());

  constructor() {
    this.loadSession();
  }

  login(credentials: {username: string, password: string}) {
    return this.http.post<{token: string}>(`${this.apiUrl}/login`, credentials);
  }

  setSession(token: string) {
    localStorage.setItem(this.TOKEN_KEY, token);
    this.decodeAndSetUser(token);
  }

  logout() {
    localStorage.removeItem(this.TOKEN_KEY);
    this._currentUser.set(null);
    this.router.navigate(['/login']);
  }

  getToken(): string | null {
    return localStorage.getItem(this.TOKEN_KEY);
  }

  private loadSession() {
    const token = this.getToken();
    if (!token) return;
    this.decodeAndSetUser(token);
  }

  private decodeAndSetUser(token: string) {
    try {
      const decoded: any = jwtDecode(token);
        
      if (decoded.exp * 1000 < Date.now()) {
        this.logout();
        return;
      }

      let cleanRole = decoded.role;
      if (cleanRole && typeof cleanRole === 'string') {
        cleanRole = cleanRole.replace('ROLE_', '');
      }

      this._currentUser.set({
      username: decoded.sub,
      role: cleanRole as UserRole, 
      tenantId: decoded.tenantId,
      token: token
    });


    } catch (e) {
      this.logout();
    }
  }
}
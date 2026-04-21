import { Component, inject, signal, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../../../core/auth/auth.service';
import { FooterComponent } from '../../../../core/layout/components/footer/footer.component';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="min-h-screen flex items-center justify-center bg-slate-100 relative overflow-hidden">
      
      <div class="absolute top-[-10%] left-[-10%] w-96 h-96 bg-indigo-400 rounded-full mix-blend-multiply filter blur-3xl opacity-20 animate-blob"></div>
      <div class="absolute bottom-[-10%] right-[-10%] w-96 h-96 bg-blue-400 rounded-full mix-blend-multiply filter blur-3xl opacity-20 animate-blob animation-delay-2000"></div>

      <div class="relative w-full max-w-sm bg-white rounded-2xl shadow-xl p-8 border border-slate-100">
        
        <div class="text-center mb-8">
          <div class="inline-flex items-center justify-center w-14 h-14 rounded-xl bg-indigo-600 text-white mb-4 shadow-lg shadow-indigo-500/30">
            <i class="bi bi-box-seam-fill text-2xl"></i>
          </div>
          <h2 class="text-2xl font-bold text-slate-800">WMS Enterprise</h2>
          <p class="text-slate-500 text-sm mt-2">Ingresa tus credenciales</p>
        </div>

        <form (ngSubmit)="onLogin()" class="space-y-5">
          
          <div>
            <label class="block text-xs font-bold text-slate-500 uppercase mb-1">Usuario</label>
            <div class="relative">
              <span class="absolute inset-y-0 left-0 pl-3 flex items-center text-slate-400">
                <i class="bi bi-person"></i>
              </span>
              <input type="text" [(ngModel)]="creds.username" name="username" required 
                     class="pl-10 w-full rounded-lg border-slate-200 bg-slate-50 focus:bg-white focus:border-indigo-500 focus:ring-indigo-500 py-2.5 text-sm transition-all"
                     placeholder="ej: admin">
            </div>
          </div>

          <div>
            <label class="block text-xs font-bold text-slate-500 uppercase mb-1">Contraseña</label>
            <div class="relative">
              <span class="absolute inset-y-0 left-0 pl-3 flex items-center text-slate-400">
                <i class="bi bi-lock"></i>
              </span>
              <input type="password" [(ngModel)]="creds.password" name="password" required 
                     class="pl-10 w-full rounded-lg border-slate-200 bg-slate-50 focus:bg-white focus:border-indigo-500 focus:ring-indigo-500 py-2.5 text-sm transition-all"
                     placeholder="•••••••">
            </div>
          </div>

          @if (errorMsg()) {
            <div class="p-3 rounded-lg bg-red-50 border border-red-100 text-red-600 text-xs flex items-center gap-2 animate-pulse">
              <i class="bi bi-exclamation-circle-fill"></i>
              {{ errorMsg() }}
            </div>
          }

          <button type="submit" [disabled]="isLoading()" 
                  class="w-full bg-slate-900 hover:bg-black text-white font-bold py-3 rounded-lg shadow-lg hover:shadow-xl transition-all transform active:scale-95 disabled:opacity-70 disabled:cursor-not-allowed flex justify-center items-center gap-2">
            @if (isLoading()) {
              <span class="w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin"></span>
            }
            <span>{{ isLoading() ? 'Validando...' : 'Iniciar Sesión' }}</span>
          </button>

        </form>

        <div class="mt-8 text-center text-[10px] text-slate-400">
          &copy; 2026 WMS System. V1.0 Hexagonal.
        </div>
      </div>
    </div>
  `,
  styles: [`
    .animate-blob { animation: blob 7s infinite; }
    @keyframes blob {
      0% { transform: translate(0px, 0px) scale(1); }
      33% { transform: translate(30px, -50px) scale(1.1); }
      66% { transform: translate(-20px, 20px) scale(0.9); }
      100% { transform: translate(0px, 0px) scale(1); }
    }
  `]
})
export class LoginComponent {
  private authService = inject(AuthService);
  private router = inject(Router);

  creds = { username: '', password: '' };
  isLoading = signal(false);
  errorMsg = signal('');

  onLogin() {
    this.isLoading.set(true);
    this.errorMsg.set('');

    this.authService.login(this.creds).subscribe({
      next: (response) => {
        this.authService.setSession(response.token);
        
        const user = this.authService.currentUser();
        this.redirectBasedOnRole(user?.role);
      },
      error: () => {
        this.errorMsg.set('Credenciales inválidas o usuario inactivo.');
        this.isLoading.set(false);
      }
    });
  }

  private redirectBasedOnRole(role: string | undefined) {
    switch (role) {
      case 'SUPER_ADMIN':
        this.router.navigate(['/saas/tenants']);
        break;
      case 'ADMIN':
        this.router.navigate(['/admin/dashboard']);
        break;
      case 'OPERATOR':
        this.router.navigate(['/operation/receive']);
        break;
      default:
        this.errorMsg.set('Rol no reconocido. Contacte soporte.');
        this.isLoading.set(false);
    }
  }
}
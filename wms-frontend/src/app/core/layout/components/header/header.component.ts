// core/layout/components/header/header.component.ts
import { Component, Input, inject, signal, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../../auth/auth.service';

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [CommonModule],
  template: `
    <header class="h-16 flex-none bg-white border-b border-gray-200 flex items-center justify-between px-6 z-40 shadow-sm">
      
      <div class="flex items-center gap-3">
        <div class="w-8 h-8 rounded-lg bg-indigo-600 flex items-center justify-center text-white shadow-lg shadow-indigo-500/30">
          <i class="bi bi-box-seam-fill text-sm"></i>
        </div>
        <div class="flex flex-col">
           <span class="font-bold text-lg tracking-tight text-gray-900 leading-none">
             {{ softwareName }}
           </span>
           <span *ngIf="subtitle" class="text-[10px] text-gray-500 font-medium uppercase tracking-wide">
             {{ subtitle }}
           </span>
        </div>
      </div>

      <div class="flex items-center gap-6">
        
        <div class="hidden md:flex flex-col items-end text-xs text-gray-500 border-r pr-6 border-gray-200">
          <span class="font-bold text-gray-700">{{ currentDate | date:'HH:mm:ss' }}</span>
          <span>{{ currentDate | date:'fullDate' }}</span>
        </div>

        <div class="flex items-center gap-3">
           <div class="text-right hidden sm:block">
              <p class="text-sm font-bold text-gray-800 leading-none mb-1">
                 {{ auth.currentUser()?.username || 'Usuario' }}
              </p>
              <button (click)="auth.logout()" class="text-xs font-medium text-red-500 hover:text-red-700 flex items-center justify-end gap-1 ml-auto transition-colors">
                 Cerrar sesión <i class="bi bi-box-arrow-right"></i>
              </button>
           </div>
           <div class="h-9 w-9 rounded-full bg-indigo-50 border border-indigo-100 flex items-center justify-center text-indigo-700 font-bold">
              {{ auth.currentUser()?.username?.charAt(0) | uppercase }}
           </div>
        </div>
      </div>
    </header>
  `
})
export class HeaderComponent implements OnInit, OnDestroy {
  @Input() softwareName: string = 'WMS Enterprise';
  @Input() subtitle: string = '';
  
  public auth = inject(AuthService);
  currentDate = new Date();
  private timer: any;

  ngOnInit() {
    // Actualiza la hora cada segundo
    this.timer = setInterval(() => {
      this.currentDate = new Date();
    }, 1000);
  }

  ngOnDestroy() {
    if (this.timer) clearInterval(this.timer);
  }
}
import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { MenuItem } from '../../config/menu.config';

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [CommonModule, RouterLink, RouterLinkActive],
  template: `
    <aside class="h-full w-64 flex flex-col transition-all duration-300 border-r"
           [ngClass]="theme === 'dark' 
              ? 'bg-slate-900 border-slate-800' 
              : 'bg-white border-gray-200'">
      
      <nav class="flex-1 overflow-y-auto py-6 px-4 space-y-1 custom-scrollbar">
        <ng-container *ngFor="let item of menuItems">
          
          <div *ngIf="item.category" 
               class="px-3 mt-6 mb-3 text-[11px] font-bold uppercase tracking-wider select-none"
               [ngClass]="theme === 'dark' 
                  ? 'text-slate-500' 
                  : 'text-slate-500'"> 
            {{ item.category }}
          </div>

          <a [routerLink]="item.route" 
             routerLinkActive="active-link"
             [routerLinkActiveOptions]="{exact: false}"
             class="group flex items-center px-3 py-2.5 text-sm font-medium rounded-lg transition-all duration-200 border-l-[3px] border-transparent"
             [ngClass]="theme === 'dark' 
                ? 'text-slate-400 hover:bg-white/10 hover:text-white' 
                : 'text-slate-600 hover:bg-indigo-50 hover:text-indigo-700'">
            
            <i [class]="'bi ' + item.icon + ' mr-3 text-lg transition-colors duration-200'"
               [ngClass]="theme === 'dark' 
                  ? 'text-slate-500 group-hover:text-white' 
                  : 'text-slate-400 group-hover:text-indigo-600'">
            </i>
            
            <span>{{ item.label }}</span>
          </a>

        </ng-container>
      </nav>
    </aside>
  `
})
export class SidebarComponent {
  @Input() menuItems: MenuItem[] = [];
  @Input() theme: 'dark' | 'light' = 'light';
}
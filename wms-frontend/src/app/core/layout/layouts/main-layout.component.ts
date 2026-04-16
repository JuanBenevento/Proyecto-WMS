import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MenuItem } from '../config/menu.config';
import { HeaderComponent } from '../components/header/header.component';
import { SidebarComponent } from '../components/sidebar/sidebar.component';
import { FooterComponent } from '../components/footer/footer.component';

@Component({
  selector: 'app-main-layout',
  standalone: true,
  imports: [CommonModule, HeaderComponent, SidebarComponent, FooterComponent],
  template: `
    <div class="flex flex-col h-screen w-full overflow-hidden bg-gray-50">
      
      <app-header 
        [softwareName]="appTitle" 
        [subtitle]="appSubtitle">
      </app-header>

      <div class="flex flex-1 overflow-hidden relative">
        
        <app-sidebar 
            class="flex-none hidden md:block"
            [menuItems]="menu" 
            [theme]="sidebarTheme">
        </app-sidebar>

        <main class="flex-1 overflow-y-auto overflow-x-hidden p-4 md:p-6 scroll-smooth relative w-full">
            <ng-content></ng-content>
        </main>

      </div>

      <app-footer [mode]="'transparent'"></app-footer>
      
    </div>
  `
})
export class MainLayoutComponent {
  @Input() menu: MenuItem[] = [];
  @Input() appTitle: string = 'WMS';
  @Input() appSubtitle: string = '';
  @Input() sidebarTheme: 'dark' | 'light' = 'light';
}
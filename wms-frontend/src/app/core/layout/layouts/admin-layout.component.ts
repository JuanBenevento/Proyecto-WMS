// core/layout/layouts/admin-layout.component.ts
import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { ADMIN_MENU } from '../config/menu.config';
import { MainLayoutComponent } from './main-layout.component';

@Component({
  selector: 'app-admin-layout',
  standalone: true,
  imports: [RouterOutlet, MainLayoutComponent],
  template: `
    <app-main-layout 
        [menu]="menu" 
        appTitle="WMS Enterprise" 
        appSubtitle="Panel de Administración"
        sidebarTheme="light">
        
        <router-outlet></router-outlet>

    </app-main-layout>
  `
})
export class AdminLayoutComponent {
  menu = ADMIN_MENU;
}
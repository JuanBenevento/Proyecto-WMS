import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { SAAS_MENU } from '../config/menu.config';
import { MainLayoutComponent } from './main-layout.component';

@Component({
  selector: 'app-saas-layout',
  standalone: true,
  imports: [RouterOutlet, MainLayoutComponent],
  template: `
    <app-main-layout 
        [menu]="menu" 
        appTitle="WMS Master Control" 
        appSubtitle="Gestión de clientes (SaaS)"
        sidebarTheme="dark"> 
        
        <router-outlet></router-outlet>

    </app-main-layout>
  `
})
export class SaasLayoutComponent {
  menu = SAAS_MENU;
}
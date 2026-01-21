import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { OPERATOR_MENU } from '../config/menu.config';
import { MainLayoutComponent } from './main-layout.component';

@Component({
  selector: 'app-operator-layout',
  standalone: true,
  imports: [RouterOutlet, MainLayoutComponent],
  template: `
    <app-main-layout 
        [menu]="menu" 
        appTitle="WMS Terminal" 
        appSubtitle="Operaciones de Piso"
        sidebarTheme="dark"> <div class="flex justify-center h-full">
            <div class="w-full max-w-4xl bg-white/50 rounded-xl p-2 md:p-4 min-h-min">
                <router-outlet></router-outlet>
            </div>
        </div>

    </app-main-layout>
  `
})
export class OperatorLayoutComponent {
  menu = OPERATOR_MENU;
}
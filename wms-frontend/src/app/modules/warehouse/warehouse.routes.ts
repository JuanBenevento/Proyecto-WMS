import { Routes } from '@angular/router';
import { DesignerPageComponent } from './ui/pages/designer-page/designer-page.component';
import { MonitorPageComponent } from './ui/pages/monitor-page/monitor-page.component';

export const WAREHOUSE_ROUTES: Routes = [
    { 
        path: 'dashboard', 
        component: MonitorPageComponent,
        title: 'Monitor de Planta | WMS'
    },
    { 
        path: 'designer', 
        component: DesignerPageComponent,
        title: 'Editor de Layout | WMS'
    },
    { 
        path: '', 
        redirectTo: 'dashboard', 
        pathMatch: 'full' 
    }
];
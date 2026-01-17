import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth-guard';
import { roleGuard } from './core/guards/role-guard';
import { InventoryRepository } from './modules/inventory/domain/ports/repository/inventory.repository';
import { InventoryApiAdapter } from './modules/inventory/infrastructure/adapters/inventory-api.adapter';
import { ReceiveInventoryUseCase } from './modules/inventory/application/usecases/inbound/receive-inventory.usecase';
import { MoveInventoryUseCase } from './modules/inventory/application/usecases/internal/move-inventory.usecase';
import { SuggestLocationUseCase } from './modules/inventory/application/usecases/internal/suggest-location.usecase';
import { AllocateStockUseCase } from './modules/inventory/application/usecases/outbound/allocate-stock.usecase';
import { ShipStockUseCase } from './modules/inventory/application/usecases/outbound/ship-stock.usecase';
import { GetInventoryUseCase } from './modules/inventory/application/usecases/query/get-inventory.usecase';


const INVENTORY_PROVIDERS = [
  { provide: InventoryRepository, useClass: InventoryApiAdapter },
  ReceiveInventoryUseCase,
  MoveInventoryUseCase,
  SuggestLocationUseCase,
  AllocateStockUseCase,
  ShipStockUseCase,
  GetInventoryUseCase
];

export const routes: Routes = [
  { 
    path: 'login', 
    loadComponent: () => import('./modules/auth/ui/login/login.component').then(m => m.LoginComponent) 
  },

  // --- SUPER ADMIN (SaaS) ---
  {
    path: 'saas',
    loadComponent: () => import('./core/layout/layouts/saas-layout.component').then(m => m.SaasLayoutComponent),
    canActivate: [authGuard, roleGuard],
    data: { role: 'SUPER_ADMIN' }, 
    children: [
      { 
        path: 'tenants', 
        loadComponent: () => import('./modules/super-admin/ui/saas-dashboard/saas-dashboard.component').then(m => m.SaasDashboardComponent) 
      },
      { path: '', redirectTo: 'tenants', pathMatch: 'full' }
    ]
  },

  // --- ADMIN (Gerente) ---
  {
    path: 'admin',
    loadComponent: () => import('./core/layout/layouts/admin-layout.component').then(m => m.AdminLayoutComponent),
    canActivate: [authGuard, roleGuard],
    data: { role: 'ADMIN' }, 
    providers: [...INVENTORY_PROVIDERS],
    children: [
      { 
        path: 'dashboard', 
        loadComponent: () => import('./modules/warehouse/ui/warehouse-map/warehouse-map.component').then(m => m.WarehouseMapComponent) 
      },
      { 
        path: 'products', 
        loadChildren: () => import('./modules/product/product.routes').then(m => m.PRODUCT_ROUTES) 
      },
      { 
        path: 'locations', 
        loadChildren: () => import('./modules/location/location.routes').then(m => m.LOCATION_ROUTES) 
      },
      { 
        path: 'audit', 
        loadChildren: () => import('./modules/audit/audit.routes').then(m => m.AUDIT_ROUTES) 
      },
      { 
        path: 'users', 
        loadComponent: () => import('./modules/admin/ui/user-management/user-management.component').then(m => m.UserManagementComponent) 
      },
   
      { 
        path: 'receive', 
        loadComponent: () => import('./modules/inventory/ui/inventory-receive/receive.component').then(m => m.InventoryReceiveComponent) 
      },
      { 
        path: 'move',
        loadComponent: () => import('./modules/inventory/ui/inventory-move/move.component').then(m => m.InventoryMoveComponent) 
      },
      { 
        path: 'picking', 
        loadComponent: () => import('./modules/inventory/ui/inventory-dispatch/dispatch.component').then(m => m.InventoryDispatchComponent) 
      },
      { 
        path: 'inventory', 
        loadComponent: () => import('./modules/inventory/ui/inventory-list/inventory-list.component').then(m => m.InventoryListComponent) 
      },
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' }
    ]
  },

  // --- OPERATOR (Operario) ---
  {
    path: 'operation',
    loadComponent: () => import('./core/layout/layouts/operator-layout.component').then(m => m.OperatorLayoutComponent),
    canActivate: [authGuard, roleGuard],
    data: { role: 'OPERATOR' },
    providers: [...INVENTORY_PROVIDERS],
    children: [
      { 
        path: 'receive', 
        loadComponent: () => import('./modules/inventory/ui/inventory-receive/receive.component').then(m => m.InventoryReceiveComponent) 
      },
      { 
        path: 'move', 
        loadComponent: () => import('./modules/inventory/ui/inventory-move/move.component').then(m => m.InventoryMoveComponent) 
      },
      { 
        path: 'dispatch', 
        loadComponent: () => import('./modules/inventory/ui/inventory-dispatch/dispatch.component').then(m => m.InventoryDispatchComponent) 
      },
      { 
        path: 'inventory', 
        loadComponent: () => import('./modules/inventory/ui/inventory-list/inventory-list.component').then(m => m.InventoryListComponent) 
      },
      { path: '', redirectTo: 'receive', pathMatch: 'full' }
    ]
  },

  // Fallback
  { path: '**', redirectTo: 'login' }
];
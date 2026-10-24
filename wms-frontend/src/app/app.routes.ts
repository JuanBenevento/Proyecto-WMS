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
import { GetLayoutUseCase } from './modules/warehouse/application/usecases/get-layout.usecase';
import { SaveLayoutUseCase } from './modules/warehouse/application/usecases/save-layout.usecase';
import { LayoutRepository } from './modules/warehouse/domain/repositories/layout.repository';
import { LayoutHttpRepository } from './modules/warehouse/infrastructure/repositories/layout-http.repository';
import { GetRackSummaryUseCase } from './modules/warehouse/application/usecases/get-rack-summary.usecase';
import { OrderRepository } from './modules/orders/domain/ports/repository/order.repository';
import { OrderRepositoryAdapter } from './modules/orders/infrastructure/adapters/order-repository.adapter';
import { ManageOrderUseCase } from './modules/orders/application/usecases/manage-order.usecase';
import { GetDashboardKpisUseCase } from './modules/dashboard/application/usecases/get-dashboard-kpis.usecase';
import { DashboardRepositoryAdapter } from './modules/dashboard/infrastructure/adapters/dashboard.adapter';


const INVENTORY_PROVIDERS = [
  { provide: InventoryRepository, useClass: InventoryApiAdapter },
  ReceiveInventoryUseCase,
  MoveInventoryUseCase,
  SuggestLocationUseCase,
  AllocateStockUseCase,
  ShipStockUseCase,
  GetInventoryUseCase
];

const WAREHOUSE_PROVIDERS = [
  { provide: LayoutRepository, useClass: LayoutHttpRepository},
  GetLayoutUseCase,
  SaveLayoutUseCase,
  GetRackSummaryUseCase
];

const ORDERS_PROVIDERS = [
  { provide: OrderRepository, useClass: OrderRepositoryAdapter },
  ManageOrderUseCase
];

const DASHBOARD_PROVIDERS = [
  GetDashboardKpisUseCase,
  DashboardRepositoryAdapter
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
    loadChildren: () => import('./modules/super-admin/super-admin.routes').then(m => m.SUPER_ADMIN_ROUTES)
  },

  // --- ADMIN (Gerente) ---
  {
    path: 'admin',
    loadComponent: () => import('./core/layout/layouts/admin-layout.component').then(m => m.AdminLayoutComponent),
    canActivate: [authGuard, roleGuard],
    data: { role: 'ADMIN' }, 
    providers: [...INVENTORY_PROVIDERS, ...WAREHOUSE_PROVIDERS, ...ORDERS_PROVIDERS, ...DASHBOARD_PROVIDERS],
    children: [
      { 
        path: 'dashboard', 
        loadComponent: () => import('./modules/dashboard/ui/dashboard-page/dashboard-page.component').then(m => m.DashboardPageComponent) 
      },
      {
        path: 'warehouse',
        loadChildren: () => import('./modules/warehouse/warehouse.routes').then(m => m.WAREHOUSE_ROUTES)
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
        path: 'orders', 
        loadChildren: () => import('./modules/orders/orders.routes').then(m => m.ORDERS_ROUTES) 
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
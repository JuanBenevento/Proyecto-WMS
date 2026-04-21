import { Routes } from '@angular/router';
import { OrderRepository } from './domain/ports/repository/order.repository';
import { OrderRepositoryAdapter } from './infrastructure/adapters/order-repository.adapter';
import { ManageOrderUseCase } from './application/usecases/manage-order.usecase';
import { OrderListComponent } from './ui/order-list/order-list.component';
import { OrderDetailComponent } from './ui/order-detail/order-detail.component';
import { OrderFormComponent } from './ui/order-form/order-form.component';

export const ORDERS_ROUTES: Routes = [
  {
    path: '',
    // INYECCIÓN DE DEPENDENCIAS A NIVEL DE RUTA (Environment Injector)
    providers: [
      // 1. Caso de Uso
      ManageOrderUseCase,
      // 2. Wiring: Abstracto -> Implementación
      { provide: OrderRepository, useClass: OrderRepositoryAdapter }
    ],
    children: [
      { path: '', component: OrderListComponent }, // Ruta por defecto (Listado)
      { path: 'create', component: OrderFormComponent },
      { path: 'detail/:orderId', component: OrderDetailComponent }
    ]
  }
];
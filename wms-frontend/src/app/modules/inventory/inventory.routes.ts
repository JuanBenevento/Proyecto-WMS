import { Routes } from '@angular/router';
import { InventoryRepository } from './domain/ports/repository/inventory.repository';
import { InventoryApiAdapter } from './infrastructure/adapters/inventory-api.adapter';
import { ReceiveInventoryUseCase } from './application/usecases/inbound/receive-inventory.usecase';
import { MoveInventoryUseCase } from './application/usecases/internal/move-inventory.usecase';
import { SuggestLocationUseCase } from './application/usecases/internal/suggest-location.usecase';
import { AllocateStockUseCase } from './application/usecases/outbound/allocate-stock.usecase';
import { ShipStockUseCase } from './application/usecases/outbound/ship-stock.usecase';
import { GetInventoryUseCase } from './application/usecases/query/get-inventory.usecase';
import { InventoryReceiveComponent } from './ui/inventory-receive/receive.component';
import { InventoryMoveComponent } from './ui/inventory-move/move.component';
import { InventoryDispatchComponent } from './ui/inventory-dispatch/dispatch.component';
import { InventoryListComponent } from './ui/inventory-list/inventory-list.component';

export const INVENTORY_ROUTES: Routes = [
  {
    path: '',
    providers: [
      { provide: InventoryRepository, useClass: InventoryApiAdapter },
      ReceiveInventoryUseCase,
      MoveInventoryUseCase,
      SuggestLocationUseCase,
      AllocateStockUseCase,
      ShipStockUseCase,
      GetInventoryUseCase
    ],
    children: [
      { path: 'list', component: InventoryListComponent }, 
      { path: 'receive', component: InventoryReceiveComponent },
      { path: 'move', component: InventoryMoveComponent },
      { path: 'dispatch', component: InventoryDispatchComponent }
    ]
  }
];
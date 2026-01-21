import { Routes } from '@angular/router';
import { ProductRepository } from './domain/ports/repository/product.repository';
import { ProductRepositoryAdapter } from './infrastructure/adapters/product-repository.adapter';
import { ManageProductUseCase } from './application/usecases/manage-product.usecase';
import { ProductListComponent } from './ui/product-list/product-list.component';
import { ProductFormComponent } from './ui/product-form/product-form.component';

export const PRODUCT_ROUTES: Routes = [
  {
    path: '',
    // INYECCIÓN DE DEPENDENCIAS A NIVEL DE RUTA (Environment Injector)
    providers: [
      // 1. Caso de Uso
      ManageProductUseCase,
      // 2. Wiring: Abstracto -> Implementación
      { provide: ProductRepository, useClass: ProductRepositoryAdapter }
    ],
    children: [
      { path: '', component: ProductListComponent }, // Ruta por defecto (Listado)
      { path: 'create', component: ProductFormComponent },
      { path: 'edit/:sku', component: ProductFormComponent }
    ]
  }
];
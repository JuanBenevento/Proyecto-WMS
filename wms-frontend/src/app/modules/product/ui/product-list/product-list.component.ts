import { Component, OnInit, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { ManageProductUseCase } from '../../application/usecases/manage-product.usecase';
import { ProductModel } from '../../domain/models/product.model';
import Swal from 'sweetalert2';

@Component({
  selector: 'app-product-list',
  standalone: true,
  imports: [CommonModule, RouterModule],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './product-list.component.html'
})
export class ProductListComponent implements OnInit {
  products: ProductModel[] = [];

  constructor(private manageProductUseCase: ManageProductUseCase) {}

  ngOnInit(): void {
    this.loadProducts();
  }

  loadProducts(): void {
    this.manageProductUseCase.getAll().subscribe(data => this.products = data);
  }

  deleteProduct(sku: string): void {
    Swal.fire({
      title: '¿Eliminar producto?',
      text: "No podrás revertir esto si tiene stock asociado.",
      icon: 'warning',
      showCancelButton: true,
      confirmButtonText: 'Sí, eliminar'
    }).then((result) => {
      if (result.isConfirmed) {
        this.manageProductUseCase.delete(sku).subscribe({
          next: () => {
            Swal.fire('Eliminado', 'El producto ha sido eliminado.', 'success');
            this.loadProducts();
          },
          error: () => Swal.fire('Error', 'No se puede eliminar (posiblemente tiene stock).', 'error')
        });
      }
    });
  }
}
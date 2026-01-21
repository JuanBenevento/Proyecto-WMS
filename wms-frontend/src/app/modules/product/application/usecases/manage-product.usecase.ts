import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ProductRepository } from '../../domain/ports/repository/product.repository';
import { ProductModel } from '../../domain/models/product.model';
import { CreateProductCommand } from '../../domain/ports/commands/create-product.command';

@Injectable()
export class ManageProductUseCase {
  // Inyectamos el Puerto (Abstracto)
  constructor(private productRepository: ProductRepository) {}

  getAll(): Observable<ProductModel[]> {
    return this.productRepository.getAll();
  }

  getBySku(sku: string): Observable<ProductModel> {
    return this.productRepository.getBySku(sku);
  }

  create(command: CreateProductCommand): Observable<ProductModel> {
    return this.productRepository.create(command);
  }

  update(sku: string, command: CreateProductCommand): Observable<ProductModel> {
    return this.productRepository.update(sku, command);
  }

  delete(sku: string): Observable<void> {
    return this.productRepository.delete(sku);
  }
}
import { Observable } from 'rxjs';
import { ProductModel } from '../../models/product.model';
import { CreateProductCommand } from '../commands/create-product.command';

export abstract class ProductRepository {
  abstract getAll(): Observable<ProductModel[]>;
  abstract getBySku(sku: string): Observable<ProductModel>;
  abstract create(command: CreateProductCommand): Observable<ProductModel>;
  abstract update(sku: string, command: CreateProductCommand): Observable<ProductModel>;
  abstract delete(sku: string): Observable<void>;
}
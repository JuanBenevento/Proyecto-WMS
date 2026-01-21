import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { ProductRepository } from '../../domain/ports/repository/product.repository';
import { ProductModel } from '../../domain/models/product.model';
import { CreateProductCommand } from '../../domain/ports/commands/create-product.command';
import { ProductDto } from '../dtos/product.dto';
import { ProductMapper } from '../mappers/product.mapper';
import { environment } from '../../../../../environments/environment';

@Injectable() 
export class ProductRepositoryAdapter extends ProductRepository {
  private readonly API_URL = `${environment.apiUrl}/products`;

  constructor(private http: HttpClient) {
    super();
  }

  getAll(): Observable<ProductModel[]> {
    return this.http.get<ProductDto[]>(this.API_URL).pipe(
      map(dtos => dtos.map(ProductMapper.toDomain))
    );
  }

  getBySku(sku: string): Observable<ProductModel> {
    return this.http.get<ProductDto>(`${this.API_URL}/${sku}`).pipe(
      map(ProductMapper.toDomain)
    );
  }

  create(command: CreateProductCommand): Observable<ProductModel> {
    return this.http.post<ProductDto>(this.API_URL, command).pipe(
      map(ProductMapper.toDomain)
    );
  }

  update(sku: string, command: CreateProductCommand): Observable<ProductModel> {
    return this.http.put<ProductDto>(`${this.API_URL}/${sku}`, command).pipe(
      map(ProductMapper.toDomain)
    );
  }

  delete(sku: string): Observable<void> {
    return this.http.delete<void>(`${this.API_URL}/${sku}`);
  }
}
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { InventoryRepository } from '../../../domain/ports/repository/inventory.repository';

@Injectable()
export class SuggestLocationUseCase {
  constructor(private repo: InventoryRepository) {}
  execute(sku: string, qty: number): Observable<string> { return this.repo.suggestLocation(sku, qty); }
}
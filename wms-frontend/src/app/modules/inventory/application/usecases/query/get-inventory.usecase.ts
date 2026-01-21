import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { InventoryRepository } from '../../../domain/ports/repository/inventory.repository';
import { InventoryItemModel } from '../../../domain/models/inventory-item.model';

@Injectable()
export class GetInventoryUseCase {
  constructor(private repo: InventoryRepository) {}
  execute(): Observable<InventoryItemModel[]> { return this.repo.getAll(); }
}
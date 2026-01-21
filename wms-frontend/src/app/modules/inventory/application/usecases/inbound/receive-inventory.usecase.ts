import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { InventoryRepository } from '../../../domain/ports/repository/inventory.repository';
import { ReceiveCommand } from '../../../domain/ports/commands/receive.command';
import { InventoryItemModel } from '../../../domain/models/inventory-item.model';

@Injectable()
export class ReceiveInventoryUseCase {
  constructor(private repo: InventoryRepository) {}
  execute(cmd: ReceiveCommand): Observable<InventoryItemModel> { return this.repo.receive(cmd); }
}
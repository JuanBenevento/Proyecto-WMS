import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { InventoryRepository } from '../../../domain/ports/repository/inventory.repository';
import { PickingCommand } from '../../../domain/ports/commands/picking.command';

@Injectable()
export class ShipStockUseCase {
  constructor(private repo: InventoryRepository) {}
  execute(cmd: PickingCommand): Observable<string> { return this.repo.ship(cmd); }
}
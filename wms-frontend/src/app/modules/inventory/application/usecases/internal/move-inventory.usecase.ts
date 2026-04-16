import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { InventoryRepository } from '../../../domain/ports/repository/inventory.repository';
import { MoveCommand } from '../../../domain/ports/commands/move.command';
import { InventoryItemModel } from '../../../domain/models/inventory-item.model';

@Injectable()
export class MoveInventoryUseCase {
  constructor(private repo: InventoryRepository) {}

  executeMove(cmd: MoveCommand): Observable<void> { return this.repo.move(cmd); }
  executePutAway(cmd: MoveCommand): Observable<void> { return this.repo.putAway(cmd); }
  
  // Helper para escanear antes de mover
  getLpnInfo(lpn: string): Observable<InventoryItemModel> { return this.repo.getByLpn(lpn); }
}
import { Observable } from 'rxjs';
import { ReceiveCommand } from '../commands/receive.command';
import { MoveCommand } from '../commands/move.command';
import { InventoryItemModel } from '../../models/inventory-item.model';
import { PickingCommand } from '../commands/picking.command';

export abstract class InventoryRepository {
  // Queries
  abstract getAll(): Observable<InventoryItemModel[]>;
  abstract getByLpn(lpn: string): Observable<InventoryItemModel>; 
  abstract suggestLocation(sku: string, quantity: number): Observable<string>;

  // Inbound
  abstract receive(command: ReceiveCommand): Observable<InventoryItemModel>;
  abstract putAway(command: MoveCommand): Observable<void>;

  // Internal
  abstract move(command: MoveCommand): Observable<void>;
  
  // Outbound
  abstract allocate(command: PickingCommand): Observable<string>;
  abstract ship(command: PickingCommand): Observable<string>;
}
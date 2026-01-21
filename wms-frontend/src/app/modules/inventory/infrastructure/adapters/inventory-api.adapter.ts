import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { InventoryRepository } from '../../domain/ports/repository/inventory.repository';
import { ReceiveCommand } from '../../domain/ports/commands/receive.command';
import { MoveCommand } from '../../domain/ports/commands/move.command';
import { PickingCommand } from '../../domain/ports/commands/picking.command';
import { InventoryItemResponseDto } from '../dtos/inventory-response.dto';
import { InventoryMapper } from '../mappers/inventory.mapper';
import { environment } from '../../../../../environments/environment';
import { InventoryItemModel } from '../../domain/models/inventory-item.model';

@Injectable()
export class InventoryApiAdapter extends InventoryRepository {

  private readonly INV_URL = `${environment.apiUrl}/inventory`; 
  private readonly PICK_URL = `${environment.apiUrl}/picking`;   

  constructor(private http: HttpClient) {
    super();
  }

  getAll(): Observable<InventoryItemModel[]> {
    return this.http.get<InventoryItemResponseDto[]>(this.INV_URL).pipe(
      map(dtos => dtos.map(dto => InventoryMapper.toDomain(dto)))
    );
  }

  getByLpn(lpn: string): Observable<InventoryItemModel> {
    // NOTA: Como el backend no tiene un endpoint GET /inventory/{lpn} específico,
    // reutilizamos getAll() y filtramos en el cliente.
    // Esto debería optimizarse en el futuro agregando el endpoint en Java.
    return this.getAll().pipe(
      map(items => {
        const found = items.find(i => i.lpn === lpn);
        if (!found) {
          throw new Error(`El LPN ${lpn} no existe o no se encuentra disponible.`);
        }
        return found;
      })
    );
  }

  suggestLocation(sku: string, quantity: number): Observable<string> {
    const params = new HttpParams()
      .set('sku', sku)
      .set('quantity', quantity.toString());

    return this.http.get(`${this.INV_URL}/suggest-location`, { 
      params, 
      responseType: 'text' 
    });
  }

  receive(command: ReceiveCommand): Observable<InventoryItemModel> {
    const dto = InventoryMapper.toReceiveDto(command);

    return this.http.post<InventoryItemResponseDto>(`${this.INV_URL}/receive`, dto).pipe(
      map(responseDto => InventoryMapper.toDomain(responseDto))
    );
  }

  putAway(command: MoveCommand): Observable<void> {
    const body = {
      lpn: command.lpn,
      targetLocationCode: command.targetLocationCode
    };
    return this.http.put<void>(`${this.INV_URL}/put-away`, body);
  }

  move(command: MoveCommand): Observable<void> {
    const body = {
      lpn: command.lpn,
      targetLocationCode: command.targetLocationCode,
      reason: command.reason || 'Movimiento Interno'
    };
    return this.http.post<void>(`${this.INV_URL}/move`, body);
  }

  allocate(command: PickingCommand): Observable<string> {
    const body = {
      sku: command.sku,
      quantity: command.quantity
    };
    return this.http.post(`${this.PICK_URL}/allocate`, body, { responseType: 'text' });
  }

  ship(command: PickingCommand): Observable<string> {
    const body = {
      sku: command.sku,
      quantity: command.quantity
    };
    return this.http.post(`${this.PICK_URL}/ship`, body, { responseType: 'text' });
  }
}
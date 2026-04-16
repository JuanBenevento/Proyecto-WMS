
import { InventoryItemModel, InventoryStatus } from '../../domain/models/inventory-item.model';
import { ReceiveCommand } from '../../domain/ports/commands/receive.command';
import { InventoryItemResponseDto } from '../dtos/inventory-response.dto';
import { ReceiveRequestDto } from '../dtos/receive-request.dto';


export class InventoryMapper {
  
  // De la API (Infra) al Dominio (App)
  static toDomain(dto: InventoryItemResponseDto): InventoryItemModel {
    return {
      lpn: dto.lpn,
      sku: dto.sku,
      productName: dto.productName,
      quantity: dto.quantity,
      // Mapeo seguro de String a Tipo Union/Enum
      status: dto.status as InventoryStatus, 
      batchNumber: dto.batchNumber,
      expiryDate: dto.expiryDate,
      locationCode: dto.locationCode
    };
  }

  // Del Dominio (Comando) a la API (DTO)
  static toReceiveDto(command: ReceiveCommand): ReceiveRequestDto {
    return {
      productSku: command.productSku,
      quantity: command.quantity,
      locationCode: command.locationCode,
      batchNumber: command.batchNumber,
      expiryDate: command.expiryDate
    };
  }
}
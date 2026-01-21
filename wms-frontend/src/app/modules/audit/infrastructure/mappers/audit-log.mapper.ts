import { AuditLogModel } from '../../domain/models/audit-log.model';

export class AuditLogMapper {
  static toDomain(dto: any): AuditLogModel {
    return {
      id: dto.id,
      timestamp: dto.timestamp,
      type: dto.type,
      sku: dto.sku,
      lpn: dto.lpn,
      quantity: dto.quantity,
      oldQuantity: dto.oldQuantity,
      newQuantity: dto.newQuantity,
      user: dto.user,
      reason: dto.reason
    };
  }
}
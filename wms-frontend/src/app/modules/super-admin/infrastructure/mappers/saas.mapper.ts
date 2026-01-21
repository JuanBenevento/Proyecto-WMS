import { TenantResponseDto } from '../dtos/tenant-response.dto';
import { Tenant } from '../../domain/models/tenant.model';

export class SaasMapper {
    static toDomain(dto: TenantResponseDto): Tenant {
        return {
            id: dto.id,     
            name: dto.name, 
            isActive: dto.status === 'ACTIVE'
        };
    }
}
import { LocationDto } from '../dtos/location.dto';
import { LocationModel, ZoneType } from '../../domain/models/location.model';

export class LocationMapper {
  static toDomain(dto: LocationDto): LocationModel {
    return {
      locationCode: dto.locationCode,
      aisle: dto.aisle || dto.locationCode.split('-')[0] || 'GEN',
      column: dto.column || dto.locationCode.split('-')[1] || '00',
      level: dto.level || dto.locationCode.split('-')[2] || '00',
      zoneType: dto.zoneType as ZoneType,
      maxWeight: dto.maxWeight,
      currentWeight: dto.currentWeight,
      availableWeight: dto.availableWeight,
      maxVolume: dto.maxVolume,
      currentVolume: dto.currentVolume,
      occupancyPercentage: dto.occupancyPercentage
    };
  }
}
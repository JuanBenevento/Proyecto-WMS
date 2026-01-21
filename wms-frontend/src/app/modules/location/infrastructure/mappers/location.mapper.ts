import { LocationDto } from '../dtos/location.dto';
import { LocationModel, ZoneType } from '../../domain/models/location.model';

export class LocationMapper {
  static toDomain(dto: LocationDto): LocationModel {
    return {
      locationCode: dto.locationCode,
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
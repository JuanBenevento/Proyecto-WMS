import { LayoutDto } from '../dtos/layout.dto';
import { RackSummaryDto } from '../dtos/rack-summary.dto';
import { RackSummary } from '../../domain/models/rack-summary.model';

export class LayoutMapper {
  static toDomain(dto: LayoutDto): { layoutJson: string; lastUpdate: Date } {
    return {
      layoutJson: dto.layoutJson,
      lastUpdate: new Date(dto.lastUpdate)
    };
  }

  static toRackSummaryDomain(dto: RackSummaryDto): RackSummary {
    return {
      rackCode: dto.rackCode,
      totalPositions: dto.totalPositions,
      occupancyPercentage: Number(dto.occupancyPercentage),
      currentWeight: Number(dto.currentWeight),
      maxWeight: Number(dto.maxWeight),
      status: dto.status
    };
  }
}
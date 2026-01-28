export enum ZoneType {
  DRY_STORAGE = 'DRY_STORAGE',
  COLD_STORAGE = 'COLD_STORAGE',
  FROZEN_STORAGE = 'FROZEN_STORAGE',
  HAZMAT = 'HAZMAT',
  RECEIVING_AREA = 'RECEIVING_AREA',
  DISPATCH_AREA = 'DISPATCH_AREA',
  DOCK_DOOR = 'DOCK_DOOR',
  PICKING_AREA = 'PICKING_AREA',
  YARD = 'YARD'
}

export interface LocationModel {
  locationCode: string;
  aisle?: string;
  column?: string;
  level?: string;
  zoneType: ZoneType;
  maxWeight: number;
  currentWeight: number;
  availableWeight: number;
  maxVolume: number;
  currentVolume: number;
  occupancyPercentage: number;
}
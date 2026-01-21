export type ZoneType = 'DRY_STORAGE' | 'COLD_STORAGE' | 'FROZEN' | 'HAZARDOUS' | 'RECEIVING' | 'DISPATCH'; 

export interface LocationModel {
  locationCode: string; // ID
  zoneType: ZoneType;
  maxWeight: number;
  currentWeight: number;
  availableWeight: number;
  maxVolume: number;
  currentVolume: number;
  occupancyPercentage: number; 
}
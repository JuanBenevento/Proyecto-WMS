export interface LocationDto {
  locationCode: string;
  zoneType: string;
  maxWeight: number;
  currentWeight: number;
  availableWeight: number;
  maxVolume: number;
  currentVolume: number;
  occupancyPercentage: number;
  items: any[]; 
}
export interface ItemShort {
  sku: string;
  lpn: string;
  quantity: number;
}

export interface Location {
  locationCode: string;
  zoneType: string;
  maxWeight: number;
  maxVolume: number;
  currentWeight: number;
  currentVolume: number;
  items?: ItemShort[]; 
}
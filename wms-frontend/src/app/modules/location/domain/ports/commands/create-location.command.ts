import { ZoneType } from "../../models/location.model";

export interface CreateLocationCommand {
  locationCode: string;
  zoneType: ZoneType;
  maxWeight: number;
  maxVolume: number;
}
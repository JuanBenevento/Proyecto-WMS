export interface RackSummary {
  rackCode: string;
  totalPositions: number;
  occupancyPercentage: number;
  currentWeight: number;
  maxWeight: number;
  status: 'EMPTY' | 'PARTIAL' | 'FULL' | 'OVERLOADED' | 'UNBOUND';
}
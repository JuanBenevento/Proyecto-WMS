export type VisualType = 'RACK' | 'ZONE' | 'WALL' | 'DOOR';

export interface VisualData {
  type: VisualType;
  code: string;               
  status: 'BOUND' | 'UNBOUND'; 
}
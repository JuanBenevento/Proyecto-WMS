export interface ProductDto {
  id: string;
  sku: string;
  name: string;
  description?: string;
  width: number;
  height: number;
  depth: number;
  weight: number;
  active: boolean;
}
export interface CreateProductCommand {
  sku: string;
  name: string;
  description?: string;
  width: number;
  height: number;
  depth: number;
  weight: number;
}
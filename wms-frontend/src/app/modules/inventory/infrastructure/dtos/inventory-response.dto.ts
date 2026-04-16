export interface InventoryItemResponseDto {
  lpn: string;
  sku: string;
  productName: string;
  quantity: number;
  status: string; 
  batchNumber: string;
  expiryDate: string; 
  locationCode: string;
}
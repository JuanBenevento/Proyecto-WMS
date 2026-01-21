export type InventoryStatus = 'IN_QUALITY_CHECK' | 'AVAILABLE' | 'RESERVED' | 'SHIPPED' | 'DAMAGED';

export interface InventoryItemModel {
  lpn: string;
  sku: string;
  productName: string;
  quantity: number;
  locationCode: string;
  status: InventoryStatus;
  batchNumber: string;
  expiryDate: string;
}
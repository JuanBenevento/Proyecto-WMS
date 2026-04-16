import { ProductDto } from '../dtos/product.dto';
import { ProductModel } from '../../domain/models/product.model';

export class ProductMapper {
  static toDomain(dto: ProductDto): ProductModel {
    return {
      id: dto.id,
      sku: dto.sku,
      name: dto.name,
      description: dto.description,
      width: dto.width,
      height: dto.height,
      depth: dto.depth,
      weight: dto.weight,
      active: dto.active
    };
  }
}
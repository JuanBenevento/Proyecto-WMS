package com.juanbenevento.wms.catalog.application.mapper;

import com.juanbenevento.wms.catalog.application.port.in.dto.ProductResponse;
import com.juanbenevento.wms.shared.domain.valueobject.Dimensions;
import com.juanbenevento.wms.catalog.domain.model.Product;
import com.juanbenevento.wms.catalog.infrastructure.out.persistence.ProductEntity;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper {

    public ProductResponse toProductResponse(Product product) {
        if (product == null) return null;
        return new ProductResponse(
                product.getId().toString(),
                product.getSku(),
                product.getName(),
                product.getDescription(),
                product.getDimensions().width(),
                product.getDimensions().height(),
                product.getDimensions().depth(),
                product.getDimensions().weight()
        );
    }

    public ProductEntity toProductEntity(Product domain) {
        if (domain == null) return null;
        return ProductEntity.builder()
                .id(domain.getId())
                .sku(domain.getSku())
                .name(domain.getName())
                .description(domain.getDescription())
                .width(domain.getDimensions().width())
                .height(domain.getDimensions().height())
                .depth(domain.getDimensions().depth())
                .weight(domain.getDimensions().weight())
                .version(domain.getVersion())
                .active(true)
                .build();
    }

    public Product toProductDomain(ProductEntity entity) {
        if (entity == null) return null;
        Dimensions dims = new Dimensions(
                entity.getWidth(), entity.getHeight(), entity.getDepth(), entity.getWeight()
        );
        return new Product(
                entity.getId(),
                entity.getSku(),
                entity.getName(),
                entity.getDescription(),
                dims,
                entity.getVersion()
        );
    }
}
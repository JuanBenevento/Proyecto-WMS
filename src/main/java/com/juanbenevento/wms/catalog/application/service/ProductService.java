package com.juanbenevento.wms.catalog.application.service;

import com.juanbenevento.wms.catalog.application.mapper.ProductMapper;
import com.juanbenevento.wms.catalog.application.port.in.command.CreateProductCommand;
import com.juanbenevento.wms.catalog.application.port.in.dto.ProductResponse;
import com.juanbenevento.wms.catalog.application.port.in.usecases.ManageProductUseCase;
import com.juanbenevento.wms.catalog.application.port.out.ProductRepositoryPort;
import com.juanbenevento.wms.catalog.domain.exception.ProductAlreadyExistsException;
import com.juanbenevento.wms.catalog.domain.exception.ProductInUseException;
import com.juanbenevento.wms.catalog.domain.exception.ProductNotFoundException;
import com.juanbenevento.wms.shared.domain.valueobject.Dimensions;
import com.juanbenevento.wms.catalog.domain.model.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService implements ManageProductUseCase {

    private final ProductRepositoryPort productRepository;
    private final ProductMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> getAllProducts() {
        return productRepository.findAll().stream()
                .map(mapper::toProductResponse)
                .toList();
    }

    @Override
    @Transactional
    public ProductResponse createProduct(CreateProductCommand command) {
        if (productRepository.findBySku(command.sku()).isPresent()) {
            throw new ProductAlreadyExistsException(command.sku());
        }

        Dimensions dims = new Dimensions(
                command.width(), command.height(), command.depth(), command.weight()
        );

        Product newProduct = Product.create(
                command.sku(),
                command.name(),
                command.description(),
                dims
        );

        return mapper.toProductResponse(productRepository.save(newProduct));
    }

    @Override
    @Transactional
    public ProductResponse updateProduct(String sku, CreateProductCommand command) {
        Product product = productRepository.findBySku(sku)
                .orElseThrow(() -> new ProductNotFoundException(sku));

        Dimensions newDims = new Dimensions(
                command.width(), command.height(), command.depth(), command.weight()
        );

        boolean dimensionsChanged = !product.getDimensions().equals(newDims);

        if (dimensionsChanged && productRepository.existsInInventory(sku)) {
            throw new ProductInUseException(sku,
                    "Existe stock físico en almacén. Modificar dimensiones rompería los cálculos de capacidad de las ubicaciones.");
        }

        product.updateDetails(command.name(), command.description());

        if (dimensionsChanged) {
            product.changeDimensions(newDims);
        }

        return mapper.toProductResponse(productRepository.save(product));
    }

    @Override
    @Transactional
    public void deleteProduct(String sku) {
        if (productRepository.findBySku(sku).isEmpty()) {
            throw new ProductNotFoundException(sku);
        }

        // Validar integridad referencial de negocio
        if (productRepository.existsInInventory(sku)) {
            throw new ProductInUseException(sku, "Tiene stock físico asociado.");
        }

        productRepository.delete(sku);
    }
}
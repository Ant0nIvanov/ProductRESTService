package ru.ivanov.productservice.service;

import ru.ivanov.productservice.model.dto.ProductDto;
import ru.ivanov.productservice.model.dto.request.CreateProductRequest;
import ru.ivanov.productservice.model.dto.request.UpdateProductRequest;

import java.util.List;
import java.util.UUID;

public interface ProductService {

    ProductDto createProduct(CreateProductRequest request);

    List<ProductDto> getAllProducts();

    ProductDto getProductById(UUID productId);

    void updateProduct(UUID productId, UpdateProductRequest request);

    void deleteProduct(UUID productId);
}

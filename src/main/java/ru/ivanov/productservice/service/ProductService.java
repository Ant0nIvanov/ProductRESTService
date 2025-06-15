package ru.ivanov.productservice.service;

import org.springframework.data.domain.Pageable;
import ru.ivanov.productservice.model.dto.ProductDto;
import ru.ivanov.productservice.model.dto.request.CreateProductRequest;
import ru.ivanov.productservice.model.dto.request.UpdateProductRequest;
import ru.ivanov.productservice.model.dto.response.PagedResponse;

import java.util.UUID;

public interface ProductService {

    ProductDto createProduct(CreateProductRequest request);

    PagedResponse<ProductDto> getAllProductsPaginated(Pageable pageable);

    ProductDto getProductById(UUID productId);

    void updateProduct(UUID productId, UpdateProductRequest request);

    void deleteProduct(UUID productId);
}
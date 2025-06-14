package ru.ivanov.productservice.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.ivanov.productservice.exception.ResourceNotFoundException;
import ru.ivanov.productservice.mapper.ProductMapper;
import ru.ivanov.productservice.model.dto.ProductDto;
import ru.ivanov.productservice.model.dto.request.CreateProductRequest;
import ru.ivanov.productservice.model.dto.request.UpdateProductRequest;
import ru.ivanov.productservice.model.entity.Product;
import ru.ivanov.productservice.repository.ProductRepository;
import ru.ivanov.productservice.service.ProductService;

import java.util.List;
import java.util.UUID;

import static ru.ivanov.productservice.util.MessageUtils.PRODUCT_NOT_FOUND_WITH_ID;

@Service
public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    public ProductServiceImpl(ProductRepository productRepository, ProductMapper productMapper) {
        this.productRepository = productRepository;
        this.productMapper = productMapper;
    }

    @Override
    @Transactional
    public ProductDto createProduct(CreateProductRequest request) {
        Product product = new Product(
                request.title(),
                request.details()
        );

        Product savedProduct = productRepository.save(product);
        return productMapper.toDto(savedProduct);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductDto> getAllProducts() {
        return productRepository.findAll().stream()
                .map(productMapper::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ProductDto getProductById(UUID productId) {
        Product product = findById(productId);
        return productMapper.toDto(product);
    }

    @Override
    @Transactional
    public void updateProduct(UUID productId, UpdateProductRequest request) {
        Product product = findById(productId);
        product.setTitle(request.title());
        product.setDetails(request.details());
        productRepository.save(product);
    }

    @Override
    @Transactional
    public void deleteProduct(UUID productId) {
        if (!productRepository.existsById(productId)) {
            throw new ResourceNotFoundException(PRODUCT_NOT_FOUND_WITH_ID.formatted(productId));
        }
        productRepository.deleteById(productId);
    }

    private Product findById(UUID productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException(PRODUCT_NOT_FOUND_WITH_ID.formatted(productId)));
    }
}

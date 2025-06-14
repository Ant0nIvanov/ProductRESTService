package ru.ivanov.productservice.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.util.CollectionUtils;
import ru.ivanov.productservice.exception.ResourceNotFoundException;
import ru.ivanov.productservice.mapper.ProductMapper;
import ru.ivanov.productservice.model.dto.ProductDto;
import ru.ivanov.productservice.model.dto.request.CreateProductRequest;
import ru.ivanov.productservice.model.dto.request.UpdateProductRequest;
import ru.ivanov.productservice.model.entity.Product;
import ru.ivanov.productservice.repository.ProductRepository;
import ru.ivanov.productservice.service.impl.ProductServiceImpl;
import ru.ivanov.productservice.util.DataUtils;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.*;
import static ru.ivanov.productservice.util.MessageUtils.PRODUCT_NOT_FOUND_WITH_ID;

@ExtendWith(MockitoExtension.class)
public class ProductServiceImplTests {
    @Mock
    private ProductRepository productRepository;
    @Mock
    private ProductMapper productMapper;
    @InjectMocks
    private ProductServiceImpl serviceUnderTest;

    @Test
    @DisplayName("Test save product functionality")
    public void givenCreateProductRequest_whenCreateProduct_thenRepositoryIsCalled() {
        //given
        CreateProductRequest request = DataUtils.getCreateProductMilkRequest();
        Product savedProduct = DataUtils.getProductMilkPersisted();
        ProductDto expectedDto = DataUtils.getProductMilkDto();

        when(productRepository.save(any(Product.class))).thenReturn(savedProduct);
        when(productMapper.toDto(any(Product.class))).thenReturn(expectedDto);

        //when
        ProductDto obtainedDto = serviceUnderTest.createProduct(request);

        //then
        assertThat(obtainedDto).isNotNull();
        assertThat(obtainedDto).isEqualTo(expectedDto);

        verify(productRepository, times(1)).save(any(Product.class));
        verify(productMapper, times(1)).toDto(any(Product.class));
    }


    @Test
    @DisplayName("Test get product by id functionality with existent product id")
    public void givenExistentProductId_whenGetProductById_thenProductDtoIsReturned() {
        //given
        UUID existentProductId = DataUtils.PRODUCT_MILK_ID;
        Product savedProduct = DataUtils.getProductMilkPersisted();
        ProductDto expectedDto = DataUtils.getProductMilkDto();
        when(productRepository.findById(any(UUID.class))).thenReturn(Optional.of(savedProduct));
        when(productMapper.toDto(any(Product.class))).thenReturn(expectedDto);

        //when
        ProductDto savedProductDto = serviceUnderTest.getProductById(existentProductId);

        //then
        assertThat(savedProductDto).isNotNull();
        assertThat(savedProductDto).isEqualTo(expectedDto);
    }

    @Test
    @DisplayName("Test get product by id functionality with not existent product id")
    public void givenNotExistentProductId_whenGetProductById_thenResourceNotFoundExceptionIsThrown() {
        //given
        UUID notExistentProductId = UUID.randomUUID();

        //when and then
        assertThatThrownBy(() -> serviceUnderTest.getProductById(notExistentProductId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage(PRODUCT_NOT_FOUND_WITH_ID.formatted(notExistentProductId));

        verify(productMapper, never()).toDto(any(Product.class));
    }

    @Test
    @DisplayName("Test get all products functionality when no products are existed")
    public void givenNoProductsExisted_whenGetAllProducts_thenEmptyListIsReturned() {
        //given
        when(productRepository.findAll()).thenReturn(Collections.emptyList());

        //when
        List<ProductDto> productDtos = serviceUnderTest.getAllProducts();

        //then
        assertThat(CollectionUtils.isEmpty(productDtos)).isTrue();
        verify(productRepository, times(1)).findAll();
        verifyNoInteractions(productMapper);

    }

    @Test
    @DisplayName("Test get all products functionality when only one product is existed")
    public void givenOneProductExisted_whenGetAllProducts_thenReturnListWithOneProductDto() {
        //given
        Product savedProduct = DataUtils.getProductButterPersisted();
        ProductDto productDto = DataUtils.getProductButterDto();
        when(productRepository.findAll()).thenReturn(List.of(savedProduct));
        when(productMapper.toDto(any(Product.class))).thenReturn(productDto);

        //when
        List<ProductDto> obtainedProductDtos = serviceUnderTest.getAllProducts();

        //then
        assertThat(CollectionUtils.isEmpty(obtainedProductDtos)).isFalse();
        assertThat(obtainedProductDtos.size()).isEqualTo(1);
        assertThat(productDto).isIn(obtainedProductDtos);

        verify(productRepository, times(1)).findAll();
        verify(productMapper, times(1)).toDto(any(Product.class));
    }

    @Test
    @DisplayName("Test get all products functionality when many products are existed")
    public void givenThreeProductsExisted_whenGetAllProducts_thenThreeProductDtosAreReturned() {
        //given
        Product savedProductMilk = DataUtils.getProductMilkPersisted();
        Product savedProductButter = DataUtils.getProductButterPersisted();
        Product savedProductCottage = DataUtils.getProductCottagePersisted();
        List<Product> savedProducts = List.of(savedProductMilk, savedProductButter, savedProductCottage);
        ProductDto milkDto = DataUtils.getProductMilkDto();
        ProductDto butterDto = DataUtils.getProductButterDto();
        ProductDto cottageDto = DataUtils.getProductCottageDto();
        List<ProductDto> expectedDtos = List.of(milkDto, butterDto, cottageDto);

        when(productRepository.findAll()).thenReturn(savedProducts);
        when(productMapper.toDto(savedProductMilk)).thenReturn(milkDto);
        when(productMapper.toDto(savedProductButter)).thenReturn(butterDto);
        when(productMapper.toDto(savedProductCottage)).thenReturn(cottageDto);

        //when
        List<ProductDto> obtainedProductDtos = serviceUnderTest.getAllProducts();

        //then
        assertThat(CollectionUtils.isEmpty(obtainedProductDtos)).isFalse();
        assertThat(obtainedProductDtos.size()).isEqualTo(3);
        assertThat(obtainedProductDtos).isEqualTo(expectedDtos);

        verify(productRepository, times(1)).findAll();
        verify(productMapper, times(3)).toDto(any(Product.class));

    }


    @Test
    @DisplayName("Test update product functionality with existent product id")
    public void givenUpdateProductRequestAndExistentProductId_whenUpdateProduct_thenRepositoryIsCalled() {
        //given
        UUID productId = DataUtils.PRODUCT_MILK_ID;
        UpdateProductRequest request = DataUtils.getUpdateProductMilkRequest();
        Product productToUpdate = DataUtils.getProductMilkPersisted();
        when(productRepository.findById(productId)).thenReturn(Optional.of(productToUpdate));

        //when
        serviceUnderTest.updateProduct(productId, request);

        //then
        verify(productRepository, times(1)).findById(productId);
        verify(productRepository, times(1)).save(productToUpdate);

    }

    @Test
    @DisplayName("Test update product functionality with not existent id")
    public void givenUpdateProductRequestAndNotExistentProductId_whenUpdateProduct_thenResourceNotFoundExceptionIsThrown() {
        //given
        UUID notExistentProductId = UUID.randomUUID();
        UpdateProductRequest request = DataUtils.getUpdateProductMilkRequest();

        //when and then
        assertThatThrownBy(() -> serviceUnderTest.updateProduct(notExistentProductId, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage(PRODUCT_NOT_FOUND_WITH_ID.formatted(notExistentProductId));

        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    @DisplayName("Test delete product functionality with existent product id")
    public void givenExistentProductId_whenDeleteProduct_thenRepositoryDeleteByIdMethodIsCalled() {
        //given
        UUID existentId = DataUtils.PRODUCT_BUTTER_ID;
        when(productRepository.existsById(any(UUID.class))).thenReturn(true);

        //when
        serviceUnderTest.deleteProduct(existentId);

        //then
        verify(productRepository, times(1)).existsById(any(UUID.class));
        verify(productRepository, times(1)).deleteById(any(UUID.class));
    }

    @Test
    @DisplayName("Test delete product functionality with not existent product id")
    public void givenNotExistentProductId_whenDeleteProduct_thenResourceNotFoundExceptionIsThrown() {
        //given
        UUID notExistentId = UUID.randomUUID();

        //when ant then
        assertThatThrownBy(() -> serviceUnderTest.deleteProduct(notExistentId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage(PRODUCT_NOT_FOUND_WITH_ID.formatted(notExistentId));

        verify(productRepository, never()).deleteById(any(UUID.class));

    }
}
package ru.ivanov.productservice.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import ru.ivanov.productservice.exception.ResourceNotFoundException;
import ru.ivanov.productservice.mapper.ProductMapper;
import ru.ivanov.productservice.model.dto.ProductDto;
import ru.ivanov.productservice.model.dto.request.CreateProductRequest;
import ru.ivanov.productservice.model.dto.request.UpdateProductRequest;
import ru.ivanov.productservice.model.dto.response.PagedResponse;
import ru.ivanov.productservice.model.entity.Product;
import ru.ivanov.productservice.repository.ProductRepository;
import ru.ivanov.productservice.service.impl.ProductServiceImpl;
import ru.ivanov.productservice.util.TestUtils;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static java.util.Collections.emptyList;
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
    @DisplayName("Should save and return product DTO when valid create request is provided")
    public void givenCreateProductRequest_whenCreateProduct_thenSaveAndReturnProductDto() {
        //given
        CreateProductRequest request = TestUtils.getCreateProductMilkRequest();
        Product persistedProduct = TestUtils.getProductMilkPersisted();
        ProductDto expectedDto = TestUtils.getProductMilkPersistedDto();

        when(productRepository.save(any(Product.class))).thenReturn(persistedProduct);
        when(productMapper.toDto(persistedProduct)).thenReturn(expectedDto);

        //when
        ProductDto obtainedDto = serviceUnderTest.createProduct(request);

        //then
        assertThat(obtainedDto).isNotNull();
        assertThat(obtainedDto).usingRecursiveComparison().isEqualTo(expectedDto);

        verify(productRepository, times(1)).save(any(Product.class));
        verify(productMapper, times(1)).toDto(persistedProduct);
        verifyNoMoreInteractions(productRepository, productMapper);
    }


    @Test
    @DisplayName("Should return product DTO when product with given id exists")
    public void givenExistentProductId_whenGetProductById_thenReturnProductDto() {
        //given
        UUID existentProductId = TestUtils.PRODUCT_MILK_ID;
        Product persistedProduct = TestUtils.getProductMilkPersisted();
        ProductDto expectedDto = TestUtils.getProductMilkPersistedDto();

        when(productRepository.findById(existentProductId)).thenReturn(Optional.of(persistedProduct));
        when(productMapper.toDto(persistedProduct)).thenReturn(expectedDto);

        //when
        ProductDto obtainedProductDto = serviceUnderTest.getProductById(existentProductId);

        //then
        assertThat(obtainedProductDto).isNotNull();
        assertThat(obtainedProductDto).usingRecursiveComparison().isEqualTo(expectedDto);

        verify(productRepository, times(1)).findById(existentProductId);
        verify(productMapper, times(1)).toDto(persistedProduct);
        verifyNoMoreInteractions(productRepository, productMapper);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when product with given id is not exist")
    public void givenNotExistentProductId_whenGetProductById_thenThrowResourceNotFoundException() {
        //given
        UUID notExistentProductId = UUID.randomUUID();

        when(productRepository.findById(notExistentProductId)).thenReturn(Optional.empty());

        //when and then
        assertThatThrownBy(() -> serviceUnderTest.getProductById(notExistentProductId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage(PRODUCT_NOT_FOUND_WITH_ID.formatted(notExistentProductId));

        verify(productRepository, times(1)).findById(notExistentProductId);
        verifyNoInteractions(productMapper);
        verifyNoMoreInteractions(productRepository);
    }

    @Test
    @DisplayName("Should return empty paged response when no products exists")
    public void givenNoProductsExist_whenGetAllProductsPaginated_thenReturnEmptyPagedResponse() {
        //given
        int pageNumber = 0;
        int pageSize = 10;
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        Page<Product> emptyPage = Page.empty(pageable);

        when(productRepository.findAll(any(Pageable.class))).thenReturn(emptyPage);

        PagedResponse<ProductDto> expectedPagedResponse = new PagedResponse<>(pageNumber, pageSize, 0L, 0, true, true, emptyList());

        //when
        PagedResponse<ProductDto> obtainedPagedResponse = serviceUnderTest.getAllProductsPaginated(pageNumber, pageSize);

        //then
        assertThat(obtainedPagedResponse.content().isEmpty()).isTrue();
        assertThat(obtainedPagedResponse).usingRecursiveComparison().isEqualTo(expectedPagedResponse);

        verify(productRepository, times(1)).findAll(pageable);
        verifyNoInteractions(productMapper);
        verifyNoMoreInteractions(productRepository);
    }

    @Test
    @DisplayName("Should return paged response with single productDto when one product exists")
    public void givenOneProductExisted_whenGetAllProductsPaginated_thenReturnPagedResponseWithSingleProductDto() {
        //given
        int pageNumber = 0;
        int pageSize = 10;
        Pageable pageable = PageRequest.of(pageNumber, pageSize);

        Product product = TestUtils.getProductButterPersisted();
        ProductDto productDto = TestUtils.getProductButterPersistedDto();

        Page<Product> mockPage = new PageImpl<>(List.of(product), pageable, 1L);

        when(productRepository.findAll(any(Pageable.class))).thenReturn(mockPage);
        when(productMapper.toDto(any(Product.class))).thenReturn(productDto);

        PagedResponse<ProductDto> expectedPagedResponse = new PagedResponse<>(pageNumber, pageSize, 1L, 1, true, true, List.of(productDto));

        //when
        PagedResponse<ProductDto> obtainedPagedResponse = serviceUnderTest.getAllProductsPaginated(pageNumber, pageSize);

        //then
        assertThat(obtainedPagedResponse.content().isEmpty()).isFalse();
        assertThat(obtainedPagedResponse.content().size()).isEqualTo(1);
        assertThat(obtainedPagedResponse).usingRecursiveComparison().isEqualTo(expectedPagedResponse);

        verify(productRepository, times(1)).findAll(any(Pageable.class));
        verify(productMapper, times(1)).toDto(any(Product.class));
        verifyNoMoreInteractions(productRepository, productMapper);
    }

    @Test
    @DisplayName("Should return paged response with 3 product DTO when 3 product exist")
    public void givenThreeProductsExisted_whenGetAllProductsPaginated_thenReturnPagedResponseWithThreeProductDtos() {
        //given
        int pageNumber = 0;
        int pageSize = 10;
        Pageable pageable = PageRequest.of(pageNumber, pageSize);

        Product productMilkPersisted = TestUtils.getProductMilkPersisted();
        Product productButterPersisted = TestUtils.getProductButterPersisted();
        Product productCottagePersisted = TestUtils.getProductCottagePersisted();
        List<Product> persistedProducts = List.of(productMilkPersisted, productButterPersisted, productCottagePersisted);

        ProductDto milkDto = TestUtils.getProductMilkPersistedDto();
        ProductDto butterDto = TestUtils.getProductButterPersistedDto();
        ProductDto cottageDto = TestUtils.getProductCottagePersistedDto();
        List<ProductDto> expectedDtos = List.of(milkDto, butterDto, cottageDto);

        Page<Product> mockPage = new PageImpl<>(
                persistedProducts,
                pageable,
                persistedProducts.size()
        );

        when(productRepository.findAll(pageable)).thenReturn(mockPage);

        when(productMapper.toDto(productMilkPersisted)).thenReturn(milkDto);
        when(productMapper.toDto(productButterPersisted)).thenReturn(butterDto);
        when(productMapper.toDto(productCottagePersisted)).thenReturn(cottageDto);

        PagedResponse<ProductDto> expectedPagedResponse = new PagedResponse<>(pageNumber, pageSize, 3L, 1, true, true, expectedDtos);

        //when
        PagedResponse<ProductDto> obtainedPagedResponse = serviceUnderTest.getAllProductsPaginated(pageNumber, pageSize);

        //then
        assertThat(obtainedPagedResponse.content().isEmpty()).isFalse();
        assertThat(obtainedPagedResponse.content().size()).isEqualTo(3);
        assertThat(obtainedPagedResponse).usingRecursiveComparison().isEqualTo(expectedPagedResponse);

        verify(productRepository, times(1)).findAll(pageable);
        verify(productMapper, times(3)).toDto(any(Product.class));
        verifyNoMoreInteractions(productRepository, productMapper);
    }

    @Test
    @DisplayName("Should update product when valid request and existing id are provided")
    public void givenUpdateProductRequestAndExistentProductId_whenUpdateProduct_thenUpdateProduct() {
        //given
        UUID productId = TestUtils.PRODUCT_MILK_ID;
        UpdateProductRequest request = TestUtils.getUpdateProductMilkRequest();
        Product existingProduct = TestUtils.getProductMilkPersisted();
        Product expectedUpdatedProduct = TestUtils.getUpdatedProductMilk();

        when(productRepository.findById(productId)).thenReturn(Optional.of(existingProduct));
        when(productRepository.save(any(Product.class))).thenReturn(expectedUpdatedProduct);
        //when
        serviceUnderTest.updateProduct(productId, request);

        //then
        verify(productRepository, times(1)).findById(productId);
        verify(productRepository, times(1)).save(existingProduct);
        verifyNoMoreInteractions(productRepository);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when updating non-existent product")
    public void givenUpdateProductRequestAndNotExistentProductId_whenUpdateProduct_thenThrowResourceNotFoundException() {
        //given
        UUID notExistentProductId = UUID.randomUUID();
        UpdateProductRequest request = TestUtils.getUpdateProductMilkRequest();

        when(productRepository.findById(notExistentProductId)).thenReturn(Optional.empty());

        //when and then
        assertThatThrownBy(() -> serviceUnderTest.updateProduct(notExistentProductId, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage(PRODUCT_NOT_FOUND_WITH_ID.formatted(notExistentProductId));

        verify(productRepository, times(1)).findById(notExistentProductId);
        verify(productRepository, never()).save(any());
        verifyNoMoreInteractions(productRepository);
    }

    @Test
    @DisplayName("Should delete product when existing product ID is provided")
    public void givenExistentProductId_whenDeleteProduct_thenDeleteProduct() {
        //given
        UUID existentId = TestUtils.PRODUCT_BUTTER_ID;
        when(productRepository.existsById(existentId)).thenReturn(true);
        doNothing().when(productRepository).deleteById(existentId);

        //when
        serviceUnderTest.deleteProduct(existentId);

        //then
        verify(productRepository, times(1)).existsById(existentId);
        verify(productRepository, times(1)).deleteById(existentId);
        verifyNoMoreInteractions(productRepository);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when deleting non-existent product")
    public void givenNotExistentProductId_whenDeleteProduct_thenThrowResourceNotFoundException() {
        //given
        UUID notExistentId = UUID.randomUUID();

        //when ant then
        assertThatThrownBy(() -> serviceUnderTest.deleteProduct(notExistentId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage(PRODUCT_NOT_FOUND_WITH_ID.formatted(notExistentId));

        verify(productRepository, times(1)).existsById(any(UUID.class));
        verify(productRepository, never()).deleteById(any());
        verifyNoMoreInteractions(productRepository);
    }
}
package ru.ivanov.productservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import ru.ivanov.productservice.exception.ResourceNotFoundException;
import ru.ivanov.productservice.model.dto.ProductDto;
import ru.ivanov.productservice.model.dto.request.CreateProductRequest;
import ru.ivanov.productservice.model.dto.request.UpdateProductRequest;
import ru.ivanov.productservice.service.ProductService;
import ru.ivanov.productservice.util.TestUtils;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static ru.ivanov.productservice.util.MessageUtils.PRODUCT_NOT_FOUND_WITH_ID;

@WebMvcTest(ProductRestController.class)
@AutoConfigureMockMvc
@ExtendWith(MockitoExtension.class)
public class ProductRestControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductService productService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Test create product functionality with valid createProductRequest")
    public void givenValidCreatProductRequest_whenCreateProduct_thenReturnStatusCreatedAndProductDtoAsBody() throws Exception {
        //given
        CreateProductRequest request = TestUtils.getCreateProductMilkRequest();
        ProductDto productDto = TestUtils.getProductMilkDto();
        when(productService.createProduct(any(CreateProductRequest.class))).thenReturn(productDto);

        //when
        ResultActions result = mockMvc.perform(post("http://localhost:8080/api/v1/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        );

        //then
        result.andExpectAll(
                status().isCreated(),
                content().contentType(MediaType.APPLICATION_JSON),
                header().exists("Location"),
                header().string("Location", "http://localhost:8080/api/v1/products/" + productDto.id().toString()),
                jsonPath("$.id", is("91efd04b-71b0-4da0-9c51-0eb94e0a63ca")),
                jsonPath("$.title", is("Milk")),
                jsonPath("$.details", is("Best milk in the world"))
        );

        verify(productService, times(1)).createProduct(any(CreateProductRequest.class));
    }

    @Test
    @DisplayName("Test create product functionality with invalid createProductRequest")
    public void givenInvalidCreateProductRequest_whenCreateProduct_thenReturnStatusBadRequestAndErrorResponseAsBody() throws Exception {
        //given
        CreateProductRequest request = new CreateProductRequest("Title", "");

        //when
        ResultActions result = mockMvc.perform(post("http://localhost:8080/api/v1/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        );

        //then
        result.andExpectAll(
                status().isBadRequest(),
                content().contentType(MediaType.APPLICATION_JSON),
                jsonPath("$.path").value("/api/v1/products"),
                jsonPath("$.message", CoreMatchers.startsWith("Validation failed:")),
                jsonPath("$.statusCode").value(BAD_REQUEST.value()),
                jsonPath("$.timestamp", notNullValue())
        );

        verifyNoInteractions(productService);
    }

    @Test
    @DisplayName("Test get all products functionality when no product existed")
    public void givenNoProductExists_whenGetAllProducts_thenReturnStatusOKAndEmptyListAsBody() throws Exception {
        //given
        when(productService.getAllProducts()).thenReturn(Collections.emptyList());

        //when
        ResultActions result = mockMvc.perform(get("/api/v1/products")
                .contentType(MediaType.APPLICATION_JSON)
        );

        //then
        result.andExpectAll(
                status().isOk(),
                content().contentType(MediaType.APPLICATION_JSON),
                jsonPath("$").isEmpty()
        );

        verify(productService, times(1)).getAllProducts();
    }

    @Test
    @DisplayName("Test get all product functionality when ony one product existed")
    public void givenOneProductExists_whenGetAllProducts_thenReturnStatusOkAndListWithOneProductDtoAsBody() throws Exception {
        //given
        ProductDto productDto = TestUtils.getProductCottageDto();
        when(productService.getAllProducts()).thenReturn(List.of(productDto));

        //when
        ResultActions result = mockMvc.perform(get("/api/v1/products")
                .contentType(MediaType.APPLICATION_JSON)
        );

        //then
        result.andExpectAll(
                status().isOk(),
                content().contentType(MediaType.APPLICATION_JSON),
                jsonPath("$", hasSize(1)),
                jsonPath("$[0].id").value(productDto.id().toString()),
                jsonPath("$[0].title").value(productDto.title()),
                jsonPath("$[0].details").value(productDto.details())
        );

        verify(productService, times(1)).getAllProducts();
    }

    @Test
    @DisplayName("Test get all products functionality when many product existed")
    public void givenManyProductExisted_whenGetAllProducts_thenReturnStatusOKAndListOfProductDtosAsBody() throws Exception {
        //given
        ProductDto milkDto = TestUtils.getProductMilkDto();
        ProductDto butterDto = TestUtils.getProductButterDto();
        ProductDto cottageDto = TestUtils.getProductCottageDto();
        when(productService.getAllProducts()).thenReturn(List.of(milkDto, butterDto, cottageDto));

        //when
        ResultActions result = mockMvc.perform(get("/api/v1/products")
                .contentType(MediaType.APPLICATION_JSON)
        );

        //then
        result.andExpectAll(
                status().isOk(),
                content().contentType(MediaType.APPLICATION_JSON),
                jsonPath("$", hasSize(3)),
                jsonPath("$[0].id").value(milkDto.id().toString()),
                jsonPath("$[0].title").value(milkDto.title()),
                jsonPath("$[0].details").value(milkDto.details()),

                jsonPath("$[1].id").value(butterDto.id().toString()),
                jsonPath("$[1].title").value(butterDto.title()),
                jsonPath("$[1].details").value(butterDto.details()),

                jsonPath("$[2].id").value(cottageDto.id().toString()),
                jsonPath("$[2].title").value(cottageDto.title()),
                jsonPath("$[2].details").value(cottageDto.details())
        );

        verify(productService, times(1)).getAllProducts();
    }

    @Test
    @DisplayName("Test get product by id functionality with existent product id")
    public void givenExistentProductId_whenGetProduct_thenReturnStatusOKAndProductDtoAsBody() throws Exception {
        //given
        UUID existentProductId = TestUtils.PRODUCT_COTTAGE_ID;
        ProductDto productDto = TestUtils.getProductCottageDto();
        when(productService.getProductById(any(UUID.class))).thenReturn(productDto);

        //when
        ResultActions result = mockMvc.perform(get("/api/v1/products/{productId}", existentProductId)
                .contentType(MediaType.APPLICATION_JSON)
        );

        //then
        result.andExpectAll(
                status().isOk(),
                content().contentType(MediaType.APPLICATION_JSON),
                jsonPath("$.id", is("b8477e47-d4f6-4d42-8cc2-7b734cdb1d1e")),
                jsonPath("$.title", is("Cottage")),
                jsonPath("$.details", is("Best cottage in the world"))
        );

        verify(productService, times(1)).getProductById(any(UUID.class));
    }

    @Test
    @DisplayName("Test get product by id functionality with not existent product id")
    public void givenNotExistentProductId_whenGetProduct_thenReturnStatusNotFoundAndErrorResponseAsBody() throws Exception {
        //given
        UUID notExistentProductId = UUID.randomUUID();
        when(productService.getProductById(any(UUID.class)))
                .thenThrow(new ResourceNotFoundException(PRODUCT_NOT_FOUND_WITH_ID.formatted(notExistentProductId)));

        //when
        ResultActions result = mockMvc.perform(get("/api/v1/products/{productId}", notExistentProductId)
                .contentType(MediaType.APPLICATION_JSON)
        );

        //then
        result.andExpectAll(
                status().isNotFound(),
                content().contentType(MediaType.APPLICATION_JSON),
                jsonPath("$.path").value("/api/v1/products/" + notExistentProductId),
                jsonPath("$.message").value(PRODUCT_NOT_FOUND_WITH_ID.formatted(notExistentProductId)),
                jsonPath("$.statusCode").value(NOT_FOUND.value()),
                jsonPath("$.timestamp", notNullValue())
        );

        verify(productService, times(1)).getProductById(any(UUID.class));
    }

    @Test
    @DisplayName("Test update product by id functionality with existent product id and valid updateProductRequest")
    public void givenExistentProductIdAndValidUpdateProductRequest_whenUpdateProduct_thenReturnNoContentStatus() throws Exception {
        //given
        UUID existentProductId = TestUtils.PRODUCT_MILK_ID;
        UpdateProductRequest request = TestUtils.getUpdateProductMilkRequest();
        doNothing().when(productService).updateProduct(any(UUID.class), any(UpdateProductRequest.class));

        //when
        ResultActions result = mockMvc.perform(put("/api/v1/products/{productId}", existentProductId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        );

        //then
        result.andExpectAll(
                status().isNoContent()
        );

        verify(productService, times(1)).updateProduct(any(UUID.class), any(UpdateProductRequest.class));
    }

    @Test
    @DisplayName("Test update product by id functionality with not existent product id and valid updateProductRequest")
    public void givenNotExistentProductIdAndValidUpdateProductRequest_whenUpdateProduct_thenReturnStatusNotFoundAndErrorResponseAsBody() throws Exception {
        //given
        UUID notExistentProductId = UUID.randomUUID();
        UpdateProductRequest request = TestUtils.getUpdateProductMilkRequest();
        doThrow(new ResourceNotFoundException(PRODUCT_NOT_FOUND_WITH_ID.formatted(notExistentProductId)))
                .when(productService).updateProduct(any(UUID.class), any(UpdateProductRequest.class));

        //when
        ResultActions result = mockMvc.perform(put("/api/v1/products/{productId}", notExistentProductId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        );

        //then
        result.andExpectAll(
                status().isNotFound(),
                content().contentType(MediaType.APPLICATION_JSON),
                jsonPath("$.path").value("/api/v1/products/" + notExistentProductId),
                jsonPath("$.message").value(PRODUCT_NOT_FOUND_WITH_ID.formatted(notExistentProductId)),
                jsonPath("$.statusCode").value(NOT_FOUND.value()),
                jsonPath("$.timestamp", notNullValue())
        );

        verify(productService, times(1)).updateProduct(any(UUID.class), any(UpdateProductRequest.class));
    }


    @Test
    @DisplayName("Test update product by id functionality with product id and invalid updateProductRequest")
    public void givenProductIdAndInvalidUpdateProductRequest_whenUpdateProduct_thenReturnStatusBadRequestAndErrorResponseAsBody() throws Exception {
        //given
        UUID productId = UUID.randomUUID();
        UpdateProductRequest request = new UpdateProductRequest("new Title", "");

        //when
        ResultActions result = mockMvc.perform(put("/api/v1/products/{productId}", productId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        );

        //then
        result.andExpectAll(
                status().isBadRequest(),
                content().contentType(MediaType.APPLICATION_JSON),
                jsonPath("$.path").value("/api/v1/products/" + productId),
                jsonPath("$.message", CoreMatchers.startsWith("Validation failed:")),
                jsonPath("$.statusCode").value(BAD_REQUEST.value()),
                jsonPath("$.timestamp", notNullValue())
        );

        verifyNoInteractions(productService);
    }

    @Test
    @DisplayName("Test delete product by id functionality with existent product id")
    public void givenExistentProductId_whenDeleteProduct_thenReturnNoContentStatus() throws Exception {
        //given
        UUID existentProductId = TestUtils.PRODUCT_BUTTER_ID;
        doNothing().when(productService).deleteProduct(any(UUID.class));

        //when
        ResultActions result = mockMvc.perform(delete("/api/v1/products/{productId}", existentProductId)
                .contentType(MediaType.APPLICATION_JSON)
        );

        //then
        result.andExpect(status().isNoContent());

        verify(productService, times(1)).deleteProduct(any(UUID.class));
    }

    @Test
    @DisplayName("Test delete product by id functionality with not existent product id")
    public void givenNotExistentProductId_whenDeleteProduct_thenReturnStatusNotFoundAndErrorResponseAsBody() throws Exception {
        //given
        UUID notExistentProductId = UUID.randomUUID();
        doThrow(new ResourceNotFoundException(PRODUCT_NOT_FOUND_WITH_ID.formatted(notExistentProductId)))
                .when(productService).deleteProduct(any(UUID.class));

        //when
        ResultActions result = mockMvc.perform(delete("/api/v1/products/{productId}", notExistentProductId)
                .contentType(MediaType.APPLICATION_JSON)
        );

        //then
        result.andExpectAll(
                status().isNotFound(),
                content().contentType(MediaType.APPLICATION_JSON),
                jsonPath("$.path").value("/api/v1/products/" + notExistentProductId),
                jsonPath("$.message").value(PRODUCT_NOT_FOUND_WITH_ID.formatted(notExistentProductId)),
                jsonPath("$.statusCode").value(NOT_FOUND.value()),
                jsonPath("$.timestamp", notNullValue())
        );

        verify(productService, times(1)).deleteProduct(any(UUID.class));
    }
}
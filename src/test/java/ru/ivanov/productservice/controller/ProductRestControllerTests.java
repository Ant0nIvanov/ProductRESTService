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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.jaxb.SpringDataJaxb;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import ru.ivanov.productservice.exception.ResourceNotFoundException;
import ru.ivanov.productservice.model.dto.ProductDto;
import ru.ivanov.productservice.model.dto.request.CreateProductRequest;
import ru.ivanov.productservice.model.dto.request.UpdateProductRequest;
import ru.ivanov.productservice.model.dto.response.PagedResponse;
import ru.ivanov.productservice.model.entity.Product;
import ru.ivanov.productservice.service.ProductService;
import ru.ivanov.productservice.util.TestUtils;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static java.lang.Boolean.*;
import static java.util.Collections.emptyList;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.MediaType.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static ru.ivanov.productservice.util.MessageUtils.PRODUCT_NOT_FOUND_WITH_ID;

@WebMvcTest(ProductRestController.class)
@AutoConfigureMockMvc
@ExtendWith(MockitoExtension.class)
public class ProductRestControllerTests {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductService productService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Should return status 201 CREATED and product DTO when creating product with valid request")
    public void givenValidCreatProductRequest_whenCreateProduct_thenReturnStatusCreatedAndCreatedProductDtoAsBody() throws Exception {
        //given
        CreateProductRequest request = TestUtils.getCreateProductMilkRequest();
        ProductDto expectedDto = TestUtils.getProductMilkPersistedDto();
        when(productService.createProduct(request)).thenReturn(expectedDto);

        //when
        ResultActions result = mockMvc.perform(post("/api/v1/products")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        );

        //then
        result.andExpectAll(
                status().isCreated(),
                content().contentType(APPLICATION_JSON),
                header().exists("Location"),
                header().string("Location", containsString("api/v1/products/" + expectedDto.id())),
                jsonPath("$.id", is(expectedDto.id().toString())),
                jsonPath("$.title", is(expectedDto.title())),
                jsonPath("$.details", is(expectedDto.details()))
        );

        verify(productService, times(1)).createProduct(request);
        verifyNoMoreInteractions(productService);
    }

    @Test
    @DisplayName("Should return status 400 BAD REQUEST and error response when creating product with invalid request")
    public void givenInvalidCreateProductRequest_whenCreateProduct_thenReturnStatusBadRequestAndErrorResponseAsBody() throws Exception {
        //given
        CreateProductRequest invalidRequest = new CreateProductRequest("Title", "");

        String expectedFieldErrorMessage = "details не должно быть пустым";

        //when
        ResultActions result = mockMvc.perform(post("/api/v1/products")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest))
                .accept(APPLICATION_JSON)
        );

        //then
        result.andExpectAll(
                status().isBadRequest(),
                content().contentType(APPLICATION_JSON),
                jsonPath("$.path").value("/api/v1/products"),
                jsonPath("$.message", CoreMatchers.startsWith("Validation failed")),
                jsonPath("$.message", containsString(expectedFieldErrorMessage)),
                jsonPath("$.statusCode").value(BAD_REQUEST.value()),
                jsonPath("$.timestamp", notNullValue())
        );

        verifyNoInteractions(productService);
    }

    @Test
    @DisplayName("Should return status 200 OK and empty paged response when getting all products paginated and no products exists")
    public void givenNoProductExists_whenGetAllProductsPaginated_thenReturnStatusOKAndEmptyPagedResponse() throws Exception {
        //given
        int pageNumber = 0;
        int pageSize = 10;
        PagedResponse<ProductDto> emptyPagedResponse = new PagedResponse<>(pageNumber, pageSize, 0L, 0, true, true, emptyList());

        when(productService.getAllProductsPaginated(pageNumber, pageSize)).thenReturn(emptyPagedResponse);

        //when
        ResultActions result = mockMvc.perform(get("/api/v1/products")
                .param("page", String.valueOf(pageNumber))
                .param("size", String.valueOf(pageSize))
                .accept(APPLICATION_JSON)
        );

        //then
        result.andExpectAll(
                status().isOk(),
                content().contentType(MediaType.APPLICATION_JSON),
                jsonPath("$.pageNumber",is(pageNumber)),
                jsonPath("$.pageSize",is(pageSize)),
                jsonPath("$.totalElements",is(0)),
                jsonPath("$.totalPages",is(0)),
                jsonPath("$.first", is(TRUE)),
                jsonPath("$.last", is(TRUE)),
                jsonPath("$.content").isArray(),
                jsonPath("$.content").isEmpty()
        );

        verify(productService, times(1)).getAllProductsPaginated(pageNumber, pageSize);
        verifyNoMoreInteractions(productService);
    }

    @Test
    @DisplayName("Should return status 200 OK and paged response with single product DTO when getting all product paginated and one product exists")
    public void givenOneProductExists_whenGetAllProductsPaginated_thenReturnStatusOkAndPagedResponseWithOneProductDtoAsBody() throws Exception {
        //given
        int pageNumber = 0;
        int pageSize = 10;
        ProductDto productPersistedDto = TestUtils.getProductCottagePersistedDto();

        PagedResponse<ProductDto> pagedResponseWithOneProductDto = new PagedResponse<>(pageNumber, pageSize, 1L, 1, true, true, List.of(productPersistedDto));

        when(productService.getAllProductsPaginated(pageNumber, pageSize)).thenReturn(pagedResponseWithOneProductDto);

        //when
        ResultActions result = mockMvc.perform(get("/api/v1/products")
                .param("page", String.valueOf(pageNumber))
                .param("size", String.valueOf(pageSize))
                .accept(APPLICATION_JSON)
        );

        //then
        result.andExpectAll(
                status().isOk(),
                content().contentType(MediaType.APPLICATION_JSON),
                jsonPath("$.pageNumber",is(pageNumber)),
                jsonPath("$.pageSize",is(pageSize)),
                jsonPath("$.totalElements",is(1)),
                jsonPath("$.totalPages",is(1)),
                jsonPath("$.first", is(TRUE)),
                jsonPath("$.last", is(TRUE)),
                jsonPath("$.content").isArray(),
                jsonPath("$.content").isNotEmpty(),
                jsonPath("$.content.size()", is(1)),
                jsonPath("$.content[0].id", is(productPersistedDto.id().toString())),
                jsonPath("$.content[0].title", is(productPersistedDto.title())),
                jsonPath("$.content[0].details", is(productPersistedDto.details()))
        );

        verify(productService, times(1)).getAllProductsPaginated(pageNumber, pageSize);
        verifyNoMoreInteractions(productService);
    }

    @Test
    @DisplayName("Should return status 200 OK and paged response with products when getting all products paginated and many products exists")
    public void givenManyProductExisted_whenGetAllProducts_thenReturnStatusOKAndListOfProductDtosAsBody() throws Exception {
        //given
        int pageNumber = 0;
        int pageSize = 10;
        List<ProductDto> products = List.of(
                TestUtils.getProductMilkPersistedDto(),
                TestUtils.getProductButterPersistedDto(),
                TestUtils.getProductCottagePersistedDto()
        );

        PagedResponse<ProductDto> pagedResponseWithThreeProductDto = new PagedResponse<>(
                pageNumber, pageSize, products.size(), 1, true, true, products
        );

        when(productService.getAllProductsPaginated(pageNumber, pageSize)).thenReturn(pagedResponseWithThreeProductDto);

        //when
        ResultActions result = mockMvc.perform(get("/api/v1/products")
                .param("page", String.valueOf(pageNumber))
                .param("size", String.valueOf(pageSize))
                .accept(APPLICATION_JSON)
        );

        //then
        result.andExpectAll(
                status().isOk(),
                content().contentType(MediaType.APPLICATION_JSON),
                jsonPath("$.pageNumber",is(pageNumber)),
                jsonPath("$.pageSize",is(pageSize)),
                jsonPath("$.pageNumber",is(pageNumber)),
                jsonPath("$.totalElements",is(3)),
                jsonPath("$.totalPages",is(1)),
                jsonPath("$.first", is(TRUE)),
                jsonPath("$.last", is(TRUE)),
                jsonPath("$.content").isArray(),
                jsonPath("$.content").isNotEmpty(),
                jsonPath("$.content.size()", is(3)),
                jsonPath("$.content[0].id", is(products.get(0).id().toString())),
                jsonPath("$.content[0].title", is(products.get(0).title())),
                jsonPath("$.content[0].details", is(products.get(0).details())),
                jsonPath("$.content[1].id", is(products.get(1).id().toString())),
                jsonPath("$.content[1].title", is(products.get(1).title())),
                jsonPath("$.content[1].details", is(products.get(1).details())),
                jsonPath("$.content[2].id", is(products.get(2).id().toString())),
                jsonPath("$.content[2].title", is(products.get(2).title())),
                jsonPath("$.content[2].details", is(products.get(2).details()))
        );

        verify(productService, times(1)).getAllProductsPaginated(pageNumber, pageSize);
        verifyNoMoreInteractions(productService);
    }

    @Test
    @DisplayName("Should return status 200 OK and product DTO when getting product with existent id")
    public void givenExistentProductId_whenGetProduct_thenReturnStatusOKAndProductDtoAsBody() throws Exception {
        //given
        UUID existentProductId = TestUtils.PRODUCT_COTTAGE_ID;
        ProductDto expectedDto = TestUtils.getProductCottagePersistedDto();
        when(productService.getProductById(existentProductId)).thenReturn(expectedDto);

        //when
        ResultActions result = mockMvc.perform(get("/api/v1/products/{productId}", existentProductId)
                        .accept(APPLICATION_JSON)
        );

        //then
        result.andExpectAll(
                status().isOk(),
                content().contentType(APPLICATION_JSON),
                jsonPath("$.id", is(expectedDto.id().toString())),
                jsonPath("$.title", is(expectedDto.title())),
                jsonPath("$.details", is(expectedDto.details()))
        );

        verify(productService, times(1)).getProductById(existentProductId);
        verifyNoMoreInteractions(productService);
    }

    @Test
    @DisplayName("Should return status 404 NOT FOUND and error response when getting product with not existent id")
    public void givenNotExistentProductId_whenGetProduct_thenReturnStatusNotFoundAndErrorResponseAsBody() throws Exception {
        //given
        UUID notExistentProductId = UUID.randomUUID();
        String expectedExceptionMessage = PRODUCT_NOT_FOUND_WITH_ID.formatted(notExistentProductId);
        when(productService.getProductById(any(UUID.class)))
                .thenThrow(new ResourceNotFoundException(expectedExceptionMessage));

        //when
        ResultActions result = mockMvc.perform(get("/api/v1/products/{productId}", notExistentProductId)
                .accept(APPLICATION_JSON)
        );

        //then
        result.andExpectAll(
                status().isNotFound(),
                content().contentType(APPLICATION_JSON),
                jsonPath("$.path").value("/api/v1/products/" + notExistentProductId),
                jsonPath("$.message").value(expectedExceptionMessage),
                jsonPath("$.statusCode").value(NOT_FOUND.value()),
                jsonPath("$.timestamp", notNullValue())
        );

        verify(productService, times(1)).getProductById(any(UUID.class));
        verifyNoMoreInteractions(productService);
    }

    @Test
    @DisplayName("Should return status 204 NO CONTENT when updating product with existent id and valid request")
    public void givenExistentProductIdAndValidUpdateProductRequest_whenUpdateProduct_thenReturnNoContentStatus() throws Exception {
        //given
        UUID existentProductId = TestUtils.PRODUCT_MILK_ID;
        UpdateProductRequest request = TestUtils.getUpdateProductMilkRequest();
        doNothing().when(productService).updateProduct(existentProductId, request);

        //when
        ResultActions result = mockMvc.perform(put("/api/v1/products/{productId}", existentProductId)
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        );

        //then
        result.andExpectAll(
                status().isNoContent(),
                content().string("")
        );

        verify(productService, times(1)).updateProduct(existentProductId, request);
        verifyNoMoreInteractions(productService);
    }

    @Test
    @DisplayName("Should return status 404 NOT FOUND and error response when updating product with not existent id and valid request")
    public void givenNotExistentProductIdAndValidUpdateProductRequest_whenUpdateProduct_thenReturnStatusNotFoundAndErrorResponseAsBody() throws Exception {
        //given
        UUID notExistentProductId = UUID.randomUUID();
        UpdateProductRequest request = TestUtils.getUpdateProductMilkRequest();
        String expectedExceptionMessage = PRODUCT_NOT_FOUND_WITH_ID.formatted(notExistentProductId);
        doThrow(new ResourceNotFoundException(expectedExceptionMessage))
                .when(productService).updateProduct(notExistentProductId, request);

        //when
        ResultActions result = mockMvc.perform(put("/api/v1/products/{productId}", notExistentProductId)
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        );

        //then
        result.andExpectAll(
                status().isNotFound(),
                content().contentType(APPLICATION_JSON),
                jsonPath("$.path").value("/api/v1/products/" + notExistentProductId),
                jsonPath("$.message").value(expectedExceptionMessage),
                jsonPath("$.statusCode").value(NOT_FOUND.value()),
                jsonPath("$.timestamp").exists()
        );

        verify(productService, times(1)).updateProduct(notExistentProductId, request);
        verifyNoMoreInteractions(productService);
    }


    @Test
    @DisplayName("Should return status 400 BAD REQUEST and error response when updating product with existent id and invalid request")
    public void givenExistentProductIdAndInvalidUpdateProductRequest_whenUpdateProduct_thenReturnStatusBadRequestAndErrorResponseAsBody() throws Exception {
        //given
        UUID existentProductId = TestUtils.PRODUCT_MILK_ID;
        UpdateProductRequest invalidRequest = new UpdateProductRequest("new Title", "");

        //when
        ResultActions result = mockMvc.perform(put("/api/v1/products/{productId}", existentProductId)
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest))
        );

        //then
        result.andExpectAll(
                status().isBadRequest(),
                content().contentType(APPLICATION_JSON),
                jsonPath("$.path").value("/api/v1/products/" + existentProductId),
                jsonPath("$.message", CoreMatchers.startsWith("Validation failed:")),
                jsonPath("$.statusCode").value(BAD_REQUEST.value()),
                jsonPath("$.timestamp", notNullValue())
        );

        verifyNoInteractions(productService);
    }

    @Test
    @DisplayName("Should return status 400 BAD REQUEST and error response when updating product with not existent id and invalid request")
    public void givenNotExistentProductIdAndInvalidUpdateProductRequest_whenUpdateProduct_thenReturnStatusBadRequestAndErrorResponseAsBody() throws Exception {
        //given
        UUID notExistentProductId = UUID.randomUUID();
        UpdateProductRequest invalidRequest = new UpdateProductRequest("new Title", "");

        //when
        ResultActions result = mockMvc.perform(put("/api/v1/products/{productId}", notExistentProductId)
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest))
        );

        //then
        result.andExpectAll(
                status().isBadRequest(),
                content().contentType(APPLICATION_JSON),
                jsonPath("$.path").value("/api/v1/products/" + notExistentProductId),
                jsonPath("$.message", CoreMatchers.startsWith("Validation failed:")),
                jsonPath("$.statusCode").value(BAD_REQUEST.value()),
                jsonPath("$.timestamp", notNullValue())
        );

        verifyNoInteractions(productService);
    }

    @Test
    @DisplayName("Should return status 204 NO CONTENT when deleting product with existent id")
    public void givenExistentProductId_whenDeleteProduct_thenReturnNoContentStatus() throws Exception {
        //given
        UUID existentProductId = TestUtils.PRODUCT_BUTTER_ID;
        doNothing().when(productService).deleteProduct(existentProductId);

        //when
        ResultActions result = mockMvc.perform(delete("/api/v1/products/{productId}", existentProductId)
                .contentType(APPLICATION_JSON)
        );

        //then
        result.andExpect(status().isNoContent());

        verify(productService, times(1)).deleteProduct(existentProductId);
        verifyNoMoreInteractions(productService);
    }

    @Test
    @DisplayName("Should return status 404 NOT FOUND and error response when deleting product with not existent id")
    public void givenNotExistentProductId_whenDeleteProduct_thenReturnStatusNotFoundAndErrorResponseAsBody() throws Exception {
        //given
        UUID notExistentProductId = UUID.randomUUID();
        String expectedExceptionMessage = PRODUCT_NOT_FOUND_WITH_ID.formatted(notExistentProductId);

        doThrow(new ResourceNotFoundException(expectedExceptionMessage))
                .when(productService).deleteProduct(notExistentProductId);

        //when
        ResultActions result = mockMvc.perform(delete("/api/v1/products/{productId}", notExistentProductId)
                .contentType(APPLICATION_JSON)
        );

        //then
        result.andExpectAll(
                status().isNotFound(),
                content().contentType(APPLICATION_JSON),
                jsonPath("$.path").value("/api/v1/products/" + notExistentProductId),
                jsonPath("$.message").value(expectedExceptionMessage),
                jsonPath("$.statusCode").value(NOT_FOUND.value()),
                jsonPath("$.timestamp", notNullValue())
        );

        verify(productService, times(1)).deleteProduct(notExistentProductId);
        verifyNoMoreInteractions(productService);
    }
}
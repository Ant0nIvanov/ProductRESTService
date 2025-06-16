package ru.ivanov.productservice.it;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.ivanov.productservice.model.dto.request.CreateProductRequest;
import ru.ivanov.productservice.model.dto.request.UpdateProductRequest;
import ru.ivanov.productservice.model.entity.Product;
import ru.ivanov.productservice.repository.ProductRepository;
import ru.ivanov.productservice.util.TestUtils;

import java.util.Optional;
import java.util.UUID;

import static java.lang.Boolean.TRUE;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static ru.ivanov.productservice.util.MessageUtils.PRODUCT_NOT_FOUND_WITH_ID;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@AutoConfigureMockMvc
public class ItProductRestControllerTests extends AbstractRestControllerBaseTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProductRepository productRepository;

    @BeforeEach
    public void setUp() {
        productRepository.deleteAll();
    }

    @Test
    @DisplayName("Should return status 201 CREATED and product DTO when creating product with valid request")
    public void givenValidCreatProductRequest_whenCreateProduct_thenReturnStatusCreatedAndCreatedProductDtoAsBody() throws Exception {
        //given
        CreateProductRequest request = TestUtils.getCreateProductMilkRequest();

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
                header().string("Location", containsString("api/v1/products/")),
                jsonPath("$.id", notNullValue()),
                jsonPath("$.title", is(request.title())),
                jsonPath("$.details", is(request.details()))
        );

        assertThat(productRepository.count()).isOne();
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

        assertThat(productRepository.count()).isZero();
    }

    @Test
    @DisplayName("Should return status 200 OK and empty paged response when getting all products paginated and no products exists")
    public void givenNoProductExists_whenGetAllProductsPaginated_thenReturnStatusOKAndEmptyPagedResponse() throws Exception {
        //given
        int pageNumber = 0;
        int pageSize = 10;

        assertThat(productRepository.count()).isZero();

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
    }

    @Test
    @DisplayName("Should return status 200 OK and paged response with single product DTO when getting all product paginated and one product exists")
    public void givenOneProductExists_whenGetAllProductsPaginated_thenReturnStatusOkAndPagedResponseWithOneProductDtoAsBody() throws Exception {
        //given
        int pageNumber = 0;
        int pageSize = 10;
        Product productMilkTransient = TestUtils.getProductMilkTransient();
        Product productMilkPersisted = productRepository.save(productMilkTransient);

        assertThat(productRepository.count()).isOne();

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
                jsonPath("$.content[0].id", is(productMilkPersisted.getId().toString())),
                jsonPath("$.content[0].title", is(productMilkPersisted.getTitle())),
                jsonPath("$.content[0].details", is(productMilkPersisted.getDetails()))
        );
    }

    @Test
    @DisplayName("Should return status 200 OK and paged response with products when getting all products paginated and many products exists")
    public void givenManyProductExisted_whenGetAllProducts_thenReturnStatusOKAndListOfProductDtosAsBody() throws Exception {
        //given
        int pageNumber = 0;
        int pageSize = 10;
        Product productMilkTransient = TestUtils.getProductMilkTransient();
        Product productButterTransient = TestUtils.getProductButterTransient();
        Product productCottageTransient = TestUtils.getProductCottageTransient();
        Product productMilkPersisted = productRepository.save(productMilkTransient);
        Product productButterPersisted = productRepository.save(productButterTransient);
        Product productCottagePersisted = productRepository.save(productCottageTransient);

        assertThat(productRepository.count()).isEqualTo(3);

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
                jsonPath("$.content[*].id", containsInAnyOrder(
                        productMilkPersisted.getId().toString(),
                        productButterPersisted.getId().toString(),
                        productCottagePersisted.getId().toString()
                )),
                jsonPath("$.content[*].title", containsInAnyOrder(
                        productMilkPersisted.getTitle(),
                        productButterPersisted.getTitle(),
                        productCottagePersisted.getTitle()

                )),
                jsonPath("$.content[*].details", containsInAnyOrder(
                        productCottagePersisted.getDetails(),
                        productMilkPersisted.getDetails(),
                        productButterPersisted.getDetails()
                ))
        );
    }

    @Test
    @DisplayName("Should return status 200 OK and product DTO when getting product with existent id")
    public void givenExistentProductId_whenGetProduct_thenReturnStatusOKAndProductDtoAsBody() throws Exception {
        //given
        Product productMilkTransient = TestUtils.getProductMilkTransient();
        Product productMilkPersisted = productRepository.save(productMilkTransient);
        UUID existentProductId = productMilkPersisted.getId();

        assertThat(productRepository.existsById(existentProductId)).isTrue();

        //when
        ResultActions result = mockMvc.perform(get("/api/v1/products/{productId}", existentProductId)
                .accept(APPLICATION_JSON)
        );

        //then
        result.andExpectAll(
                status().isOk(),
                content().contentType(APPLICATION_JSON),
                jsonPath("$.id", is(productMilkPersisted.getId().toString())),
                jsonPath("$.title", is(productMilkPersisted.getTitle())),
                jsonPath("$.details", is(productMilkPersisted.getDetails()))
        );
    }

    @Test
    @DisplayName("Should return status 404 NOT FOUND and error response when getting product with not existent id")
    public void givenNotExistentProductId_whenGetProduct_thenReturnStatusNotFoundAndErrorResponseAsBody() throws Exception {
        //given
        UUID notExistentProductId = UUID.randomUUID();
        String expectedExceptionMessage = PRODUCT_NOT_FOUND_WITH_ID.formatted(notExistentProductId);

        assertThat(productRepository.existsById(notExistentProductId)).isFalse();

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
    }

    @Test
    @DisplayName("Should return status 204 NO CONTENT when updating product with existent id and valid request")
    public void givenExistentProductIdAndValidUpdateProductRequest_whenUpdateProduct_thenReturnNoContentStatus() throws Exception {
        //given
        Product productMilkTransient = TestUtils.getProductMilkTransient();
        Product productMilkPersisted = productRepository.save(productMilkTransient);
        UUID existentProductId = productMilkPersisted.getId();

        UpdateProductRequest request = TestUtils.getUpdateProductMilkRequest();

        assertThat(productRepository.existsById(existentProductId)).isTrue();

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

        assertThat(productRepository.existsById(existentProductId)).isTrue();
        Optional<Product> obtainedProductOptional = productRepository.findById(existentProductId);
        assertThat(obtainedProductOptional).isPresent();
        Product obtainedProduct = obtainedProductOptional.get();
        assertThat(obtainedProduct.getTitle()).isEqualTo(request.title());
        assertThat(obtainedProduct.getDetails()).isEqualTo(request.details());
    }

    @Test
    @DisplayName("Should return status 404 NOT FOUND and error response when updating product with not existent id and valid request")
    public void givenNotExistentProductIdAndValidUpdateProductRequest_whenUpdateProduct_thenReturnStatusNotFoundAndErrorResponseAsBody() throws Exception {
        //given
        UUID notExistentProductId = UUID.randomUUID();
        UpdateProductRequest request = TestUtils.getUpdateProductMilkRequest();
        String expectedExceptionMessage = PRODUCT_NOT_FOUND_WITH_ID.formatted(notExistentProductId);

        assertThat(productRepository.existsById(notExistentProductId)).isFalse();

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
    }

    @Test
    @DisplayName("Should return status 400 BAD REQUEST and error response when updating product with existent id and invalid request")
    public void givenExistentProductIdAndInvalidUpdateProductRequest_whenUpdateProduct_thenReturnStatusBadRequestAndErrorResponseAsBody() throws Exception {
        //given
        Product productMilkTransient = TestUtils.getProductMilkTransient();
        Product productMilkPersisted = productRepository.save(productMilkTransient);
        UUID existentProductId = productMilkPersisted.getId();

        UpdateProductRequest invalidRequest = new UpdateProductRequest("new Title", "");

        assertThat(productRepository.existsById(existentProductId)).isTrue();

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

        assertThat(productRepository.existsById(existentProductId)).isTrue();
        Optional<Product> obtainedProductOptional = productRepository.findById(existentProductId);
        assertThat(obtainedProductOptional).isPresent();
        Product obtainedProduct = obtainedProductOptional.get();
        assertThat(obtainedProduct).usingRecursiveComparison().isEqualTo(productMilkPersisted);
    }

    @Test
    @DisplayName("Should return status 400 BAD REQUEST and error response when updating product with not existent id and invalid request")
    public void givenNotExistentProductIdAndInvalidUpdateProductRequest_whenUpdateProduct_thenReturnStatusBadRequestAndErrorResponseAsBody() throws Exception {
        //given
        UUID notExistentProductId = UUID.randomUUID();
        UpdateProductRequest invalidRequest = new UpdateProductRequest("new Title", "");

        assertThat(productRepository.existsById(notExistentProductId)).isFalse();

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
    }

    @Test
    @DisplayName("Should return status 204 NO CONTENT when deleting product with existent id")
    public void givenExistentProductId_whenDeleteProduct_thenReturnNoContentStatus() throws Exception {
        //given
        Product productMilkTransient = TestUtils.getProductMilkTransient();
        Product productMilkPersisted = productRepository.save(productMilkTransient);
        UUID existentProductId = productMilkPersisted.getId();

        assertThat(productRepository.existsById(existentProductId)).isTrue();

        //when
        ResultActions result = mockMvc.perform(delete("/api/v1/products/{productId}", existentProductId)
                .contentType(APPLICATION_JSON)
        );

        //then
        result.andExpect(status().isNoContent());

        assertThat(productRepository.existsById(existentProductId)).isFalse();
        assertThat(productRepository.findById(existentProductId)).isNotPresent();
    }

    @Test
    @DisplayName("Should return status 404 NOT FOUND and error response when deleting product with not existent id")
    public void givenNotExistentProductId_whenDeleteProduct_thenReturnStatusNotFoundAndErrorResponseAsBody() throws Exception {
        //given
        UUID notExistentProductId = UUID.randomUUID();
        String expectedExceptionMessage = PRODUCT_NOT_FOUND_WITH_ID.formatted(notExistentProductId);

        assertThat(productRepository.existsById(notExistentProductId)).isFalse();

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
    }
}
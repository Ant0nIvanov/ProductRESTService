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

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;
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
    @DisplayName("Test create product functionality with valid createProductRequest")
    public void givenValidCreatProductRequest_whenCreateProduct_thenReturnStatusCreatedAndProductDtoAsBody() throws Exception {
        //given
        CreateProductRequest request = TestUtils.getCreateProductMilkRequest();

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
                header().string("Location", notNullValue()),
                jsonPath("$.id", notNullValue()),
                jsonPath("$.title", is(request.title())),
                jsonPath("$.details", is(request.details()))
        );
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
    }

    @Test
    @DisplayName("Test get all products functionality when no product existed")
    public void givenNoProductExists_whenGetAllProducts_thenReturnStatusOKAndEmptyListAsBody() throws Exception {
        //given

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
    }

    @Test
    @DisplayName("Test get all product functionality when ony one product existed")
    public void givenOneProductExists_whenGetAllProducts_thenReturnStatusOkAndListWithOneProductDtoAsBody() throws Exception {
        //given
        Product product = TestUtils.getProductMilkTransient();
        productRepository.save(product);

        //when
        ResultActions result = mockMvc.perform(get("/api/v1/products")
                .contentType(MediaType.APPLICATION_JSON)
        );

        //then
        result.andExpectAll(
                status().isOk(),
                content().contentType(MediaType.APPLICATION_JSON),
                jsonPath("$", hasSize(1)),
                jsonPath("$[0].id", notNullValue()),
                jsonPath("$[0].title", is(product.getTitle())),
                jsonPath("$[0].details", is(product.getDetails()))
        );
    }

    @Test
    @DisplayName("Test get all products functionality when many product existed")
    public void givenManyProductExisted_whenGetAllProducts_thenReturnStatusOKAndListOfProductDtosAsBody() throws Exception {
        //given
        Product productMilk = TestUtils.getProductMilkTransient();
        Product productButter = TestUtils.getProductButterTransient();
        Product productCottage = TestUtils.getProductCottageTransient();
        productRepository.saveAll(List.of(productMilk, productButter, productCottage));

        //when
        ResultActions result = mockMvc.perform(get("/api/v1/products")
                .contentType(MediaType.APPLICATION_JSON)
        );

        //then
        result.andExpectAll(
                status().isOk(),
                content().contentType(MediaType.APPLICATION_JSON),
                jsonPath("$", hasSize(3)),

                jsonPath("$[0].id", notNullValue()),
//                jsonPath("$[0].title").value(milkDto.title()),
//                jsonPath("$[0].details").value(milkDto.details()),

                jsonPath("$[1].id", notNullValue()),
//                jsonPath("$[1].title").value(butterDto.title()),
//                jsonPath("$[1].details").value(butterDto.details()),

                jsonPath("$[2].id", notNullValue())
//                jsonPath("$[2].title").value(cottageDto.title()),
//                jsonPath("$[2].details").value(cottageDto.details())
        );

    }

    @Test
    @DisplayName("Test get product by id functionality with existent product id")
    public void givenExistentProductId_whenGetProduct_thenReturnStatusOKAndProductDtoAsBody() throws Exception {
        //given
        Product product = TestUtils.getProductCottageTransient();
        Product savedProduct = productRepository.save(product);
        UUID existentProductId = savedProduct.getId();

        //when
        ResultActions result = mockMvc.perform(get("/api/v1/products/{productId}", existentProductId)
                .contentType(MediaType.APPLICATION_JSON)
        );

        //then
        result.andExpectAll(
                status().isOk(),
                content().contentType(MediaType.APPLICATION_JSON),
                jsonPath("$.id", notNullValue()),
                jsonPath("$.title", is(product.getTitle())),
                jsonPath("$.details", is(product.getDetails()))
        );
    }

    @Test
    @DisplayName("Test get product by id functionality with not existent product id")
    public void givenNotExistentProductId_whenGetProduct_thenReturnStatusNotFoundAndErrorResponseAsBody() throws Exception {
        //given
        UUID notExistentProductId = UUID.randomUUID();

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
    }

    @Test
    @DisplayName("Test update product by id functionality with existent product id and valid updateProductRequest")
    public void givenExistentProductIdAndValidUpdateProductRequest_whenUpdateProduct_thenReturnNoContentStatus() throws Exception {
        //given
        Product product = TestUtils.getProductMilkTransient();
        Product savedProduct = productRepository.save(product);
        UUID existentProductId = savedProduct.getId();
        UpdateProductRequest request = TestUtils.getUpdateProductMilkRequest();

        //when
        ResultActions result = mockMvc.perform(put("/api/v1/products/{productId}", existentProductId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        );

        //then
        result.andExpectAll(
                status().isNoContent()
        );
    }

    @Test
    @DisplayName("Test update product by id functionality with not existent product id and valid updateProductRequest")
    public void givenNotExistentProductIdAndValidUpdateProductRequest_whenUpdateProduct_thenReturnStatusNotFoundAndErrorResponseAsBody() throws Exception {
        //given
        UUID notExistentProductId = UUID.randomUUID();
        UpdateProductRequest request = TestUtils.getUpdateProductMilkRequest();

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
    }

    @Test
    @DisplayName("Test update product by id functionality with product id and invalid updateProductRequest")
    public void givenExistentProductIdAndInvalidUpdateProductRequest_whenUpdateProduct_thenReturnStatusBadRequestAndErrorResponseAsBody() throws Exception {
        //given
        Product product = TestUtils.getProductMilkTransient();
        Product savedProduct = productRepository.save(product);
        UUID existentProductId = savedProduct.getId();
        UpdateProductRequest request = new UpdateProductRequest("new Title", "");

        //when
        ResultActions result = mockMvc.perform(put("/api/v1/products/{productId}", existentProductId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        );

        //then
        result.andExpectAll(
                status().isBadRequest(),
                content().contentType(MediaType.APPLICATION_JSON),
                jsonPath("$.path").value("/api/v1/products/" + existentProductId),
                jsonPath("$.message", CoreMatchers.startsWith("Validation failed:")),
                jsonPath("$.statusCode").value(BAD_REQUEST.value()),
                jsonPath("$.timestamp", notNullValue())
        );
    }

    @Test
    @DisplayName("Test update product by id functionality with product id and invalid updateProductRequest")
    public void givenNotExistentProductIdAndInvalidUpdateProductRequest_whenUpdateProduct_thenReturnStatusBadRequestAndErrorResponseAsBody() throws Exception {
        //given
        UUID notExistentProductId = UUID.randomUUID();
        UpdateProductRequest request = new UpdateProductRequest("new Title", "");

        //when
        ResultActions result = mockMvc.perform(put("/api/v1/products/{productId}", notExistentProductId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        );

        //then
        result.andExpectAll(
                status().isBadRequest(),
                content().contentType(MediaType.APPLICATION_JSON),
                jsonPath("$.path").value("/api/v1/products/" + notExistentProductId),
                jsonPath("$.message", CoreMatchers.startsWith("Validation failed:")),
                jsonPath("$.statusCode").value(BAD_REQUEST.value()),
                jsonPath("$.timestamp", notNullValue())
        );
    }

    @Test
    @DisplayName("Test delete product by id functionality with existent product id")
    public void givenExistentProductId_whenDeleteProduct_thenReturnNoContentStatus() throws Exception {
        //given
        Product product = TestUtils.getProductMilkTransient();
        Product savedProduct = productRepository.save(product);
        UUID existentProductId = savedProduct.getId();

        //when
        ResultActions result = mockMvc.perform(delete("/api/v1/products/{productId}", existentProductId)
                .contentType(MediaType.APPLICATION_JSON)
        );

        //then
        Product obtainedProduct = productRepository.findById(existentProductId).orElse(null);
        assertThat(obtainedProduct).isNull();

        result.andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Test delete product by id functionality with not existent product id")
    public void givenNotExistentProductId_whenDeleteProduct_thenReturnStatusNotFoundAndErrorResponseAsBody() throws Exception {
        //given
        UUID notExistentProductId = UUID.randomUUID();

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
    }
}
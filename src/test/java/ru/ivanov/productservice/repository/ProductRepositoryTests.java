package ru.ivanov.productservice.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.util.CollectionUtils;
import ru.ivanov.productservice.model.entity.Product;
import ru.ivanov.productservice.util.DataUtils;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class ProductRepositoryTests {
    @Autowired
    private ProductRepository repositoryUnderTest;

    @BeforeEach
    public void setUp() {
        repositoryUnderTest.deleteAll();
    }

    @Test
    @DisplayName("Test save product functionality")
    public void givenProductObject_whenSave_thenProductIsSaved() {
        //given
        Product productToSave = DataUtils.getProductMilkTransient();

        //when
        Product savedProduct = repositoryUnderTest.save(productToSave);

        //then
        assertThat(savedProduct).isNotNull();
        assertThat(savedProduct.getId()).isNotNull();
        assertThat(savedProduct.getTitle()).isEqualTo(productToSave.getTitle());
        assertThat(savedProduct.getDetails()).isEqualTo(productToSave.getDetails());
    }

    @Test
    @DisplayName("Test get product by id functionality with existent product id")
    public void givenProductSaved_whenGetById_thenProductIsReturned() {
        //given
        Product productToSave = DataUtils.getProductMilkTransient();
        Product savedProduct = repositoryUnderTest.save(productToSave);

        //when
        Product obtainedProduct = repositoryUnderTest.findById(savedProduct.getId()).orElse(null);

        //then
        assertNotNull(obtainedProduct);
        assertThat(obtainedProduct.getId()).isEqualTo(savedProduct.getId());
        assertThat(obtainedProduct.getTitle()).isEqualTo(savedProduct.getTitle());
        assertThat(obtainedProduct.getDetails()).isEqualTo(savedProduct.getDetails());
    }

    @Test
    @DisplayName("Test get product by id functionality with not existent product id")
    public void givenProductIsNotSaved_whenGetById_thenOptionalIsEmpty() {
        //given

        //when
        Product obtainedProduct = repositoryUnderTest.findById(UUID.randomUUID()).orElse(null);
        //then
        assertThat(obtainedProduct).isNull();
    }

    @Test
    @DisplayName("Test get all products functionality when no products are existed")
    public void givenNoProductsIsExisted_whenGetAll_thenReturnEmptyList() {
        //given

        //when
        List<Product> obtainedProducts = repositoryUnderTest.findAll();

        //then
        assertThat(CollectionUtils.isEmpty(obtainedProducts)).isTrue();
    }

    @Test
    @DisplayName("Test get all products functionality when only one product is existed")
    public void givenOneProductIsExisted_whenGetAll_thenReturnListWithOneProduct() {
        //given
        Product productButterToSave = DataUtils.getProductButterTransient();
        Product savedProduct = repositoryUnderTest.save(productButterToSave);

        //when
        List<Product> obtainedProducts = repositoryUnderTest.findAll();

        //then
        assertThat(CollectionUtils.isEmpty(obtainedProducts)).isFalse();
        assertThat(obtainedProducts.size()).isEqualTo(1);
        assertThat(savedProduct).isIn(obtainedProducts);
    }

    @Test
    @DisplayName("Test get all products functionality when many products are existed")
    public void givenThreeProductsAreExisted_whenGetAll_thenThreeProductsAreReturned() {
        //given
        Product productMilkToSave = DataUtils.getProductMilkTransient();
        Product productButterToSave = DataUtils.getProductButterTransient();
        Product productCottageToSave = DataUtils.getProductCottageTransient();
        Product savedMilk = repositoryUnderTest.save(productMilkToSave);
        Product savedButter = repositoryUnderTest.save(productButterToSave);
        Product savedCottage = repositoryUnderTest.save(productCottageToSave);

        //when
        List<Product> obtainedProducts = repositoryUnderTest.findAll();

        //then
        assertThat(CollectionUtils.isEmpty(obtainedProducts)).isFalse();
        assertThat(obtainedProducts.size()).isEqualTo(3);
        assertThat(savedMilk).isIn(obtainedProducts);
        assertThat(savedButter).isIn(obtainedProducts);
        assertThat(savedCottage).isIn(obtainedProducts);
    }

    @Test
    @DisplayName("Test update product functionality")
    public void givenProductToUpdate_whenSave_thenDetailsIsChanged() {
        //given
        String updatedDetails = "Updated product details";
        Product productToSave = DataUtils.getProductMilkTransient();
        Product savedProduct = repositoryUnderTest.save(productToSave);

        //when
        Product productToUpdate = repositoryUnderTest.findById(savedProduct.getId()).orElse(null);
        assertNotNull(productToUpdate);

        productToUpdate.setDetails(updatedDetails);
        Product updatedProduct = repositoryUnderTest.save(productToUpdate);

        //then
        assertThat(updatedProduct).isNotNull();
        assertThat(updatedProduct.getId()).isEqualTo(productToUpdate.getId());
        assertThat(updatedProduct.getTitle()).isEqualTo(productToUpdate.getTitle());
        assertThat(updatedProduct.getDetails()).isEqualTo(updatedDetails);
    }

    @Test
    @DisplayName("Test product is existed with existent product id")
    public void givenExistentProductId_whenExistsById_thenReturnTrue() {
        //given
        Product product = DataUtils.getProductCottageTransient();
        Product savedProduct = repositoryUnderTest.save(product);

        //when
        boolean isProductExists = repositoryUnderTest.existsById(savedProduct.getId());

        // then
        assertThat(isProductExists).isTrue();
    }

    @Test
    @DisplayName("Test product is existed with not existent product id")
    public void givenNotExistentProductId_whenExistsById_thenReturnFalse() {
        // given
        UUID notExistentId = UUID.randomUUID();

        //when
        boolean isProductExists = repositoryUnderTest.existsById(notExistentId);

        //then
        assertThat(isProductExists).isFalse();
    }


    @Test
    @DisplayName("Test delete product by id functionality")
    public void givenProductSaved_whenDeleteById_thenProductIsRemovedFromDB() {
        //given
        Product productToSave = DataUtils.getProductMilkTransient();
        Product savedProduct = repositoryUnderTest.save(productToSave);

        //when
        repositoryUnderTest.deleteById(savedProduct.getId());

        //then
        Product obtainedProduct = repositoryUnderTest.findById(savedProduct.getId()).orElse(null);
        assertThat(obtainedProduct).isNull();
    }
}
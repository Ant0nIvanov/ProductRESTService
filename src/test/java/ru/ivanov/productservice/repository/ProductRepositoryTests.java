package ru.ivanov.productservice.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import ru.ivanov.productservice.model.entity.Product;
import ru.ivanov.productservice.util.TestUtils;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@ActiveProfiles("test")
@DataJpaTest
public class ProductRepositoryTests {
    @Autowired
    private ProductRepository repositoryUnderTest;

    @BeforeEach
    public void setUp() {
        repositoryUnderTest.deleteAll();
    }

    @Test
    @DisplayName("Should save product and generate id when saving product")
    public void givenProduct_whenSave_thenSaveProduct() {
        //given
        Product productTransient = TestUtils.getProductMilkTransient();

        //when
        Product productPersisted = repositoryUnderTest.save(productTransient);

        //then
        assertThat(productPersisted).isNotNull();
        assertThat(productPersisted.getId()).isNotNull();
        assertThat(productPersisted.getId()).isInstanceOf(UUID.class);
        assertThat(productPersisted.getTitle()).isEqualTo(productTransient.getTitle());
        assertThat(productPersisted.getDetails()).isEqualTo(productTransient.getDetails());

        Product obtainedProduct = repositoryUnderTest.findById(productPersisted.getId()).orElse(null);
        assertThat(obtainedProduct).isNotNull();
        assertThat(obtainedProduct).usingRecursiveComparison().isEqualTo(productPersisted);
    }

    @Test
    @DisplayName("Should return Optional with product when existing product id is provided")
    public void givenExistentProductId_whenFindById_thenReturnProduct() {
        //given
        Product productTransient = TestUtils.getProductMilkTransient();
        Product productPersisted = repositoryUnderTest.save(productTransient);
        UUID existentProductId = productPersisted.getId();

        assertThat(repositoryUnderTest.existsById(existentProductId)).isTrue();

        //when
        Optional<Product> obtainedProduct = repositoryUnderTest.findById(existentProductId);

        //then
        assertThat(obtainedProduct).isPresent();
        assertThat(obtainedProduct).get().usingRecursiveComparison().isEqualTo(productPersisted);
    }

    @Test
    @DisplayName("Should return empty Optional when product is not exist")
    public void givenNotExistentProductId_whenFindById_thenOptionalIsEmpty() {
        //given
        UUID notExistentProductId = UUID.randomUUID();

        assertThat(repositoryUnderTest.existsById(notExistentProductId)).isFalse();

        //when
        Optional<Product> obtainedProduct = repositoryUnderTest.findById(notExistentProductId);
        //then
        assertThat(obtainedProduct).isNotPresent();
    }

    @Test
    @DisplayName("Should return empty page when finding all products with pageable from empty repository")
    public void givenEmptyRepository_whenFindAllWithPageable_thenReturnEmptyPage() {
        //given
        int pageNumber = 0;
        int pageSize = 10;
        Pageable pageable = PageRequest.of(pageNumber, pageSize);

        assertThat(repositoryUnderTest.count()).isZero();

        //when
        Page<Product> obtainedPage = repositoryUnderTest.findAll(pageable);

        //then
        assertThat(obtainedPage).isNotNull();
        assertThat(obtainedPage)
                .returns(true, Page::isEmpty)
                .returns(pageNumber, Page::getNumber)
                .returns(pageSize, Page::getSize)
                .returns(0L, Page::getTotalElements)
                .returns(0, Page::getTotalPages)
                .returns(true, Page::isFirst)
                .returns(true, Page::isLast);
    }

    @Test
    @DisplayName("Should return page with single product when finding all products with pageable from DB which contains only one product")
    public void givenOneProductIsExisted_whenFindAllWithPageable_thenReturnPageWithOneProduct() {
        //given
        int pageNumber = 0;
        int pageSize = 10;
        Pageable pageable = PageRequest.of(pageNumber, pageSize);

        Product productTransient = TestUtils.getProductButterTransient();
        Product productPersisted = repositoryUnderTest.save(productTransient);

        assertThat(repositoryUnderTest.count()).isOne();

        //when
        Page<Product> obtainedPage = repositoryUnderTest.findAll(pageable);

        //then
        assertThat(obtainedPage).isNotNull();
        assertThat(obtainedPage)
                .returns(false, Page::isEmpty)
                .returns(pageNumber, Page::getNumber)
                .returns(pageSize, Page::getSize)
                .returns(1L, Page::getTotalElements)
                .returns(1, Page::getTotalPages)
                .returns(true, Page::isFirst)
                .returns(true, Page::isLast);
        assertThat(productPersisted).isIn(obtainedPage.getContent());
    }

    @Test
    @DisplayName("Should return page with many products when finding all products with pageable from DB which contains many products")
    public void givenThreeProductsAreExisted_whenFindAllWithPageable_thenReturnPageWithThreeProducts() {
        //given
        int pageNumber = 0;
        int pageSize = 10;
        Pageable pageable = PageRequest.of(pageNumber, pageSize);

        Product productMilkTransient = TestUtils.getProductMilkTransient();
        Product productButterTransient = TestUtils.getProductButterTransient();
        Product productCottageTransient = TestUtils.getProductCottageTransient();
        Product productMilkPersisted = repositoryUnderTest.save(productMilkTransient);
        Product productButterPersisted = repositoryUnderTest.save(productButterTransient);
        Product productCottagePersisted = repositoryUnderTest.save(productCottageTransient);

        //when
        Page<Product> obtainedPage = repositoryUnderTest.findAll(pageable);

        //then
        assertThat(obtainedPage).isNotNull();
        assertThat(obtainedPage)
                .returns(false, Page::isEmpty)
                .returns(pageNumber, Page::getNumber)
                .returns(pageSize, Page::getSize)
                .returns(3L, Page::getTotalElements)
                .returns(1, Page::getTotalPages)
                .returns(true, Page::isFirst)
                .returns(true, Page::isLast);
        assertThat(productMilkPersisted).isIn(obtainedPage.getContent());
        assertThat(productButterPersisted).isIn(obtainedPage.getContent());
        assertThat(productCottagePersisted).isIn(obtainedPage.getContent());
    }

    @Test
    @DisplayName("Should update product when updating product")
    public void givenExistingProduct_whenSave_thenProductIsUpdated() {
        //given
        Product productTransient = TestUtils.getProductMilkTransient();
        Product productPersisted = repositoryUnderTest.save(productTransient);
        UUID existentProductId = productPersisted.getId();

        String detailsToUpdate = "Updated product details";

        assertThat(repositoryUnderTest.existsById(existentProductId)).isTrue();

        //when
        Optional<Product> obtainedOptional = repositoryUnderTest.findById(existentProductId);
        assertThat(obtainedOptional).isPresent();

        Product productToUpdate = obtainedOptional.get();
        productToUpdate.setDetails(detailsToUpdate);
        Product updatedProductPersisted = repositoryUnderTest.save(productToUpdate);

        //then
        assertThat(updatedProductPersisted).isNotNull();
        assertThat(updatedProductPersisted.getId()).isEqualTo(productToUpdate.getId());
        assertThat(updatedProductPersisted.getTitle()).isEqualTo(productToUpdate.getTitle());
        assertThat(updatedProductPersisted.getDetails()).isEqualTo(detailsToUpdate);
    }

    @Test
    @DisplayName("Should return true when checking product existence by existent product id")
    public void givenExistentProductId_whenExistsById_thenReturnTrue() {
        //given
        Product productTransient = TestUtils.getProductCottageTransient();
        Product productPersisted = repositoryUnderTest.save(productTransient);
        UUID existentProductId = productPersisted.getId();

        assertThat(repositoryUnderTest.findById(existentProductId)).isPresent();

        //when
        boolean isProductExists = repositoryUnderTest.existsById(existentProductId);

        // then
        assertThat(isProductExists).isTrue();
    }

    @Test
    @DisplayName("Should return false when checking product existence by not existent product id")
    public void givenNotExistentProductId_whenExistsById_thenReturnFalse() {
        // given
        UUID notExistentId = UUID.randomUUID();

        assertThat(repositoryUnderTest.findById(notExistentId)).isNotPresent();

        //when
        boolean isProductExists = repositoryUnderTest.existsById(notExistentId);

        //then
        assertThat(isProductExists).isFalse();
    }

    @Test
    @DisplayName("Should delete product from DB when deleting with existent product id")
    public void givenExistentProductId_whenDeleteById_thenProductIsRemovedFromDB() {
        //given
        Product productTransient = TestUtils.getProductMilkTransient();
        Product productPersisted = repositoryUnderTest.save(productTransient);
        UUID existentProductId = productPersisted.getId();

        assertThat(repositoryUnderTest.existsById(existentProductId)).isTrue();

        //when
        repositoryUnderTest.deleteById(existentProductId);

        //then
        boolean isProductExists = repositoryUnderTest.existsById(existentProductId);
        Optional<Product> obtainedProductOptional = repositoryUnderTest.findById(existentProductId);
        assertThat(obtainedProductOptional).isNotPresent();
        assertThat(isProductExists).isFalse();
    }
}
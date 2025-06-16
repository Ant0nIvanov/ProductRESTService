package ru.ivanov.productservice.util;

import ru.ivanov.productservice.model.dto.ProductDto;
import ru.ivanov.productservice.model.dto.request.CreateProductRequest;
import ru.ivanov.productservice.model.dto.request.UpdateProductRequest;
import ru.ivanov.productservice.model.entity.Product;

import java.util.UUID;

public class TestUtils {
    public static final UUID PRODUCT_MILK_ID = UUID.fromString("91efd04b-71b0-4da0-9c51-0eb94e0a63ca");
    public static final UUID PRODUCT_BUTTER_ID = UUID.fromString("52c6d5b6-b73d-415a-bcf4-a4e59d520015");
    public static final UUID PRODUCT_COTTAGE_ID = UUID.fromString("b8477e47-d4f6-4d42-8cc2-7b734cdb1d1e");

    public static Product getProductMilkTransient() {
        return new Product("Milk", "Best milk in the world");
    }

    public static Product getProductButterTransient() {
        return new Product("Butter", "Best butter in the world");
    }

    public static Product getProductCottageTransient() {
        return new Product("Cottage", "Best cottage in the world");
    }

    public static Product getProductMilkPersisted() {
         Product productTransient = getProductMilkTransient();
         productTransient.setId(PRODUCT_MILK_ID);
        return productTransient;
    }

    public static Product getProductButterPersisted() {
        Product productTransient = getProductButterTransient();
        productTransient.setId(PRODUCT_BUTTER_ID);
        return productTransient;
    }

    public static Product getProductCottagePersisted() {
        Product productTransient = getProductCottageTransient();
        productTransient.setId(PRODUCT_COTTAGE_ID);
        return productTransient;
    }

    public static CreateProductRequest getCreateProductMilkRequest() {
        return new CreateProductRequest("Milk", "Best milk in the world");
    }

    public static UpdateProductRequest getUpdateProductMilkRequest() {
        return new UpdateProductRequest("Milk", "Ordinary milk");
    }

    public static Product getUpdatedProductMilk() {
        Product productToUpdateByUpdateProductMilkRequest = getProductMilkPersisted();
        productToUpdateByUpdateProductMilkRequest.setDetails("Ordinary milk");
        return productToUpdateByUpdateProductMilkRequest;
    }

    public static ProductDto getProductMilkPersistedDto() {
        return new ProductDto(PRODUCT_MILK_ID, "Milk", "Best milk in the world");
    }

    public static ProductDto getProductButterPersistedDto() {
        return new ProductDto(PRODUCT_BUTTER_ID, "Butter", "Best butter in the world");
    }

    public static ProductDto getProductCottagePersistedDto() {
        return new ProductDto(PRODUCT_COTTAGE_ID, "Cottage", "Best cottage in the world");
    }
}

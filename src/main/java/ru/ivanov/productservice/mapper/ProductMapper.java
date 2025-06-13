package ru.ivanov.productservice.mapper;

import org.mapstruct.Mapper;
import ru.ivanov.productservice.model.dto.ProductDto;
import ru.ivanov.productservice.model.entity.Product;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    ProductDto toDto(Product product);

}
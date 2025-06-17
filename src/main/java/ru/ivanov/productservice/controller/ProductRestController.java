package ru.ivanov.productservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import ru.ivanov.productservice.model.dto.ProductDto;
import ru.ivanov.productservice.model.dto.request.CreateProductRequest;
import ru.ivanov.productservice.model.dto.request.UpdateProductRequest;
import ru.ivanov.productservice.model.dto.response.ErrorResponse;
import ru.ivanov.productservice.model.dto.response.PagedResponse;
import ru.ivanov.productservice.service.ProductService;

import java.net.URI;
import java.util.UUID;

import static org.springframework.http.MediaType.*;

@RestController
@RequestMapping("api/v1/products")
@Tag(name = "Product API")
public class ProductRestController {
    private final ProductService productService;

    public ProductRestController(ProductService productService) {
        this.productService = productService;
    }

    @Operation(
            summary = "Create new product",
            description = "Creates a new product with data from request",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Product creation data",
                    required = true,
                    content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = CreateProductRequest.class))
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "Product is successfully created",
                            content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProductDto.class))
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid request data",
                            content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))
                    )
            }
    )
    @PostMapping
    public ResponseEntity<ProductDto> createProduct(@Valid @RequestBody CreateProductRequest request) {
        ProductDto product = productService.createProduct(request);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{productId}")
                .buildAndExpand(product.id())
                .toUri();
        return ResponseEntity.created(location)
                .contentType(APPLICATION_JSON)
                .body(product);
    }

    @Operation(
            summary = "Get paginated list of products",
            description = "Retrieves a paginated list of all products",
            parameters = {
                    @Parameter(
                            name = "page",
                            description = "Zero-based page number",
                            in = ParameterIn.QUERY,
                            example = "0",
                            schema = @Schema(type = "integer", defaultValue = "0", minimum = "0")),
                    @Parameter(
                            name = "size",
                            description = "Number of items per page",
                            in = ParameterIn.QUERY,
                            example = "10",
                            schema = @Schema(type = "integer", defaultValue = "10", minimum = "1")
                    )
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Paginated list of product successfully retrieved",
                            content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = PagedResponse.class))
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid pagination parameters",
                            content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))
                    )
            }
    )
    @GetMapping
    public ResponseEntity<PagedResponse<ProductDto>> getAllProductsPaginated(
            @RequestParam(name = "page", required = false, defaultValue = "0") int pageNumber,
            @RequestParam(name = "size", required = false, defaultValue = "10")  int pageSize
    ) {
        PagedResponse<ProductDto> page = productService.getAllProductsPaginated(pageNumber, pageSize);
        return ResponseEntity.ok()
                .contentType(APPLICATION_JSON)
                .body(page);
    }

    @Operation(
            summary = "Get product",
            description = "Retrieve product by ID",
            parameters = {
                    @Parameter(
                            name = "productId",
                            description = "ID of the product to retrieve",
                            required = true,
                            in = ParameterIn.PATH,
                            example = "9b63c77d-8e91-4f40-adb1-6817b92081ab",
                            schema = @Schema(type = "string", format = "uuid")
                    )
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Product retrieved successfully",
                            content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProductDto.class))
                    ),
                    @ApiResponse(
                            responseCode = "204",
                            description = "Product not found",
                            content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))
                    )
            }
    )
    @GetMapping("{productId}")
    public ResponseEntity<ProductDto> getProduct(@PathVariable("productId") UUID productId) {
        ProductDto product = productService.getProductById(productId);
        return ResponseEntity.ok()
                .contentType(APPLICATION_JSON)
                .body(product);
    }

    @Operation(
            summary = "Update product",
            description = "Updates product by ID",
            parameters = {
                    @Parameter(
                            name = "productId",
                            description = "ID of the product to update",
                            required = true,
                            in = ParameterIn.PATH,
                            example = "9b63c77d-8e91-4f40-adb1-6817b92081ab",
                            schema = @Schema(type = "string", format = "uuid")
                    )
            },
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Product data for update",
                    required = true,
                    content = @Content(
                            mediaType = APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = UpdateProductRequest.class)
                    )
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "204",
                            description = "Product updated successfully"
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Product for update not found",
                            content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid request data",
                            content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))
                    )
            }
    )
    @PutMapping("{productId}")
    public ResponseEntity<Void> updateProduct(
            @PathVariable("productId") UUID productId,
            @Valid @RequestBody UpdateProductRequest request
    ) {
        productService.updateProduct(productId, request);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Delete product",
            description = "Delete product by ID",
            parameters = {
                    @Parameter(
                            name = "productId",
                            description = "ID of the product to delete",
                            required = true,
                            in = ParameterIn.PATH,
                            example = "9b63c77d-8e91-4f40-adb1-6817b92081ab",
                            schema = @Schema(type = "string", format = "uuid")
                    )
            },
            responses = {
                    @ApiResponse(responseCode = "204", description = "Product deleted successfully"),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Product not found",
                            content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))
                    )
            }
    )
    @DeleteMapping("{productId}")
    public ResponseEntity<Void> deleteProduct(@PathVariable("productId") UUID productId) {
        productService.deleteProduct(productId);
        return ResponseEntity.noContent().build();
    }
}
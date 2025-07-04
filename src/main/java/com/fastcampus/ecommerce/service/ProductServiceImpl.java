package com.fastcampus.ecommerce.service;

import com.fastcampus.ecommerce.common.errors.ResourceNotFoundException;
import com.fastcampus.ecommerce.entity.Category;
import com.fastcampus.ecommerce.entity.Product;
import com.fastcampus.ecommerce.entity.ProductCategory;
import com.fastcampus.ecommerce.entity.ProductCategory.ProductCategoryId;
import com.fastcampus.ecommerce.model.*;
import com.fastcampus.ecommerce.repository.CategoryRepository;
import com.fastcampus.ecommerce.repository.ProductCategoryRepository;
import com.fastcampus.ecommerce.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductCategoryRepository productCategoryRepository;

    private final String PRODUCT_CACHE_KEY = "products:";
    private final CacheService cacheService;
    private final RateLimitingService rateLimitingService;
    private final ProductIndexService productIndexService;
    private final ProductReindexProducer productReindexProducer;

    @Override
    public List<ProductResponse> findAll() {
        return productRepository.findAll().stream()
                .map(product -> {
                    List<CategoryResponse> productCategories = getProductCategories(product.getProductId());
                    return ProductResponse.fromProductAndCategories(product, productCategories);
                })
                .toList();
    }

    @Override
    public Page<ProductResponse> findByPage(Pageable pageable) {
        return rateLimitingService.executeWithRateLimit("product_listing", () -> productRepository.findByPageable(pageable)
                .map(product -> {
                    List<CategoryResponse> productCategories = getProductCategories(product.getProductId());
                    return ProductResponse.fromProductAndCategories(product, productCategories);
                }));
    }

    @Override
    public Page<ProductResponse> findByNameAndPageable(String name, Pageable pageable) {
        name = "%" + name + "%";
        name = name.toLowerCase();
        return productRepository.findByNamePageable(name, pageable)
                .map(product -> {
                    List<CategoryResponse> productCategories = getProductCategories(product.getProductId());
                    return ProductResponse.fromProductAndCategories(product, productCategories);
                });
    }

    @Override
    public ProductResponse findById(Long productId) {
        String cacheKey = PRODUCT_CACHE_KEY + productId;
        Optional<ProductResponse> cachedProduct = cacheService.get(cacheKey, ProductResponse.class);
        if (cachedProduct.isPresent()) { // check redis cache
            return cachedProduct.get();
        }

        Product existingProduct = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));
        List<CategoryResponse> productCategories = getProductCategories(productId);
        ProductResponse productResponse = ProductResponse.fromProductAndCategories(existingProduct, productCategories);
        cacheService.put(cacheKey, productResponse); // Save redis cache
        return productResponse;
    }

    @Override
    @Transactional
    public ProductResponse create(ProductRequest productRequest) {
        List<Category> categories = getCategoriesByIds(productRequest.getCategoryIds());

        Product product = Product.builder()
                .name(productRequest.getName())
                .description(productRequest.getDescription())
                .price(productRequest.getPrice())
                .stockQuantity(productRequest.getStockQuantity())
                .userId(productRequest.getUser().getUserId())
                .weight(productRequest.getWeight())
                .build();

        Product createdProduct = productRepository.save(product);

        List<ProductCategory> productCategories = categories.stream()
                .map(category -> {
                    ProductCategory productCategory = new ProductCategory();
                    ProductCategoryId productCategoryId = new ProductCategoryId();
                    productCategoryId.setCategoryId(category.getCategoryId());
                    productCategoryId.setProductId(createdProduct.getProductId());
                    productCategory.setId(productCategoryId);
                    return productCategory;
                }).toList();

        productCategoryRepository.saveAll(productCategories);

        List<CategoryResponse> categoryResponseList = categories.stream().map(CategoryResponse::fromCategory)
                .toList();

        String cacheKey = PRODUCT_CACHE_KEY + createdProduct.getProductId();
        ProductResponse productResponse = ProductResponse.fromProductAndCategories(createdProduct, categoryResponseList);
        cacheService.put(cacheKey, productResponse); // Save redis cache

        // Reindex elasticsearch
//        productIndexService.reindexProduct(product);

        // Reindex elasticsearch dengan kafka
        productReindexProducer.publishProductReindex(ProductReindex.builder()
                        .action("REINDEX")
                        .productId(product.getProductId())
                .build());

        return productResponse;
    }

    @Override
    @Transactional
    public ProductResponse update(Long productId, ProductRequest productRequest) {
        Product existingProduct = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        List<Category> categories = getCategoriesByIds(productRequest.getCategoryIds());

        existingProduct.setName(productRequest.getName());
        existingProduct.setDescription(productRequest.getDescription());
        existingProduct.setPrice(productRequest.getPrice());
        existingProduct.setStockQuantity(productRequest.getStockQuantity());
        existingProduct.setWeight(productRequest.getWeight());
        productRepository.save(existingProduct);

        List<ProductCategory> existingProductCategories = productCategoryRepository.findCategoriesByProductId(productId);
        productCategoryRepository.deleteAll(existingProductCategories);

        List<ProductCategory> productCategories = categories.stream()
                .map(category -> {
                    ProductCategory productCategory = new ProductCategory();
                    ProductCategoryId productCategoryId = new ProductCategoryId();
                    productCategoryId.setCategoryId(category.getCategoryId());
                    productCategoryId.setProductId(productId);
                    productCategory.setId(productCategoryId);
                    return productCategory;
                })
                .toList();

        productCategoryRepository.saveAll(productCategories);

        List<CategoryResponse> categoryResponseList = categories.stream().map(CategoryResponse::fromCategory)
                .toList();

        String cacheKey = PRODUCT_CACHE_KEY + productId;
        cacheService.evict(cacheKey); // Delete redis cache

        // Reindex elasticsearch
//        productIndexService.reindexProduct(existingProduct);

        // Reindex elasticsearch dengan kafka
        productReindexProducer.publishProductReindex(ProductReindex.builder()
                .action("REINDEX")
                .productId(existingProduct.getProductId())
                .build());

        return ProductResponse.fromProductAndCategories(existingProduct, categoryResponseList);
    }

    @Override
    @Transactional
    public void delete(Long productId) {
        Product existingProduct = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));
        List<ProductCategory> productCategories = productCategoryRepository.findCategoriesByProductId(productId);

        productCategoryRepository.deleteAll(productCategories);

        // Delete elasticsearch
//        productIndexService.deleteProduct(existingProduct);

        // Reindex elasticsearch dengan kafka
        productReindexProducer.publishProductReindex(ProductReindex.builder()
                .action("DELETE")
                .productId(existingProduct.getProductId())
                .build());

        productRepository.delete(existingProduct);
    }

    @Override
    public PaginatedProductResponse convertProductPage(Page<ProductResponse> productPage) {
        return PaginatedProductResponse.builder()
                .data(productPage.getContent())
                .pageNo(productPage.getNumber())
                .pageSize(productPage.getSize())
                .totalElements(productPage.getTotalElements())
                .totalPages(productPage.getTotalPages())
                .last(productPage.isLast())
                .build();
    }

    @Override
    public Product get(Long id) {
        return productRepository.findById(id)
                .orElse(null);
    }

    private List<Category> getCategoriesByIds(List<Long> categoryIds) {
        return categoryIds.stream()
                .map(categoryId -> categoryRepository.findById(categoryId)
                        .orElseThrow(() -> new ResourceNotFoundException("Category not found for id : " + categoryId)))
                .toList();
    }

    private List<CategoryResponse> getProductCategories(Long productId) {
        List<ProductCategory> productCategories = productCategoryRepository.findCategoriesByProductId(productId);
        List<Long> categoryIds = productCategories.stream()
                .map(productCategory -> productCategory.getId().getCategoryId())
                .toList();
        return categoryRepository.findAllById(categoryIds).stream()
                .map(CategoryResponse::fromCategory)
                .toList();
    }
}

package com.fastcampus.ecommerce.controller;

import com.fastcampus.ecommerce.common.PageUtil;
import com.fastcampus.ecommerce.model.*;
import com.fastcampus.ecommerce.service.CachedProductAutocompleteService;
import com.fastcampus.ecommerce.service.ProductService;
import com.fastcampus.ecommerce.service.SearchService;
import com.fastcampus.ecommerce.service.UserActivityService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("products")
@SecurityRequirement(name = "Bearer")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final SearchService searchService;
    private final UserActivityService userActivityService;
    private final CachedProductAutocompleteService cachedProductAutocompleteService;

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> findProductById(@PathVariable(value = "id") Long productId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserInfo userInfo = (UserInfo) authentication.getPrincipal();

        ProductResponse productResponse = productService.findById(productId);
        if (productResponse.getUserId() != userInfo.getUser().getUserId()) {
            userActivityService.trackProductView(productId, userInfo.getUser().getUserId()); // Track user activity product view
        }
        return ResponseEntity.ok(productResponse);
    }

    @GetMapping("/{id}/similar")
    public ResponseEntity<SearchResponse<ProductResponse>> search(@PathVariable(value = "id") Long productId) {
        SearchResponse<ProductResponse> response = searchService.similarProducts(productId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/search")
    public ResponseEntity<SearchResponse<ProductResponse>> search(@RequestBody ProductSearchRequest request) {
        SearchResponse<ProductResponse> response = searchService.search(request);
        return ResponseEntity.ok(response);
    }

//    @GetMapping("/suggests")
//    public ResponseEntity<List<String>> suggestions(@RequestParam("text") String text) {
//        List<String> suggestions = List.of();
//        if (text.length() > 2) {
//            suggestions = cachedProductAutocompleteService.getAutocomplete(text);
//        }
//        return ResponseEntity.ok(suggestions);
//    }

    // Ubah dari menggunakan suggest berdasarkan prefix ke combined
    @GetMapping("/suggests")
    public ResponseEntity<List<String>> suggestions(@RequestParam("text") String text) {
        List<String> suggestions = List.of();
        if (text.length() > 2) {
            suggestions = cachedProductAutocompleteService.combinedAutocomplete(text);
        }
        return ResponseEntity.ok(suggestions);
    }

    @GetMapping("/suggests/ngram")
    public ResponseEntity<List<String>> ngramSuggestions(@RequestParam("text") String text) {
        List<String> suggestions = List.of();
        if (text.length() > 2) {
            suggestions = cachedProductAutocompleteService.getNgramAutocomplete(text);
        }
        return ResponseEntity.ok(suggestions);
    }

    @GetMapping("/suggests/fuzzy")
    public ResponseEntity<List<String>> fuzzySuggestions(@RequestParam("text") String text) {
        List<String> suggestions = List.of();
        if (text.length() > 2) {
            suggestions = cachedProductAutocompleteService.getFuzzyAutocomplete(text);
        }
        return ResponseEntity.ok(suggestions);
    }

    @GetMapping("/recommendations")
    public ResponseEntity<SearchResponse<ProductResponse>> recommendations(@RequestParam(value = "user_activity", defaultValue = "VIEW") ActivityType activityType) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserInfo userInfo = (UserInfo) authentication.getPrincipal();

        SearchResponse<ProductResponse> response = searchService.userRecommendation(userInfo.getUser().getUserId(), activityType);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<PaginatedProductResponse> getAllProduct(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "product_id,asc") String[] sort,
            @RequestParam(required = false) String name
    ) {
        List<Sort.Order> orders = PageUtil.parseSortOrderRequest(sort);
        Pageable pageable = PageRequest.of(page, size, Sort.by(orders));
        Page<ProductResponse> productResponses;

        if (name != null && !name.isEmpty()) {
            productResponses = productService.findByNameAndPageable(name, pageable);
        } else {
            productResponses = productService.findByPage(pageable);
        }

        return ResponseEntity.ok(productService.convertProductPage(productResponses));
    }

    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(@RequestBody @Valid ProductRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserInfo userInfo = (UserInfo) authentication.getPrincipal();

        request.setUser(userInfo.getUser());
        ProductResponse response = productService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> updateProduct(
            @RequestBody @Valid ProductRequest request,
            @PathVariable(value = "id") Long productId
    ) {
        ProductResponse response = productService.update(productId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable(value = "id") Long productId) {
        productService.delete(productId);
        return ResponseEntity.noContent().build();
    }

}

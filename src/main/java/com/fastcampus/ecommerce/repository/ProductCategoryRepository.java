package com.fastcampus.ecommerce.repository;

import com.fastcampus.ecommerce.entity.ProductCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import static com.fastcampus.ecommerce.entity.ProductCategory.*;

@Repository
public interface ProductCategoryRepository extends JpaRepository<ProductCategory, ProductCategoryId> {
}

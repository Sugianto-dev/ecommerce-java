package com.fastcampus.ecommerce.repository;

import com.fastcampus.ecommerce.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {

    boolean existsByUserId(Long userId);

    Optional<Cart> findByUserId(Long userId);

}

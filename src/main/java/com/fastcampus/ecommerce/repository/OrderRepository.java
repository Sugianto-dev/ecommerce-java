package com.fastcampus.ecommerce.repository;

import com.fastcampus.ecommerce.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUserId(Long userId);
    List<Order> findByStatus(String status);

    @Query(value = """
            SELECT * FROM orders
            WHERE user_id = :userId
            AND order_data BETWEEN :startDate AND :endDate
            """, nativeQuery = true)
    List<Order> findByUserIdAndDateRange(Long userId, LocalDateTime startDate, LocalDateTime endDate);
}

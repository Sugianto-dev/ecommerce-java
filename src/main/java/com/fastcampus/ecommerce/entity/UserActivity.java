package com.fastcampus.ecommerce.entity;

import com.fastcampus.ecommerce.model.ActivityType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_activity")
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserActivity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "product_id")
    private Long productId;

    @Enumerated(EnumType.STRING)
    private ActivityType activityType;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}

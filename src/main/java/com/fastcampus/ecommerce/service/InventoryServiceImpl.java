package com.fastcampus.ecommerce.service;

import com.fastcampus.ecommerce.common.errors.InventoryException;
import com.fastcampus.ecommerce.entity.Product;
import com.fastcampus.ecommerce.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryServiceImpl implements InventoryService {
    private final ProductRepository productRepository;

    @Override
    @Transactional // Wajib untuk locking
    public boolean checkAndLockInventory(Map<Long, Integer> productQuantities) {
        for (Map.Entry<Long, Integer> entry : productQuantities.entrySet()) {
            Product product = productRepository.findByIdWithPessimisticLock(entry.getKey())
                    .orElseThrow(() -> new InventoryException("Product with id " + entry.getKey() + " is not found"));

            if (product.getStockQuantity() < entry.getValue()) {
                return false;
            }
        }
        return true;
    }

    @Override
    @Transactional // Wajib untuk locking
    public void decreaseQuantity(Map<Long, Integer> productQuantities) {
        for (Map.Entry<Long, Integer> entry : productQuantities.entrySet()) {
            Product product = productRepository.findByIdWithPessimisticLock(entry.getKey())
                    .orElseThrow(() -> new InventoryException("Product with id " + entry.getKey() + " is not found"));

            if (product.getStockQuantity() < entry.getValue()) {
                throw new InventoryException("Insufficient inventory for product " + entry.getKey());
            }

            Integer newQuantity = product.getStockQuantity() - entry.getValue();
            product.setStockQuantity(newQuantity);
            productRepository.save(product);
        }
    }

    @Override
    @Transactional // Wajib untuk locking
    public void increaseQuantity(Map<Long, Integer> productQuantities) {
        for (Map.Entry<Long, Integer> entry : productQuantities.entrySet()) {
            Product product = productRepository.findByIdWithPessimisticLock(entry.getKey())
                    .orElseThrow(() -> new InventoryException("Product with id " + entry.getKey() + " is not found"));

            Integer newQuantity = product.getStockQuantity() + entry.getValue();
            product.setStockQuantity(newQuantity);
            productRepository.save(product);
        }
    }
}

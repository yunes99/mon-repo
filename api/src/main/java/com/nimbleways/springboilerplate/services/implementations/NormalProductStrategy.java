package com.nimbleways.springboilerplate.services.implementations;

import java.util.Objects;

import com.nimbleways.springboilerplate.entities.Product;
import com.nimbleways.springboilerplate.repositories.ProductRepository;
import org.springframework.stereotype.Service;

@Service
public class NormalProductStrategy implements ProductStrategy {

    private final ProductRepository productRepository;
    private final NotificationService notificationService;

    public NormalProductStrategy(ProductRepository productRepository, 
                                NotificationService notificationService) {
        this.productRepository = productRepository;
        this.notificationService = notificationService;
    }

    @Override
    public void handleOutOfStock(Product product) {
        int leadTime = product.getLeadTime();
        if (leadTime > 0) {
            notificationService.sendDelayNotification(leadTime, product.getName());
            product.setLeadTime(leadTime);
            productRepository.save(product);
        }
    }
}

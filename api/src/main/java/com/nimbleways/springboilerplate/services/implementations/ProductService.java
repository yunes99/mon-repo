package com.nimbleways.springboilerplate.services.implementations;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.springframework.stereotype.Service;

import com.nimbleways.springboilerplate.entities.Product;
import com.nimbleways.springboilerplate.entities.ProductType;
import com.nimbleways.springboilerplate.repositories.ProductRepository;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final NotificationService notificationService;
    private final NormalProductStrategy normalStrategy;
    private final SeasonalProductStrategy seasonalStrategy;
    private final ExpirableProductStrategy expirableStrategy;
    private Map<ProductType, ProductStrategy> strategies;

    public ProductService(ProductRepository productRepository, 
                         NotificationService notificationService,
                         NormalProductStrategy normalStrategy,
                         SeasonalProductStrategy seasonalStrategy,
                         ExpirableProductStrategy expirableStrategy) {
        this.productRepository = productRepository;
        this.notificationService = notificationService;
        this.normalStrategy = normalStrategy;
        this.seasonalStrategy = seasonalStrategy;
        this.expirableStrategy = expirableStrategy;
    }

    private Map<ProductType, ProductStrategy> getStrategies() {
        if (Objects.isNull(strategies)) {
            strategies = new HashMap<>();
            strategies.put(ProductType.NORMAL, normalStrategy);
            strategies.put(ProductType.SEASONAL, seasonalStrategy);
            strategies.put(ProductType.EXPIRABLE, expirableStrategy);
        }
        return strategies;
    }

    public void processProductOrder(Product product) {
        if (product.getAvailable() > 0) {
            product.setAvailable(product.getAvailable() - 1);
            productRepository.save(product);
        } else {
            ProductStrategy strategy = getStrategies().get(product.getType());
            if (Objects.nonNull(strategy)) {
                strategy.handleOutOfStock(product);
            }
        }
    }

    // Keep for backward compatibility with existing tests
    public void notifyDelay(int leadTime, Product p) {
        p.setLeadTime(leadTime);
        productRepository.save(p);
        notificationService.sendDelayNotification(leadTime, p.getName());
    }

    // Keep for backward compatibility with existing tests
    public void handleSeasonalProduct(Product p) {
        if (LocalDate.now().plusDays(p.getLeadTime()).isAfter(p.getSeasonEndDate())) {
            notificationService.sendOutOfStockNotification(p.getName());
            p.setAvailable(0);
            productRepository.save(p);
        } else if (p.getSeasonStartDate().isAfter(LocalDate.now())) {
            notificationService.sendOutOfStockNotification(p.getName());
            productRepository.save(p);
        } else {
            notifyDelay(p.getLeadTime(), p);
        }
    }

    // Keep for backward compatibility with existing tests
    public void handleExpiredProduct(Product p) {
        if (p.getAvailable() > 0 && p.getExpiryDate().isAfter(LocalDate.now())) {
            p.setAvailable(p.getAvailable() - 1);
            productRepository.save(p);
        } else {
            notificationService.sendExpirationNotification(p.getName(), p.getExpiryDate());
            p.setAvailable(0);
            productRepository.save(p);
        }
    }
}
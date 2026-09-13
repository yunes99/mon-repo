package com.nimbleways.springboilerplate.services.implementations;

import com.nimbleways.springboilerplate.entities.Product;

public interface ProductStrategy {
    void handleOutOfStock(Product product);
}

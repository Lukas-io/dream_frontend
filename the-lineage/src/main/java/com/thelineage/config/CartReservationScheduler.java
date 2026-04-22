package com.thelineage.config;

import com.thelineage.service.CartService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class CartReservationScheduler {

    private final CartService cart;

    public CartReservationScheduler(CartService cart) {
        this.cart = cart;
    }

    @Scheduled(fixedDelayString = "PT1M")
    public void releaseExpired() {
        cart.releaseExpired();
    }
}

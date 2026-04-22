package com.thelineage.service;

import com.thelineage.domain.*;
import com.thelineage.exception.BadRequestException;
import com.thelineage.repository.CartRepository;
import com.thelineage.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class CheckoutServiceImpl implements CheckoutService {

    private final CartRepository carts;
    private final OrderRepository orders;
    private final OrderService orderService;
    private final ListingService listings;
    private final PaymentService payments;
    private final NotificationService notifications;

    public CheckoutServiceImpl(CartRepository carts,
                               OrderRepository orders,
                               OrderService orderService,
                               ListingService listings,
                               PaymentService payments,
                               NotificationService notifications) {
        this.carts = carts;
        this.orders = orders;
        this.orderService = orderService;
        this.listings = listings;
        this.payments = payments;
        this.notifications = notifications;
    }

    @Override
    @Transactional
    public OrderEntity checkout(UUID buyerUserId) {
        Cart cart = carts.findByOwnerId(buyerUserId)
                .orElseThrow(() -> new BadRequestException("No active cart"));
        if (cart.getItems().isEmpty()) {
            throw new BadRequestException("Cart is empty");
        }
        CartItem item = cart.getItems().get(0);
        Listing listing = item.getListing();
        if (listing.getState() == ListingState.AVAILABLE) {
            listings.reserve(listing.getId());
        }
        OrderEntity order = orderService.createFromCart(buyerUserId);
        Payment payment = payments.initiate(order.getId());
        payments.confirm(payment.getId(), "mock-" + UUID.randomUUID());
        listings.markSold(listing.getId(), buyerUserId);
        order.setStatus(OrderStatus.PAID);
        orders.save(order);
        cart.getItems().clear();
        carts.save(cart);
        notifications.notify(order.getBuyer(), NotificationType.ORDER_UPDATE,
                "{\"orderId\":\"" + order.getId() + "\",\"status\":\"PAID\"}");
        notifications.notify(order.getSeller().getUser(), NotificationType.ORDER_UPDATE,
                "{\"orderId\":\"" + order.getId() + "\",\"status\":\"PAID\"}");
        return order;
    }
}

package com.thelineage.service;

import com.thelineage.domain.*;
import com.thelineage.exception.BadRequestException;
import com.thelineage.exception.ConflictException;
import com.thelineage.exception.NotFoundException;
import com.thelineage.repository.CartRepository;
import com.thelineage.repository.OrderRepository;
import com.thelineage.repository.ShippingRecordRepository;
import com.thelineage.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orders;
    private final CartRepository carts;
    private final UserRepository users;
    private final ShippingRecordRepository shipping;
    private final ProvenanceService provenance;

    public OrderServiceImpl(OrderRepository orders, CartRepository carts, UserRepository users,
                            ShippingRecordRepository shipping, ProvenanceService provenance) {
        this.orders = orders;
        this.carts = carts;
        this.users = users;
        this.shipping = shipping;
        this.provenance = provenance;
    }

    @Override
    @Transactional
    public OrderEntity createFromCart(UUID buyerUserId) {
        User buyer = users.findById(buyerUserId)
                .orElseThrow(() -> new NotFoundException("User not found: " + buyerUserId));
        Cart cart = carts.findByOwnerId(buyerUserId)
                .orElseThrow(() -> new BadRequestException("No active cart"));
        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new BadRequestException("Cart is empty");
        }
        CartItem item = cart.getItems().get(0);
        Listing listing = item.getListing();
        OrderEntity order = OrderEntity.builder()
                .buyer(buyer)
                .seller(listing.getSeller())
                .listing(listing)
                .totalAmount(listing.getPrice())
                .currency(listing.getCurrency())
                .status(OrderStatus.CREATED)
                .build();
        return orders.save(order);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderEntity findById(UUID orderId) {
        return orders.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found: " + orderId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderEntity> findByBuyer(UUID buyerUserId) {
        return orders.findAllByBuyerId(buyerUserId);
    }

    @Override
    @Transactional
    public OrderEntity markShipped(UUID orderId, String carrier, String trackingNumber) {
        OrderEntity order = findById(orderId);
        if (order.getStatus() != OrderStatus.PAID) {
            throw new ConflictException("Order must be PAID to be shipped");
        }
        order.setStatus(OrderStatus.SHIPPED);
        OrderEntity saved = orders.save(order);
        ShippingRecord record = shipping.findByOrderId(orderId).orElseGet(() ->
                ShippingRecord.builder().order(order).build());
        record.setCarrier(carrier);
        record.setTrackingNumber(trackingNumber);
        record.setStatus(ShippingStatus.IN_TRANSIT);
        record.setShippedAt(Instant.now());
        shipping.save(record);
        provenance.append(order.getListing().getShoe().getId(),
                order.getSeller().getUser().getId(),
                ProvenanceEventType.SHIPPED,
                "{\"orderId\":\"" + orderId + "\",\"carrier\":\"" + carrier + "\"}");
        return saved;
    }

    @Override
    @Transactional
    public OrderEntity markDelivered(UUID orderId) {
        OrderEntity order = findById(orderId);
        if (order.getStatus() != OrderStatus.SHIPPED) {
            throw new ConflictException("Order must be SHIPPED to be delivered");
        }
        order.setStatus(OrderStatus.DELIVERED);
        shipping.findByOrderId(orderId).ifPresent(rec -> {
            rec.setStatus(ShippingStatus.DELIVERED);
            rec.setDeliveredAt(Instant.now());
            shipping.save(rec);
        });
        return orders.save(order);
    }

    @Override
    @Transactional
    public OrderEntity complete(UUID orderId) {
        OrderEntity order = findById(orderId);
        if (order.getStatus() != OrderStatus.DELIVERED && order.getStatus() != OrderStatus.PAID) {
            throw new ConflictException("Order cannot be completed from current status: " + order.getStatus());
        }
        order.setStatus(OrderStatus.COMPLETED);
        order.setCompletedAt(Instant.now());
        OrderEntity saved = orders.save(order);
        provenance.append(order.getListing().getShoe().getId(),
                order.getBuyer().getId(),
                ProvenanceEventType.RECEIVED,
                "{\"orderId\":\"" + orderId + "\"}");
        return saved;
    }
}

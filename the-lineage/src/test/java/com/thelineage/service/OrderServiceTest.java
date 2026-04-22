package com.thelineage.service;

import com.thelineage.domain.*;
import com.thelineage.exception.BadRequestException;
import com.thelineage.exception.ConflictException;
import com.thelineage.exception.NotFoundException;
import com.thelineage.repository.CartRepository;
import com.thelineage.repository.OrderRepository;
import com.thelineage.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock private OrderRepository orders;
    @Mock private CartRepository carts;
    @Mock private UserRepository users;
    @InjectMocks private OrderServiceImpl service;

    @Test
    void createFromCart_withItem_createsOrderInCreatedStatus() {
        UUID buyerId = UUID.randomUUID();
        User buyer = User.builder().id(buyerId).build();
        SellerProfile seller = SellerProfile.builder().id(UUID.randomUUID()).build();
        Listing listing = Listing.builder().id(UUID.randomUUID()).price(new BigDecimal("500")).currency("USD").seller(seller).build();
        Cart cart = Cart.builder().owner(buyer).build();
        cart.getItems().add(CartItem.builder().cart(cart).listing(listing).build());
        when(users.findById(buyerId)).thenReturn(Optional.of(buyer));
        when(carts.findByOwnerId(buyerId)).thenReturn(Optional.of(cart));
        when(orders.save(any())).thenAnswer(inv -> inv.getArgument(0));
        OrderEntity order = service.createFromCart(buyerId);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CREATED);
        assertThat(order.getTotalAmount()).isEqualByComparingTo("500");
    }

    @Test
    void createFromCart_emptyCart_throwsBadRequest() {
        UUID buyerId = UUID.randomUUID();
        User buyer = User.builder().id(buyerId).build();
        when(users.findById(buyerId)).thenReturn(Optional.of(buyer));
        when(carts.findByOwnerId(buyerId)).thenReturn(Optional.of(Cart.builder().owner(buyer).build()));
        assertThatThrownBy(() -> service.createFromCart(buyerId)).isInstanceOf(BadRequestException.class);
    }

    @Test
    void findById_whenMissing_throwsNotFound() {
        UUID id = UUID.randomUUID();
        when(orders.findById(id)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.findById(id)).isInstanceOf(NotFoundException.class);
    }

    @Test
    void findByBuyer_delegatesToRepo() {
        UUID buyerId = UUID.randomUUID();
        when(orders.findAllByBuyerId(buyerId)).thenReturn(List.of(new OrderEntity()));
        assertThat(service.findByBuyer(buyerId)).hasSize(1);
    }

    @Test
    void markShipped_fromPaid_succeeds() {
        UUID id = UUID.randomUUID();
        OrderEntity order = OrderEntity.builder().id(id).status(OrderStatus.PAID).build();
        when(orders.findById(id)).thenReturn(Optional.of(order));
        when(orders.save(any())).thenAnswer(inv -> inv.getArgument(0));
        assertThat(service.markShipped(id).getStatus()).isEqualTo(OrderStatus.SHIPPED);
    }

    @Test
    void markShipped_fromCreated_throwsConflict() {
        UUID id = UUID.randomUUID();
        when(orders.findById(id)).thenReturn(Optional.of(OrderEntity.builder().id(id).status(OrderStatus.CREATED).build()));
        assertThatThrownBy(() -> service.markShipped(id)).isInstanceOf(ConflictException.class);
    }

    @Test
    void complete_fromDelivered_succeeds() {
        UUID id = UUID.randomUUID();
        OrderEntity order = OrderEntity.builder().id(id).status(OrderStatus.DELIVERED).build();
        when(orders.findById(id)).thenReturn(Optional.of(order));
        when(orders.save(any())).thenAnswer(inv -> inv.getArgument(0));
        OrderEntity result = service.complete(id);
        assertThat(result.getStatus()).isEqualTo(OrderStatus.COMPLETED);
        assertThat(result.getCompletedAt()).isNotNull();
    }
}

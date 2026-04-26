package com.thelineage.mapper;

import com.thelineage.domain.*;
import com.thelineage.dto.cart.CartDto;
import com.thelineage.dto.cart.CartItemDto;
import com.thelineage.dto.comment.CommentDto;
import com.thelineage.dto.dispute.DisputeDto;
import com.thelineage.dto.order.OrderDto;
import com.thelineage.dto.review.ReviewDto;
import com.thelineage.dto.shoe.ShoeDto;
import org.springframework.stereotype.Component;

@Component
public class DomainMappers {

    public CartDto toDto(Cart c) {
        return new CartDto(
                c.getId(),
                c.getOwner().getId(),
                c.getItems().stream().map(this::toDto).toList()
        );
    }

    public CartItemDto toDto(CartItem i) {
        return new CartItemDto(
                i.getId(),
                i.getListing().getId(),
                i.getListing().getPrice(),
                i.getListing().getCurrency(),
                i.getReservedAt(),
                i.getExpiresAt()
        );
    }

    public OrderDto toDto(OrderEntity o) {
        return new OrderDto(
                o.getId(),
                o.getListing().getId(),
                o.getBuyer().getId(),
                o.getSeller().getId(),
                o.getTotalAmount(),
                o.getCurrency(),
                o.getStatus(),
                o.getCreatedAt(),
                o.getCompletedAt()
        );
    }

    public CommentDto toDto(Comment c) {
        return new CommentDto(
                c.getId(),
                c.getListing().getId(),
                c.getParent() != null ? c.getParent().getId() : null,
                c.getAuthor().getId(),
                c.getBody(),
                c.isFlagged(),
                c.getCreatedAt()
        );
    }

    public ReviewDto toDto(Review r) {
        return new ReviewDto(
                r.getId(),
                r.getOrder().getId(),
                r.getAuthor().getId(),
                r.getSeller().getId(),
                r.getAccuracyScore(),
                r.getConditionScore(),
                r.getShippingScore(),
                r.getBody(),
                r.getCreatedAt()
        );
    }

    public DisputeDto toDto(Dispute d) {
        return new DisputeDto(
                d.getId(),
                d.getOrder().getId(),
                d.getRaisedBy().getId(),
                d.getResolver() != null ? d.getResolver().getId() : null,
                d.getStatus(),
                d.getReason(),
                d.getResolutionNote(),
                d.getOpenedAt(),
                d.getResolvedAt()
        );
    }

    public ShoeDto toDto(Shoe s) {
        return new ShoeDto(
                s.getId(),
                s.getPassportId(),
                s.getBrand(),
                s.getModel(),
                s.getColorway(),
                s.getEraYear(),
                s.getConditionGrade(),
                s.getRarityScore(),
                s.isAuthenticated(),
                s.getAuthenticatedAt()
        );
    }
}

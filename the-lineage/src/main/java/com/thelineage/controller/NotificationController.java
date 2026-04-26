package com.thelineage.controller;

import com.thelineage.domain.Notification;
import com.thelineage.repository.NotificationRepository;
import com.thelineage.security.LineageUserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/notifications")
@Tag(name = "Notifications", description = "In-app notifications for the authenticated user (delivery is mocked)")
public class NotificationController {

    private final NotificationRepository notifications;

    public NotificationController(NotificationRepository notifications) {
        this.notifications = notifications;
    }

    public record NotificationDto(UUID id, String type, String payloadJson, boolean read, Instant createdAt) {
        static NotificationDto from(Notification n) {
            return new NotificationDto(n.getId(), n.getType().name(), n.getPayloadJson(), n.isRead(), n.getCreatedAt());
        }
    }

    @GetMapping("/me")
    @Operation(summary = "List notifications for the authenticated user (newest first)")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List of notifications."),
            @ApiResponse(responseCode = "401", description = "Missing or invalid bearer token.", content = @Content)
    })
    public List<NotificationDto> mine(@AuthenticationPrincipal LineageUserPrincipal principal) {
        return notifications.findAllByRecipientIdOrderByCreatedAtDesc(principal.id())
                .stream().map(NotificationDto::from).toList();
    }
}

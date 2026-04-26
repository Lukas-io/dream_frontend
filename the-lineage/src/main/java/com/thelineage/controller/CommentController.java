package com.thelineage.controller;

import com.thelineage.domain.Comment;
import com.thelineage.dto.comment.CommentDto;
import com.thelineage.dto.comment.PostCommentRequest;
import com.thelineage.mapper.DomainMappers;
import com.thelineage.security.LineageUserPrincipal;
import com.thelineage.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@Tag(name = "Comments", description = "Threaded comments on listings")
public class CommentController {

    private final CommentService comments;
    private final DomainMappers mappers;

    public CommentController(CommentService comments, DomainMappers mappers) {
        this.comments = comments;
        this.mappers = mappers;
    }

    @GetMapping("/listings/{id}/comments")
    @Operation(summary = "List the comment thread for a listing (public)")
    @SecurityRequirements
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Comment thread returned.")})
    public List<CommentDto> thread(@PathVariable UUID id) {
        return comments.threadFor(id).stream().map(mappers::toDto).toList();
    }

    @PostMapping("/listings/{id}/comments")
    @Operation(summary = "Post a top-level comment on a listing")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Comment posted."),
            @ApiResponse(responseCode = "400", description = "Validation failed or body blank.",
                    content = @Content),
            @ApiResponse(responseCode = "401", description = "Missing or invalid bearer token.", content = @Content),
            @ApiResponse(responseCode = "404", description = "Listing not found.", content = @Content)
    })
    public CommentDto post(@AuthenticationPrincipal LineageUserPrincipal principal,
                           @PathVariable UUID id,
                           @Valid @RequestBody PostCommentRequest body) {
        Comment c = comments.post(id, principal.id(), body.body());
        return mappers.toDto(c);
    }

    @PostMapping("/comments/{parentId}/replies")
    @Operation(summary = "Reply to an existing comment")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Reply posted."),
            @ApiResponse(responseCode = "400", description = "Validation failed or body blank.",
                    content = @Content),
            @ApiResponse(responseCode = "401", description = "Missing or invalid bearer token.", content = @Content),
            @ApiResponse(responseCode = "404", description = "Parent comment not found.", content = @Content)
    })
    public CommentDto reply(@AuthenticationPrincipal LineageUserPrincipal principal,
                            @PathVariable UUID parentId,
                            @Valid @RequestBody PostCommentRequest body) {
        Comment c = comments.reply(parentId, principal.id(), body.body());
        return mappers.toDto(c);
    }

    @PostMapping("/comments/{commentId}/flag")
    @Operation(summary = "Flag a comment for moderator review")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Comment flagged."),
            @ApiResponse(responseCode = "401", description = "Missing or invalid bearer token.", content = @Content),
            @ApiResponse(responseCode = "404", description = "Comment not found.", content = @Content)
    })
    public CommentDto flag(@AuthenticationPrincipal LineageUserPrincipal principal,
                           @PathVariable UUID commentId) {
        Comment c = comments.flag(commentId, principal.id());
        return mappers.toDto(c);
    }
}

package com.thelineage.controller;

import com.thelineage.domain.Comment;
import com.thelineage.dto.comment.CommentDto;
import com.thelineage.dto.comment.PostCommentRequest;
import com.thelineage.mapper.DomainMappers;
import com.thelineage.security.LineageUserPrincipal;
import com.thelineage.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
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
    public List<CommentDto> thread(@PathVariable UUID id) {
        return comments.threadFor(id).stream().map(mappers::toDto).toList();
    }

    @PostMapping("/listings/{id}/comments")
    @Operation(summary = "Post a top-level comment on a listing")
    public CommentDto post(@AuthenticationPrincipal LineageUserPrincipal principal,
                           @PathVariable UUID id,
                           @Valid @RequestBody PostCommentRequest body) {
        Comment c = comments.post(id, principal.id(), body.body());
        return mappers.toDto(c);
    }

    @PostMapping("/comments/{parentId}/replies")
    @Operation(summary = "Reply to an existing comment")
    public CommentDto reply(@AuthenticationPrincipal LineageUserPrincipal principal,
                            @PathVariable UUID parentId,
                            @Valid @RequestBody PostCommentRequest body) {
        Comment c = comments.reply(parentId, principal.id(), body.body());
        return mappers.toDto(c);
    }

    @PostMapping("/comments/{commentId}/flag")
    @Operation(summary = "Flag a comment for moderator review")
    public CommentDto flag(@AuthenticationPrincipal LineageUserPrincipal principal,
                           @PathVariable UUID commentId) {
        Comment c = comments.flag(commentId, principal.id());
        return mappers.toDto(c);
    }
}

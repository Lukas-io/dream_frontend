package com.thelineage.service;

import com.thelineage.domain.Comment;

import java.util.List;
import java.util.UUID;

public interface CommentService {
    Comment post(UUID listingId, UUID authorUserId, String body);
    Comment reply(UUID parentCommentId, UUID authorUserId, String body);
    List<Comment> threadFor(UUID listingId);
    Comment flag(UUID commentId, UUID flaggerUserId);
}

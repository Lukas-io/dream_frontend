package com.thelineage.service;

import com.thelineage.domain.Comment;
import com.thelineage.domain.Listing;
import com.thelineage.domain.User;
import com.thelineage.exception.BadRequestException;
import com.thelineage.exception.NotFoundException;
import com.thelineage.repository.CommentRepository;
import com.thelineage.repository.ListingRepository;
import com.thelineage.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class CommentServiceImpl implements CommentService {

    private final CommentRepository comments;
    private final ListingRepository listings;
    private final UserRepository users;

    public CommentServiceImpl(CommentRepository comments, ListingRepository listings, UserRepository users) {
        this.comments = comments;
        this.listings = listings;
        this.users = users;
    }

    @Override
    @Transactional
    public Comment post(UUID listingId, UUID authorUserId, String body) {
        if (body == null || body.isBlank()) {
            throw new BadRequestException("Comment body cannot be empty");
        }
        Listing listing = listings.findById(listingId)
                .orElseThrow(() -> new NotFoundException("Listing not found: " + listingId));
        User author = users.findById(authorUserId)
                .orElseThrow(() -> new NotFoundException("User not found: " + authorUserId));
        return comments.save(Comment.builder()
                .listing(listing)
                .author(author)
                .body(body)
                .flagged(false)
                .build());
    }

    @Override
    @Transactional
    public Comment reply(UUID parentCommentId, UUID authorUserId, String body) {
        if (body == null || body.isBlank()) {
            throw new BadRequestException("Reply body cannot be empty");
        }
        Comment parent = comments.findById(parentCommentId)
                .orElseThrow(() -> new NotFoundException("Parent comment not found: " + parentCommentId));
        User author = users.findById(authorUserId)
                .orElseThrow(() -> new NotFoundException("User not found: " + authorUserId));
        return comments.save(Comment.builder()
                .listing(parent.getListing())
                .parent(parent)
                .author(author)
                .body(body)
                .flagged(false)
                .build());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Comment> threadFor(UUID listingId) {
        return comments.findAllByListingIdOrderByCreatedAtAsc(listingId);
    }

    @Override
    @Transactional
    public Comment flag(UUID commentId, UUID flaggerUserId) {
        Comment comment = comments.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Comment not found: " + commentId));
        users.findById(flaggerUserId)
                .orElseThrow(() -> new NotFoundException("User not found: " + flaggerUserId));
        comment.setFlagged(true);
        return comments.save(comment);
    }
}

package com.thelineage.repository;

import com.thelineage.domain.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CommentRepository extends JpaRepository<Comment, UUID> {
    List<Comment> findAllByListingIdOrderByCreatedAtAsc(UUID listingId);
}

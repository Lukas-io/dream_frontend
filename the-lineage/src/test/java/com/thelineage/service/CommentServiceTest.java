package com.thelineage.service;

import com.thelineage.domain.Comment;
import com.thelineage.domain.Listing;
import com.thelineage.domain.User;
import com.thelineage.exception.BadRequestException;
import com.thelineage.repository.CommentRepository;
import com.thelineage.repository.ListingRepository;
import com.thelineage.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock private CommentRepository comments;
    @Mock private ListingRepository listings;
    @Mock private UserRepository users;
    @InjectMocks private CommentServiceImpl service;

    @Test
    void post_withValidBody_persistsComment() {
        UUID listingId = UUID.randomUUID();
        UUID authorId = UUID.randomUUID();
        Listing listing = Listing.builder().id(listingId).build();
        User author = User.builder().id(authorId).build();
        when(listings.findById(listingId)).thenReturn(Optional.of(listing));
        when(users.findById(authorId)).thenReturn(Optional.of(author));
        when(comments.save(any())).thenAnswer(inv -> inv.getArgument(0));
        Comment c = service.post(listingId, authorId, "Nice pair");
        assertThat(c.getBody()).isEqualTo("Nice pair");
        assertThat(c.getListing()).isSameAs(listing);
    }

    @Test
    void post_withBlankBody_throwsBadRequest() {
        assertThatThrownBy(() -> service.post(UUID.randomUUID(), UUID.randomUUID(), "  "))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void reply_setsParentAndInheritsListing() {
        UUID parentId = UUID.randomUUID();
        UUID authorId = UUID.randomUUID();
        Listing listing = Listing.builder().id(UUID.randomUUID()).build();
        Comment parent = Comment.builder().id(parentId).listing(listing).build();
        User author = User.builder().id(authorId).build();
        when(comments.findById(parentId)).thenReturn(Optional.of(parent));
        when(users.findById(authorId)).thenReturn(Optional.of(author));
        when(comments.save(any())).thenAnswer(inv -> inv.getArgument(0));
        Comment reply = service.reply(parentId, authorId, "Agreed");
        assertThat(reply.getParent()).isSameAs(parent);
        assertThat(reply.getListing()).isSameAs(listing);
    }

    @Test
    void threadFor_delegatesToRepo() {
        UUID listingId = UUID.randomUUID();
        when(comments.findAllByListingIdOrderByCreatedAtAsc(listingId)).thenReturn(List.of(new Comment()));
        assertThat(service.threadFor(listingId)).hasSize(1);
    }

    @Test
    void flag_marksFlaggedTrue() {
        UUID commentId = UUID.randomUUID();
        UUID flaggerId = UUID.randomUUID();
        Comment c = Comment.builder().id(commentId).flagged(false).build();
        when(comments.findById(commentId)).thenReturn(Optional.of(c));
        when(users.findById(flaggerId)).thenReturn(Optional.of(User.builder().id(flaggerId).build()));
        when(comments.save(any())).thenAnswer(inv -> inv.getArgument(0));
        assertThat(service.flag(commentId, flaggerId).isFlagged()).isTrue();
    }
}

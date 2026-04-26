package com.thelineage.service;

import com.thelineage.domain.*;
import com.thelineage.exception.NotFoundException;
import com.thelineage.repository.ProvenanceRecordRepository;
import com.thelineage.repository.ShoeRepository;
import com.thelineage.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProvenanceServiceTest {

    @Mock private ProvenanceRecordRepository records;
    @Mock private ShoeRepository shoes;
    @Mock private UserRepository users;
    @InjectMocks private ProvenanceServiceImpl service;

    @Test
    void append_withValidShoeAndUser_savesRecord() {
        UUID shoeId = UUID.randomUUID();
        UUID actorId = UUID.randomUUID();
        Shoe shoe = Shoe.builder().id(shoeId).build();
        User actor = User.builder().id(actorId).build();
        when(shoes.findById(shoeId)).thenReturn(Optional.of(shoe));
        when(users.findById(actorId)).thenReturn(Optional.of(actor));
        when(records.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ProvenanceRecord result = service.append(shoeId, actorId, ProvenanceEventType.LISTED, "{}");

        assertThat(result.getShoe()).isSameAs(shoe);
        assertThat(result.getActor()).isSameAs(actor);
        assertThat(result.getEventType()).isEqualTo(ProvenanceEventType.LISTED);
    }

    @Test
    void append_whenShoeMissing_throwsNotFound() {
        UUID shoeId = UUID.randomUUID();
        when(shoes.findById(shoeId)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.append(shoeId, UUID.randomUUID(), ProvenanceEventType.LISTED, "{}"))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void chainFor_returnsRepositoryResults() {
        UUID shoeId = UUID.randomUUID();
        ProvenanceRecord r1 = ProvenanceRecord.builder().id(UUID.randomUUID()).build();
        when(records.findByShoeIdOrderByOccurredAtAsc(shoeId)).thenReturn(List.of(r1));
        List<ProvenanceRecord> chain = service.chainFor(shoeId);
        assertThat(chain).hasSize(1);
    }

    @Test
    void serviceInterface_hasNoUpdateOrDeleteMethods() {
        List<String> illegal = Arrays.stream(ProvenanceService.class.getMethods())
                .map(Method::getName)
                .filter(n -> n.startsWith("update") || n.startsWith("delete") || n.startsWith("remove"))
                .toList();
        assertThat(illegal).isEmpty();
    }
}

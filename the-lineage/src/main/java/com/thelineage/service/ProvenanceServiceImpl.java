package com.thelineage.service;

import com.thelineage.domain.ProvenanceEventType;
import com.thelineage.domain.ProvenanceRecord;
import com.thelineage.domain.Shoe;
import com.thelineage.domain.User;
import com.thelineage.exception.NotFoundException;
import com.thelineage.repository.ProvenanceRecordRepository;
import com.thelineage.repository.ShoeRepository;
import com.thelineage.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ProvenanceServiceImpl implements ProvenanceService {

    private final ProvenanceRecordRepository records;
    private final ShoeRepository shoes;
    private final UserRepository users;

    public ProvenanceServiceImpl(ProvenanceRecordRepository records, ShoeRepository shoes, UserRepository users) {
        this.records = records;
        this.shoes = shoes;
        this.users = users;
    }

    @Override
    @Transactional
    public ProvenanceRecord append(UUID shoeId, UUID actorUserId, ProvenanceEventType eventType, String payloadJson) {
        Shoe shoe = shoes.findById(shoeId).orElseThrow(() -> new NotFoundException("Shoe not found: " + shoeId));
        User actor = users.findById(actorUserId).orElseThrow(() -> new NotFoundException("User not found: " + actorUserId));
        ProvenanceRecord record = ProvenanceRecord.builder()
                .shoe(shoe)
                .actor(actor)
                .eventType(eventType)
                .payloadJson(payloadJson)
                .build();
        return records.save(record);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProvenanceRecord> chainFor(UUID shoeId) {
        return records.findByShoeIdOrderByOccurredAtAsc(shoeId);
    }
}

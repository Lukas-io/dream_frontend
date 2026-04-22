package com.thelineage.service;

import com.thelineage.domain.Notification;
import com.thelineage.domain.NotificationType;
import com.thelineage.domain.User;
import com.thelineage.repository.NotificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository repo;

    public NotificationServiceImpl(NotificationRepository repo) {
        this.repo = repo;
    }

    @Override
    @Transactional
    public void notify(User recipient, NotificationType type, String payloadJson) {
        repo.save(Notification.builder()
                .recipient(recipient)
                .type(type)
                .payloadJson(payloadJson)
                .read(false)
                .build());
    }

    @Override
    @Transactional
    public void notifyAll(List<User> recipients, NotificationType type, String payloadJson) {
        recipients.forEach(r -> notify(r, type, payloadJson));
    }
}

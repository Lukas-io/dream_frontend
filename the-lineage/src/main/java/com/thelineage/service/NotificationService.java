package com.thelineage.service;

import com.thelineage.domain.NotificationType;
import com.thelineage.domain.User;

import java.util.List;

public interface NotificationService {
    void notify(User recipient, NotificationType type, String payloadJson);
    void notifyAll(List<User> recipients, NotificationType type, String payloadJson);
}

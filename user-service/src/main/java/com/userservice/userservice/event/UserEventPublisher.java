package com.userservice.userservice.event;

import com.cyberlearnix.shared.dto.UserDTO;
import com.cyberlearnix.shared.dto.event.UserCreatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserEventPublisher {
    private final ApplicationEventPublisher eventPublisher;

    public void publishUserCreatedEvent(UserDTO userDTO) {
        eventPublisher.publishEvent(new UserCreatedEvent(this, userDTO));
    }
}

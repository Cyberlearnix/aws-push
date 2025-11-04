package com.userservice.userservice.event;

import com.userservice.userservice.entity.UserEntity;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class UserCreatedEvent extends ApplicationEvent {
    private final UserEntity user;

    public UserCreatedEvent(Object source, UserEntity user) {
        super(source);
        this.user = user;
    }
}

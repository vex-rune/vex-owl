package com.vex.owl.user.user.auth.app.auth.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class UserRegistrationEvent extends ApplicationEvent {

    private final String userId;
    private final String email;
    private final String nickname;

    public UserRegistrationEvent(Object source, String userId, String email, String nickname) {
        super(source);
        this.userId = userId;
        this.email = email;
        this.nickname = nickname;
    }
}
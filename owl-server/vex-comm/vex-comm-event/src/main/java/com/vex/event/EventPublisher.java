package com.vex.event;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class EventPublisher {

    private final ApplicationEventPublisher springPublisher;
    private final ObjectProvider<CurrentUserResolver> currentUserResolver;

    public <T extends Serializable> void publish(String eventType, T payload) {
        Optional<CurrentUserResolver> opt = currentUserResolver.stream().findFirst();

        EventMetadata metadata;

        if (opt.isPresent()) {
            CurrentUserResolver resolver = opt.get();
            metadata = EventMetadata.of(eventType,
                    resolver.resolveCurrentUser().orElse(CurrentUser.anonymous()),
                    resolver.resolveCurrentTrace().orElse(CurrentTrace.anonymous())
            );
        } else {
            metadata = EventMetadata.of(eventType,
                    CurrentUser.anonymous(),
                    CurrentTrace.anonymous()
            );
        }

        springPublisher.publishEvent(Event.of(metadata, payload));
    }
}
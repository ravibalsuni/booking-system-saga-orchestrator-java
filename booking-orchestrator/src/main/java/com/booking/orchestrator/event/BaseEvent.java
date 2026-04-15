package com.booking.orchestrator.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public abstract class BaseEvent {
    private String traceId;
    private String eventType;
    private Instant timestamp;
}

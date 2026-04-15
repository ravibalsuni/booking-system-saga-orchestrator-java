package com.booking.orchestrator.entity;

public enum SagaState {
    STARTED,
    RESERVATION_CREATED,
    PAYMENT_COMPLETED,
    CONFIRMED,
    FAILED,
    COMPENSATING,
    COMPENSATED
}
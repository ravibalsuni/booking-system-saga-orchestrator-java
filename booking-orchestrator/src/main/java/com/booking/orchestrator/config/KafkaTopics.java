package com.booking.orchestrator.config;

public final class KafkaTopics {

    private KafkaTopics() {
    }

    // Command topics
    public static final String RESERVATION_COMMANDS = "reservation-commands";
    public static final String PAYMENT_COMMANDS = "payment-commands";
    public static final String CONFIRMATION_COMMANDS = "confirmation-commands";

    // Reply topics
    public static final String RESERVATION_REPLIES = "reservation-replies";
    public static final String PAYMENT_REPLIES = "payment-replies";
    public static final String CONFIRMATION_REPLIES = "confirmation-replies";

    // Dead Letter Topics
    public static final String RESERVATION_COMMANDS_DLT = "reservation-commands-dlt";
    public static final String PAYMENT_COMMANDS_DLT = "payment-commands-dlt";
    public static final String CONFIRMATION_COMMANDS_DLT = "confirmation-commands-dlt";

    public static final String RESERVATION_REPLIES_DLT = "reservation-replies-dlt";
    public static final String PAYMENT_REPLIES_DLT = "payment-replies-dlt";
    public static final String CONFIRMATION_REPLIES_DLT = "confirmation-replies-dlt";
}

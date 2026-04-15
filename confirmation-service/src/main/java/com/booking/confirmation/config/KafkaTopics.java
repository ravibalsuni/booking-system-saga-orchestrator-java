package com.booking.confirmation.config;

public final class KafkaTopics {

    private KafkaTopics() {
    }

    // Command topics
    public static final String CONFIRMATION_COMMANDS = "confirmation-commands";

    // Reply topics
    public static final String CONFIRMATION_REPLIES = "confirmation-replies";

    // Dead Letter Topics
    public static final String CONFIRMATION_COMMANDS_DLT = "confirmation-commands-dlt";
    public static final String CONFIRMATION_REPLIES_DLT = "confirmation-replies-dlt";
}

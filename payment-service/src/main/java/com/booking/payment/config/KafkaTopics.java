package com.booking.payment.config;

public final class KafkaTopics {

    private KafkaTopics() {
    }

    // Command topics
    public static final String PAYMENT_COMMANDS = "payment-commands";

    // Reply topics
    public static final String PAYMENT_REPLIES = "payment-replies";

    // Dead Letter Topics
    public static final String PAYMENT_COMMANDS_DLT = "payment-commands-dlt";
    public static final String PAYMENT_REPLIES_DLT = "payment-replies-dlt";
}

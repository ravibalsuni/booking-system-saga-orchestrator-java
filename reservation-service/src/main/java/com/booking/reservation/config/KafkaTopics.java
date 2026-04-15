package com.booking.reservation.config;

public final class KafkaTopics {

    private KafkaTopics() {
    }

    // Command topics
    public static final String RESERVATION_COMMANDS = "reservation-commands";

    // Reply topics
    public static final String RESERVATION_REPLIES = "reservation-replies";

    // Dead Letter Topics
    public static final String RESERVATION_COMMANDS_DLT = "reservation-commands-dlt";
    public static final String RESERVATION_REPLIES_DLT = "reservation-replies-dlt";
}

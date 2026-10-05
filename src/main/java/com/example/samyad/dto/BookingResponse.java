package com.example.samyad.dto;

public class BookingResponse {

    private Long id;
    private Long eventId;
    private String eventName;
    private int quantity;
    private int remainingTickets;

    public BookingResponse(
            Long id,
            Long eventId,
            String eventName,
            int quantity,
            int remainingTickets) {

        this.id = id;
        this.eventId = eventId;
        this.eventName = eventName;
        this.quantity = quantity;
        this.remainingTickets = remainingTickets;
    }

    public Long getId() {
        return id;
    }

    public Long getEventId() {
        return eventId;
    }

    public String getEventName() {
        return eventName;
    }

    public int getQuantity() {
        return quantity;
    }

    public int getRemainingTickets() {
        return remainingTickets;
    }
}
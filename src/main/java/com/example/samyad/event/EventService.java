package com.example.samyad.event;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class EventService {

    private final EventRepository eventRepository;

    public EventService(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    public Event createEvent(String name, int totalTickets) {

        Event event = new Event();

        event.setName(name);
        event.setTotalTickets(totalTickets);
        event.setAvailableTickets(totalTickets);

        return eventRepository.save(event);
    }

    @Cacheable(value = "events", key = "#id")
    public Event getEvent(Long id) {

        System.out.println("DATABASE HIT - Loading event " + id);

        return eventRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Event not found"));
    }
}
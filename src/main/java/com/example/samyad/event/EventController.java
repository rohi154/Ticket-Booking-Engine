package com.example.samyad.event;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/events")
public class EventController {

    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @GetMapping("/test-auth")
    public String testAuthentication(Authentication authentication) {
        return "Authenticated as: " + authentication.getName();
    }

    @PostMapping
    public Event createEvent(
            @RequestParam String name,
            @RequestParam int totalTickets) {

        return eventService.createEvent(name, totalTickets);
    }

    @GetMapping("/{id}")
    public Event getEvent(@PathVariable Long id) {
        return eventService.getEvent(id);
    }
}
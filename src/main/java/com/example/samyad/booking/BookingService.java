package com.example.samyad.booking;

import com.example.samyad.dto.BookingRequest;
import com.example.samyad.event.Event;
import com.example.samyad.event.EventRepository;
import com.example.samyad.user.User;
import com.example.samyad.user.UserRepository;

import org.springframework.dao.CannotAcquireLockException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.retry.annotation.Recover;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    public BookingService(
            BookingRepository bookingRepository,
            EventRepository eventRepository,
            UserRepository userRepository) {

        this.bookingRepository = bookingRepository;
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
    }

    @Retryable(
            retryFor = CannotAcquireLockException.class,
            maxAttempts = 3,
            backoff = @Backoff(delay = 50)
    )
    @Transactional
    public Booking createBooking(
            BookingRequest request,
            String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        Event event = eventRepository.findById(request.getEventId())
                .orElseThrow(() ->
                        new RuntimeException("Event not found"));

        if (event.getAvailableTickets() < request.getQuantity()) {
            throw new InsufficientTicketsException(
                    "Not enough tickets"
            );
        }

        event.setAvailableTickets(
                event.getAvailableTickets() - request.getQuantity()
        );

        eventRepository.save(event);

        Booking booking = new Booking();

        booking.setUser(user);
        booking.setEvent(event);
        booking.setQuantity(request.getQuantity());

        return bookingRepository.save(booking);
    }
    @Recover
    public Booking recover(
        CannotAcquireLockException exception,
        BookingRequest request,
        String email) {

    throw new BookingConflictException(
            "The event is currently under heavy load. Please try again."
    );
    }
}
package com.example.samyad.booking;

import com.example.samyad.dto.BookingRequest;
import com.example.samyad.dto.BookingResponse;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    public BookingResponse createBooking(
            @Valid @RequestBody BookingRequest request,
            Authentication authentication) {

        String email = authentication.getName();

        Booking booking = bookingService.createBooking(
                request,
                email
        );

        return new BookingResponse(
                booking.getId(),
                booking.getEvent().getId(),
                booking.getEvent().getName(),
                booking.getQuantity(),
                booking.getEvent().getAvailableTickets()
        );
    }
}
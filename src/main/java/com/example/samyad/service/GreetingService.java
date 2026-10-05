package com.example.samyad.service;

import org.springframework.stereotype.Service;

@Service
public class GreetingService {

    public String getGreeting() {
        return "FlashTicket application is running!";
    }
}